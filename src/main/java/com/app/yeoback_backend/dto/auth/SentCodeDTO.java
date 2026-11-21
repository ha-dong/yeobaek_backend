package com.app.yeoback_backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 인증코드 발송 결과. 시간은 epoch milliseconds.
 * testCode 는 verification.expose-code=true(개발용)일 때만 채워진다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SentCodeDTO(long expiresAt, long resendAvailableAt, String testCode) {
}
