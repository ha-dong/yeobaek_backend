package com.app.yeoback_backend.common.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException() {
        ;
    }

    public MemberNotFoundException(String message) {
        super(message);
    }
}
