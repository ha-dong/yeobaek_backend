package com.app.yeoback_backend.dto.auth;

import com.app.yeoback_backend.common.enumeration.CodePurpose;
import com.app.yeoback_backend.common.enumeration.ContactMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** 인증코드 발송(/api/auth/code/send) · 확인(/api/auth/code/verify) 요청 */
@Getter @Setter @NoArgsConstructor @ToString
public class CodeRequestDTO {
    private CodePurpose purpose;
    private ContactMethod method;
    private String contact;
    private String code;            // 확인할 때만 사용
}
