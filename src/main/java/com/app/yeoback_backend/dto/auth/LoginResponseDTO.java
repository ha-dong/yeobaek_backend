package com.app.yeoback_backend.dto.auth;

import com.app.yeoback_backend.dto.MemberDTO;

/** POST /api/auth/login 응답: 프론트가 accessToken 과 member 를 localStorage 에 저장한다 */
public record LoginResponseDTO(String accessToken, MemberDTO member) {
}
