package com.app.yeoback_backend.service;

import com.app.yeoback_backend.mapper.MemberSettingMapper;
import com.app.yeoback_backend.repository.MemberDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * 새 회원의 기본값 (일반 가입 MemberService.join · 소셜 가입 OAuthLoginService 공통)
 *  - 닉네임: '여백' + 임의 숫자 6자리 (예: 여백482913)
 *    회원 id 를 붙이면 가입 순서와 전체 회원 수가 드러나서 임의 숫자를 쓴다.
 *  - 설정: tbl_member_setting 에 기본값(create.sql default) 행 생성
 */
@Service
@RequiredArgsConstructor
public class MemberDefaultsService {
    private static final String NICKNAME_PREFIX = "여백";
    private static final int MAX_TRIES = 10;

    private final MemberDAO memberDAO;
    private final MemberSettingMapper memberSettingMapper;
    private final SecureRandom random = new SecureRandom();

    /** 아직 쓰이지 않는 자동 닉네임 (10번 모두 겹치면 마지막 값을 그대로 쓴다 — 닉네임은 unique 가 아님) */
    public String newNickname() {
        String candidate = null;
        for (int i = 0; i < MAX_TRIES; i++) {
            candidate = NICKNAME_PREFIX + String.format("%06d", random.nextInt(1_000_000));
            if (!memberDAO.existsByAlias(candidate)) return candidate;
        }
        return candidate;
    }

    /** 가입 직후 기본 설정 행 만들기 (이미 있으면 그대로) */
    public void createDefaultSetting(Long memberId) {
        memberSettingMapper.insertDefault(memberId);
    }
}
