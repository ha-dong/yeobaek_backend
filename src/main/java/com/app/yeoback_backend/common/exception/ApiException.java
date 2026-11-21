package com.app.yeoback_backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 프론트에 상태코드 + reason 으로 알려줄 예외.
 * 응답 형태: { "reason": "duplicate", "message": "이미 가입된 계정이에요." }
 */
@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String reason;

    public ApiException(HttpStatus status, String reason, String message) {
        super(message);
        this.status = status;
        this.reason = reason;
    }
}
