package com.app.yeoback_backend.service;

import com.app.yeoback_backend.common.enumeration.CodePurpose;
import com.app.yeoback_backend.common.enumeration.ContactMethod;
import com.app.yeoback_backend.service.mail.MailService;
import com.app.yeoback_backend.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 인증코드 발송 창구: 수단에 따라 이메일(MailService) 또는 문자(SmsService)로 보낸다.
 * 발송에 실패하면 IllegalStateException 을 던지고, 처리 방식은 VerificationService 가 정한다.
 */
@Service
@RequiredArgsConstructor
public class CodeSendService {
    private final MailService mailService;
    private final SmsService smsService;

    public void send(ContactMethod method, String contact, CodePurpose purpose, String code, long ttlMinutes) {
        String title = purpose == CodePurpose.SIGNUP ? "회원가입" : "비밀번호 재설정";
        if (method == ContactMethod.EMAIL) {
            mailService.sendCode(contact, title, code, ttlMinutes);
        } else {
            smsService.sendCode(contact, title, code, ttlMinutes);
        }
    }
}
