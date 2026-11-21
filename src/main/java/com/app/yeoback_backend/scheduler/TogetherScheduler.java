package com.app.yeoback_backend.scheduler;

import com.app.yeoback_backend.service.TogetherService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TogetherScheduler {
    private final TogetherService togetherService;

    @Scheduled(fixedDelay = 300000) // 5분(300000ms)마다 실행 → 조용한 지 2시간~2시간 5분 사이에 끝남
    public void endFinished() {
        togetherService.endFinished();
    }
}