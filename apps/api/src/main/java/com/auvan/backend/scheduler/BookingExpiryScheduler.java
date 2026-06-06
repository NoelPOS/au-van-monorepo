package com.auvan.backend.scheduler;

import com.auvan.backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    @Value("${booking.expiry.sweep.limit:30}")
    private int sweepLimit;

    private final BookingService bookingService;

    @Scheduled(cron = "0 */5 * * * *")
    public void expireUnpaidBookings() {
        int expired = bookingService.expireUnpaidBookings(sweepLimit);
        if (expired > 0) {
            log.info("Booking expiry scheduler cancelled {} unpaid booking(s)", expired);
        }
    }
}
