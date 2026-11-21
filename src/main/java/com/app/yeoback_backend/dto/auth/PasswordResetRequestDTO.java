package com.app.yeoback_backend.dto.auth;

import com.app.yeoback_backend.common.enumeration.ContactMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** POST /api/auth/password/reset 요청 (인증코드 확인을 마친 뒤) */
@Getter @Setter @NoArgsConstructor
@ToString(exclude = "newPassword")
public class PasswordResetRequestDTO {
    private ContactMethod method;
    private String contact;
    private String newPassword;
}
