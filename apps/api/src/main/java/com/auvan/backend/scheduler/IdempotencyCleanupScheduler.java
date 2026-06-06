package com.auvan.backend.scheduler;

import com.auvan.backend.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyCleanupScheduler {

    private final IdempotencyService idempotencyService;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredIdempotencyKeys() {
        int deleted = idempotencyService.deleteExpired();
        if (deleted > 0) {
            log.debug("Removed {} expired idempotency key record(s)", deleted);
        }
    }
}
