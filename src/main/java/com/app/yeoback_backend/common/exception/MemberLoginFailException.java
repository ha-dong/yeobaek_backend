package com.app.yeoback_backend.common.exception;

public class MemberLoginFailException extends RuntimeException {
    public MemberLoginFailException() {
    }

    public MemberLoginFailException(String message) {
        super(message);
    }
}
