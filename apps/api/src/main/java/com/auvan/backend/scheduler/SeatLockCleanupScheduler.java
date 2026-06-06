package com.auvan.backend.scheduler;

import com.auvan.backend.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatLockCleanupScheduler {

    private final SeatService seatService;

    @Scheduled(fixedDelayString = "${seat.lock.cleanup.interval.ms:60000}")
    public void releaseExpiredSeatLocks() {
        int released = seatService.releaseExpiredLocks();
        if (released > 0) {
            log.debug("Seat lock cleanup released {} lock(s)", released);
        }
    }
}
