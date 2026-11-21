package com.app.yeoback_backend.service;

import com.app.yeoback_backend.common.enumeration.ContactMethod;
import com.app.yeoback_backend.common.enumeration.MemberRole;
import com.app.yeoback_backend.common.enumeration.OAuthProvider;
import com.app.yeoback_backend.dto.MemberDTO;
import com.app.yeoback_backend.dto.OAuthDTO;
import com.app.yeoback_backend.repository.MemberDAO;
import com.app.yeoback_backend.repository.OAuthDAO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 소셜 로그인(카카오 · 네이버 · 구글) 회원 처리
 *  1. 이미 연동된 소셜 계정(provider + providerId) → 그 회원으로 로그인
 *  2. 같은 이메일의 기존 회원 → 소셜 계정을 연동하고 로그인
 *  3. 처음 온 사람 → 소셜 정보(이름 · 이메일)로 바로 가입하고 로그인 (비밀번호 없음, 자동 닉네임 · 기본 설정)
 * 반환값은 JWT 의 subject 로 쓰는 로그인 아이디(이메일, 없으면 휴대폰)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthLoginService {
    // tbl_member.member_name varchar(10)
    private static final int NAME_MAX_LENGTH = 10;

    private final MemberDAO memberDAO;
    private final OAuthDAO oAuthDAO;
    private final MemberDefaultsService memberDefaultsService;

    /** 소셜 로그인을 진행할 수 없을 때. reason 은 프론트 로그인 화면이 안내 문구를 고르는 데 쓴다 */
    @Getter
    public static class OAuthLoginException extends RuntimeException {
        private final String reason;

        public OAuthLoginException(String reason, String message) {
            super(message);
            this.reason = reason;
        }
    }

    @Transactional
    public String login(String providerValue, String providerId, String email, String name, String profileUrl) {
        OAuthProvider provider = OAuthProvider.getOAuthProvider(providerValue);
        if (provider == null || providerId == null || providerId.isBlank()) {
            throw new OAuthLoginException("failed", "알 수 없는 소셜 로그인 응답: " + providerValue);
        }

        // 1. 이미 연동된 소셜 계정
        Optional<MemberDTO> linked = oAuthDAO.findMemberByProviderId(provider, providerId);
        if (linked.isPresent()) {
            return activeLoginId(linked.get());
        }

        // 소셜 계정을 새로 연결하려면 이메일이 있어야 한다 (로그인 아이디로 사용)
        String normalizedEmail = email == null ? null : ContactMethod.EMAIL.normalize(email);
        if (normalizedEmail == null || !ContactMethod.EMAIL.isValid(normalizedEmail)) {
            throw new OAuthLoginException("no_email", provider.getValue() + " 계정에서 이메일을 받지 못함");
        }

        // 2. 같은 이메일의 기존 회원 → 연동
        Optional<MemberDTO> sameEmail = memberDAO.findMemberByLoginId(normalizedEmail);
        if (sameEmail.isPresent()) {
            link(sameEmail.get().getId(), provider, providerId, profileUrl);
            log.info("oauth linked to existing member: id={}, provider={}", sameEmail.get().getId(), provider.getValue());
            return normalizedEmail;
        }
        // 이메일은 있지만 활동 중이 아닌 회원(정지 · 탈퇴)
        if (memberDAO.existsByLoginId(normalizedEmail)) {
            throw new OAuthLoginException("unavailable", "활동 중이 아닌 회원의 이메일: " + provider.getValue());
        }

        // 3. 신규 가입: tbl_member → tbl_auth(member) → tbl_oauth
        MemberDTO member = new MemberDTO();
        member.setMemberEmail(normalizedEmail);
        member.setMemberName(trimName(name));
        member.setMemberAlias(memberDefaultsService.newNickname());
        memberDAO.save(member);

        member.setMemberRole(MemberRole.MEMBER);
        memberDAO.saveRole(member);
        memberDefaultsService.createDefaultSetting(member.getId());

        link(member.getId(), provider, providerId, profileUrl);
        log.info("oauth member joined: id={}, provider={}", member.getId(), provider.getValue());
        return normalizedEmail;
    }

    private void link(Long memberId, OAuthProvider provider, String providerId, String profileUrl) {
        OAuthDTO oAuthDTO = new OAuthDTO();
        oAuthDTO.setMemberId(memberId);
        oAuthDTO.setProvider(provider);
        oAuthDTO.setProviderId(providerId);
        oAuthDTO.setProfileURL(profileUrl);
        oAuthDAO.save(oAuthDTO.toOAuthVO());
    }

    /** 연동된 회원이 지금도 로그인할 수 있는지 (활동 중인 회원 조회로 확인) */
    private String activeLoginId(MemberDTO member) {
        String loginId = member.getMemberEmail() != null ? member.getMemberEmail() : member.getMemberPhone();
        if (loginId == null || memberDAO.findMemberByLoginId(loginId).isEmpty()) {
            throw new OAuthLoginException("unavailable", "활동 중이 아닌 연동 회원: id=" + member.getId());
        }
        return loginId;
    }

    private String trimName(String name) {
        if (name == null || name.isBlank()) return "여행자";
        String v = name.trim();
        return v.length() > NAME_MAX_LENGTH ? v.substring(0, NAME_MAX_LENGTH) : v;
    }
}
