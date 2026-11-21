package com.app.yeoback_backend.service.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 이메일 인증코드 발송
 *  - Gmail SMTP (spring.mail.*) 사용, 발신 주소는 spring.mail.username
 *    코드 생성·비교는 VerificationService 가 서버에서 하고, 여기서는 발송만 한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public void sendCode(String email, String title, String code, long ttlMinutes) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            // 발신 주소는 기존 메일 계정 설정을 그대로 사용한다.
            if (senderEmail != null && !senderEmail.isBlank()) {
                helper.setFrom(senderEmail);
            }
            helper.setTo(email);
            helper.setSubject("[여백] " + title + " 인증코드");
            helper.setText("[여백] " + title + " 인증코드는 [" + code + "] 입니다.\n"
                    + ttlMinutes + "분 안에 입력해 주세요.", false);

            javaMailSender.send(mimeMessage);
            log.info("verification mail sent");
        } catch (Exception e) {
            throw new IllegalStateException("메일 발송 실패: " + e.getMessage(), e);
        }
    }
}
