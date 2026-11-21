package com.app.yeoback_backend.dto.profile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** PUT /api/profile 요청 — 프로필 수정 화면(닉네임 · 소개) */
@Getter @Setter @ToString
@NoArgsConstructor
public class ProfileUpdateRequestDTO {
    private String alias;   // 2~12자
    private String bio;     // 80자 이내, 비워도 됨
}
