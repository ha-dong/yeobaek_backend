package com.app.yeoback_backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 인증코드 확인 결과. reason: mismatch · expired · too_many · no_code */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VerifyResultDTO(boolean ok, String reason, Integer attemptsLeft) {
    public static VerifyResultDTO success() {
        return new VerifyResultDTO(true, null, null);
    }

    public static VerifyResultDTO fail(String reason) {
        return new VerifyResultDTO(false, reason, null);
    }

    public static VerifyResultDTO mismatch(int attemptsLeft) {
        return new VerifyResultDTO(false, "mismatch", attemptsLeft);
    }
}
