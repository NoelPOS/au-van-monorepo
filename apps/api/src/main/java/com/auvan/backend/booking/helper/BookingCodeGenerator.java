package com.auvan.backend.booking.helper;

import com.auvan.backend.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class BookingCodeGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int MAX_ATTEMPTS = 10;

    private final BookingRepository bookingRepository;

    public String generate() {
        String datePart = LocalDate.now().format(DATE_FORMAT);

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = "AUV-" + datePart + "-" + RandomStringUtils.secure()
                    .nextAlphanumeric(5)
                    .toUpperCase();

            if (!bookingRepository.existsByBookingCode(code)) {
                return code;
            }
        }

        throw new IllegalStateException("Could not generate unique booking code after 10 attempts");
    }
}
