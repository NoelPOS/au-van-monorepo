package com.auvan.backend.reminder.service;

import java.util.UUID;

public interface ReminderService {

    /** Schedules reminder job(s) for the given booking based on configured mode. */
    void scheduleForBooking(UUID bookingId);

    /** Cancels all pending reminder jobs for the given booking (e.g. on cancellation). */
    void cancelForBooking(UUID bookingId);

    /**
     * Processes up to {@code limit} pending reminder jobs that are due.
     * Uses pessimistic locking to safely run under concurrent workers.
     *
     * @return number of jobs processed
     */
    int processPendingReminders(int limit);
}
