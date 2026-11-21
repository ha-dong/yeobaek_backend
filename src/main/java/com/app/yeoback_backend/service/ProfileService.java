package com.app.yeoback_backend.service;

import com.app.yeoback_backend.common.exception.ApiException;
import com.app.yeoback_backend.common.exception.MemberNotFoundException;
import com.app.yeoback_backend.dto.FileDTO;
import com.app.yeoback_backend.dto.MemberDTO;
import com.app.yeoback_backend.dto.MemberSettingDTO;
import com.app.yeoback_backend.dto.profile.ProfileResponseDTO;
import com.app.yeoback_backend.dto.profile.ProfileUpdateRequestDTO;
import com.app.yeoback_backend.dto.profile.SettingUpdateRequestDTO;
import com.app.yeoback_backend.mapper.FileMapper;
import com.app.yeoback_backend.mapper.MemberSettingMapper;
import com.app.yeoback_backend.repository.MemberDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 개인 프로필 · 세부 설정 조회와 수정
 *  - 조회: tbl_member(닉네임 · 소개) + tbl_member_setting + 취향 · 키워드 + 프로필 사진
 *  - 설정 행이나 닉네임이 없는 회원(이 기능 이전 가입자)은 조회할 때 기본값으로 채운다
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    private static final int ALIAS_MIN = 2;
    private static final int ALIAS_MAX = 12;
    private static final int BIO_MAX = 80;
    private static final int CHOICE_MAX = 30;
    // 프론트 lib/accent.ts 의 ACCENT_PALETTES id, 또는 직접 고른 #rrggbb
    private static final Pattern THEME_COLOR = Pattern.compile("^(sky|aqua|blossom|lavender|mint|#[0-9a-f]{6})$");

    private final MemberDAO memberDAO;
    private final MemberSettingMapper settingMapper;
    private final FileMapper fileMapper;
    private final FileStorageService fileStorageService;
    private final MemberDefaultsService memberDefaultsService;

    @Transactional
    public ProfileResponseDTO getProfile(Long memberId) {
        MemberDTO member = member(memberId);

        // 이 기능 이전에 가입한 회원 보정
        if (member.getMemberAlias() == null || member.getMemberAlias().isBlank()) {
            member.setMemberAlias(memberDefaultsService.newNickname());
            memberDAO.updateProfile(memberId, member.getMemberAlias(), member.getMemberBio());
        }
        MemberSettingDTO setting = settingMapper.selectByMemberId(memberId).orElseGet(() -> {
            memberDefaultsService.createDefaultSetting(memberId);
            return settingMapper.selectByMemberId(memberId).orElseThrow();
        });

        String avatarUrl = fileMapper.selectMemberFile(memberId).map(f -> "/api/files/" + f.getId()).orElse(null);

        return new ProfileResponseDTO(
                member.getId(),
                member.getMemberName(),
                member.getMemberEmail(),
                member.getMemberPhone(),
                member.getMemberAlias(),
                member.getMemberBio(),
                avatarUrl,
                settingMapper.selectPreferenceNames(memberId),
                settingMapper.selectVibeNames(memberId),
                Boolean.TRUE.equals(setting.getAliasPublic()),
                Boolean.TRUE.equals(setting.getPhonePublic()),
                Boolean.TRUE.equals(setting.getNoteAutoSave()),
                setting.getThemaColor());
    }

    /** 프로필 수정 화면: 닉네임 · 소개 */
    @Transactional
    public ProfileResponseDTO updateProfile(Long memberId, ProfileUpdateRequestDTO request) {
        String alias = request.getAlias() == null ? "" : request.getAlias().trim();
        if (alias.length() < ALIAS_MIN || alias.length() > ALIAS_MAX) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "닉네임은 2~12자로 적어 주세요.");
        }
        String bio = request.getBio() == null ? "" : request.getBio().trim();
        if (bio.length() > BIO_MAX) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "소개는 80자 이내로 적어 주세요.");
        }
        member(memberId);
        memberDAO.updateProfile(memberId, alias, bio);
        return getProfile(memberId);
    }

    /** 세부 설정 화면: 보낸 항목만 바꾼다 */
    @Transactional
    public ProfileResponseDTO updateSettings(Long memberId, SettingUpdateRequestDTO request) {
        getProfile(memberId); // 회원 확인 + 설정 행 보정

        MemberSettingDTO setting = new MemberSettingDTO();
        setting.setId(memberId);
        setting.setAliasPublic(request.getAliasPublic());
        setting.setPhonePublic(request.getPhonePublic());
        setting.setNoteAutoSave(request.getNoteAutoSave());
        if (request.getThemeColor() != null) {
            String color = request.getThemeColor().trim().toLowerCase();
            if (!THEME_COLOR.matcher(color).matches()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "알 수 없는 컬러 조합이에요.");
            }
            setting.setThemaColor(color);
        }
        settingMapper.update(setting);

        if (request.getPreferences() != null) {
            replaceChoices(memberId, request.getPreferences(), "여행 취향",
                    settingMapper::selectPreferenceIdsByNames,
                    settingMapper::deletePreferences,
                    ids -> settingMapper.insertPreferences(memberId, ids));
        }
        if (request.getVibes() != null) {
            replaceChoices(memberId, request.getVibes(), "여행 키워드",
                    settingMapper::selectVibeIdsByNames,
                    settingMapper::deleteVibes,
                    ids -> settingMapper.insertVibes(memberId, ids));
        }
        return getProfile(memberId);
    }

    /** 프로필 사진 올리기: 디스크 저장 → tbl_file · tbl_file_member → 이전 사진 삭제 */
    @Transactional
    public ProfileResponseDTO uploadAvatar(Long memberId, MultipartFile file) {
        member(memberId);
        Optional<FileDTO> previous = fileMapper.selectMemberFile(memberId);

        FileDTO saved = fileStorageService.saveImage(file);
        try {
            fileMapper.insert(saved);
            fileMapper.insertMemberFile(saved.getId(), memberId);
        } catch (RuntimeException e) {
            fileStorageService.delete(saved); // DB 저장에 실패하면 디스크에 남기지 않는다
            throw e;
        }
        previous.ifPresent(this::removeMemberFile);
        return getProfile(memberId);
    }

    /** 프로필 사진 지우기 (닉네임 첫 글자로 돌아감) */
    @Transactional
    public ProfileResponseDTO deleteAvatar(Long memberId) {
        member(memberId);
        fileMapper.selectMemberFile(memberId).ifPresent(this::removeMemberFile);
        return getProfile(memberId);
    }

    private void removeMemberFile(FileDTO file) {
        fileMapper.deleteMemberFile(file.getId());
        fileMapper.deleteById(file.getId());
        fileStorageService.delete(file);
    }

    private void replaceChoices(Long memberId, List<String> names, String label,
                                Function<List<String>, List<Long>> findIds,
                                java.util.function.Consumer<Long> deleteAll,
                                java.util.function.Consumer<List<Long>> insertAll) {
        List<String> unique = new ArrayList<>(new LinkedHashSet<>(names.stream()
                .filter(n -> n != null && !n.isBlank()).map(String::trim).toList()));
        if (unique.size() > CHOICE_MAX) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", label + "은(는) " + CHOICE_MAX + "개까지 고를 수 있어요.");
        }
        List<Long> ids = unique.isEmpty() ? List.of() : findIds.apply(unique);
        if (ids.size() != unique.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid", "알 수 없는 " + label + "이 있어요.");
        }
        deleteAll.accept(memberId);
        if (!ids.isEmpty()) insertAll.accept(ids);
    }

    private MemberDTO member(Long memberId) {
        return memberDAO.findById(memberId).orElseThrow(MemberNotFoundException::new);
    }
}
