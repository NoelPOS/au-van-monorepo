package com.auvan.backend.scheduler;

import com.auvan.backend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    @Value("${reminder.schedule.mode:daily_batch}")
    private String scheduleMode;

    private final ReminderService reminderService;

    @Scheduled(cron = "0 * * * * *")
    public void processPendingReminders() {
        if (!"timed".equalsIgnoreCase(scheduleMode)) {
            return;
        }

        int processed = reminderService.processPendingReminders(50);
        if (processed > 0) {
            log.debug("Reminder scheduler processed {} reminder job(s)", processed);
        }
    }
}
