package com.app.yeoback_backend.common.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/** 인증코드 용도 (회원가입 · 비밀번호 재설정) */
public enum CodePurpose {
    SIGNUP("signup"), RESET("reset");

    private final String value;

    CodePurpose(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CodePurpose from(String value) {
        return Arrays.stream(values()).filter(p -> p.value.equals(value)).findFirst().orElse(null);
    }
}
