package com.app.yeoback_backend.service;

import com.app.yeoback_backend.common.enumeration.CodePurpose;
import com.app.yeoback_backend.common.enumeration.ContactMethod;
import com.app.yeoback_backend.common.exception.ApiException;
import com.app.yeoback_backend.dto.auth.SentCodeDTO;
import com.app.yeoback_backend.dto.auth.VerifyResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 인증코드 발급 · 확인 (서버 메모리 보관)
 *  - Redis 를 쓰지 않으므로 서버를 재시작하면 진행 중인 인증은 사라진다.
 *  - 규칙은 기존 프론트 테스트 코드와 같다: 영문·숫자 4자리, 5분 유효, 60초 재전송 대기, 5회 시도
 *  - 확인에 성공하면 10분 동안 "인증 완료" 상태가 남고, 회원가입/비밀번호 재설정이 그것을 사용한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {
    private static final int CODE_LENGTH = 4;
    private static final long CODE_TTL_MS = 5 * 60 * 1000L;
    private static final long RESEND_COOLDOWN_MS = 60 * 1000L;
    private static final long VERIFIED_TTL_MS = 10 * 60 * 1000L;
    private static final int MAX_ATTEMPTS = 5;
    // 헷갈리기 쉬운 문자(0, O, 1, I)는 제외
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final CodeSendService codeSendService;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Pending> pending = new ConcurrentHashMap<>();
    private final Map<String, Long> verified = new ConcurrentHashMap<>();

    @Value("${verification.expose-code:false}")
    private boolean exposeCode;

    private static final class Pending {
        final String code;
        final long sentAt;
        final long expiresAt;
        int attempts;

        Pending(String code, long sentAt) {
            this.code = code;
            this.sentAt = sentAt;
            this.expiresAt = sentAt + CODE_TTL_MS;
        }
    }

    /** contact 는 이미 normalize 된 값 */
    public SentCodeDTO send(CodePurpose purpose, ContactMethod method, String contact) {
        String key = key(purpose, contact);
        long now = System.currentTimeMillis();

        Pending prev = pending.get(key);
        if (prev != null && now < prev.sentAt + RESEND_COOLDOWN_MS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "cooldown", "잠시 후 다시 요청해 주세요.");
        }

        Pending next = new Pending(generateCode(), now);
        try {
            codeSendService.send(method, contact, purpose, next.code, CODE_TTL_MS / 60000);
        } catch (IllegalStateException e) {
            if (!exposeCode) {
                // 실패 원인(SMTP 인증 실패, 발신번호 미등록 등)을 로그로 남겨 추적할 수 있게 한다
                log.error("인증코드 발송 실패 ({}): {}", method.getValue(), e.getMessage(), e);
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "send_failed",
                        "인증코드를 보내지 못했어요. 잠시 후 다시 시도해 주세요.");
            }
            // 개발용: 발송이 안 돼도 화면의 testCode 로 진행할 수 있게 한다
            log.warn("{} (expose-code=true 이므로 계속 진행)", e.getMessage());
        }
        pending.put(key, next);
        verified.remove(key);

        return new SentCodeDTO(next.expiresAt, next.sentAt + RESEND_COOLDOWN_MS, exposeCode ? next.code : null);
    }

    public VerifyResultDTO verify(CodePurpose purpose, String contact, String input) {
        String key = key(purpose, contact);
        Pending p = pending.get(key);
        if (p == null) return VerifyResultDTO.fail("no_code");
        if (System.currentTimeMillis() > p.expiresAt) return VerifyResultDTO.fail("expired");

        synchronized (p) {
            if (p.attempts >= MAX_ATTEMPTS) return VerifyResultDTO.fail("too_many");
            if (input == null || !p.code.equals(input.trim().toUpperCase())) {
                p.attempts += 1;
                int left = MAX_ATTEMPTS - p.attempts;
                return left <= 0 ? VerifyResultDTO.fail("too_many") : VerifyResultDTO.mismatch(left);
            }
        }
        pending.remove(key);
        verified.put(key, System.currentTimeMillis() + VERIFIED_TTL_MS);
        return VerifyResultDTO.success();
    }

    /** 인증을 마쳤는지 확인 (아직 소모하지 않음) */
    public boolean isVerified(CodePurpose purpose, String contact) {
        Long until = verified.get(key(purpose, contact));
        return until != null && System.currentTimeMillis() <= until;
    }

    /** 가입/재설정이 끝난 뒤 인증 완료 상태를 지운다 */
    public void consume(CodePurpose purpose, String contact) {
        verified.remove(key(purpose, contact));
    }

    private String key(CodePurpose purpose, String contact) {
        return purpose.getValue() + ":" + contact;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        return sb.toString();
    }
}
