package com.app.yeoback_backend.service.sms;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 휴대폰 문자 인증코드 발송 (SOLAPI)
 *  - message.api-key / message.api-secret-key : SOLAPI 키
 *  - message.sender-number                    : SOLAPI 에 등록된 발신번호 (등록된 번호만 발송 가능)
 *  - 코드 생성·비교는 VerificationService 가 하고, 여기서는 발송만 한다.
 */
@Service
@Slf4j
public class SmsService {

    @Value("${message.api-key:}")
    private String apiKey;

    @Value("${message.api-secret-key:}")
    private String apiSecretKey;

    @Value("${message.sender-number:}")
    private String senderNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    private void init() {
        // 애플리케이션 시작 시 SolAPI 클라이언트를 한 번만 준비한다. (키가 없으면 발송 시 실패 처리)
        if (apiKey.isBlank() || apiSecretKey.isBlank()) {
            log.warn("SOLAPI 키가 설정되지 않아 문자 발송을 할 수 없어요.");
            return;
        }
        this.messageService = SolapiClient.INSTANCE.createInstance(apiKey, apiSecretKey);
    }

    public void sendCode(String phone, String title, String code, long ttlMinutes) {
        if (messageService == null) {
            throw new IllegalStateException("문자 발송 실패: SOLAPI 키(message.api-key)가 설정되지 않았어요.");
        }
        if (senderNumber == null || senderNumber.isBlank()) {
            throw new IllegalStateException("문자 발송 실패: 발신번호(message.sender-number)가 설정되지 않았어요.");
        }

        Message message = new Message();
        // 하이픈 없이 숫자만 남겨 SolAPI 요청 형식에 맞춘다.
        message.setFrom(senderNumber.replaceAll("\\D", ""));
        message.setTo(phone == null ? "" : phone.replaceAll("\\D", ""));
        message.setText("[여백] " + title + " 인증코드: " + code + " (" + ttlMinutes + "분 이내 입력)");

        try {
            messageService.send(message);
            log.info("verification sms sent");
        } catch (SolapiMessageNotReceivedException e) {
            // SolAPI가 메시지를 정상 접수하지 못한 경우 (발신번호 미등록, 잔액 부족 등)
            throw new IllegalStateException("문자 발송 실패(접수 거부): " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalStateException("문자 발송 실패: " + e.getMessage(), e);
        }
    }
}
