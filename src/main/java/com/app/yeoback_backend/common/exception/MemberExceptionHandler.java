package com.app.yeoback_backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 회원·인증 API 예외 → JSON 응답.
 * (기존 basePackages 가 com.app.oauth... 로 되어 있어 적용되지 않던 것을 실제 패키지로 수정)
 * 화면은 React 가 그리므로 redirect 대신 상태코드와 reason 을 내려준다.
 * 여기서 잡지 않는 예외(NullPointerException 등)는 그대로 500 으로 나간다.
 */
@RestControllerAdvice(basePackages = "com.app.yeoback_backend.controller")
@Slf4j
public class MemberExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApiException(ApiException e) {
        return body(e.getStatus(), e.getReason(), e.getMessage());
    }

    // 로그인 실패 (아이디 없음 · 비밀번호 불일치 모두 같은 안내)
    @ExceptionHandler({MemberLoginFailException.class, AuthenticationException.class})
    public ResponseEntity<Map<String, String>> handleLoginFail(RuntimeException e) {
        log.info("login failed: {}", e.getMessage());
        return body(HttpStatus.UNAUTHORIZED, "invalid", "아이디 또는 비밀번호가 일치하지 않아요.");
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleMemberNotFound(MemberNotFoundException e) {
        return body(HttpStatus.NOT_FOUND, "not_found", "회원을 찾을 수 없어요.");
    }

    // 이메일/휴대폰 unique 제약 위반 (동시에 가입한 경우 등)
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateKey(DuplicateKeyException e) {
        return body(HttpStatus.CONFLICT, "duplicate", "이미 가입된 계정이에요.");
    }

    private ResponseEntity<Map<String, String>> body(HttpStatus status, String reason, String message) {
        return ResponseEntity.status(status).body(Map.of("reason", reason, "message", message == null ? "" : message));
    }
}
