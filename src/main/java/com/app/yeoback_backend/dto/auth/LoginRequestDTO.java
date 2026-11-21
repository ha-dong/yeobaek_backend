package com.app.yeoback_backend.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** POST /api/auth/login 요청 */
@Getter @Setter @NoArgsConstructor
@ToString(exclude = "memberPassword")
public class LoginRequestDTO {
    private String loginId;         // 이메일 또는 휴대폰 번호
    private String memberPassword;
}
