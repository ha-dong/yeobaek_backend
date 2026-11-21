package com.app.yeoback_backend.dto.profile;

import java.util.List;

/**
 * GET /api/profile 응답 — 개인 프로필 화면(Mobile 20)과 세부 설정(Mobile 22)에 필요한 값 전부
 *  avatarUrl : 프로필 사진 주소(/api/files/{id}), 없으면 null (화면은 닉네임 첫 글자)
 *  themeColor: 팔레트 id(sky·aqua·blossom·lavender·mint) 또는 직접 고른 #rrggbb
 */
public record ProfileResponseDTO(
        Long id,
        String name,
        String email,
        String phone,
        String alias,
        String bio,
        String avatarUrl,
        List<String> preferences,
        List<String> vibes,
        boolean aliasPublic,
        boolean phonePublic,
        boolean noteAutoSave,
        String themeColor
) {
}
