package com.auvan.backend.seat.service;

import com.auvan.backend.seat.Seat;
import com.auvan.backend.timeslot.Timeslot;
import com.auvan.backend.seat.SeatStatus;
import com.auvan.backend.shared.exception.SeatLockException;

import com.auvan.backend.seat.SeatRepository;
import com.auvan.backend.timeslot.TimeslotRepository;
import com.auvan.backend.seat.service.SeatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class SeatServiceImplTest {

    @Mock private SeatRepository seatRepository;
    @Mock private TimeslotRepository timeslotRepository;
    @InjectMocks private SeatServiceImpl seatService;

    @BeforeEach
    void setUp() {
        setField(seatService, "seatLockTimeoutSeconds", 300);
    }

    @Test
    void lockSeatsSuccess() {
        UUID timeslotId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Timeslot timeslot = new Timeslot();
        timeslot.setId(timeslotId);

        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setTimeslot(timeslot);
        seat.setStatus(SeatStatus.AVAILABLE);

        when(seatRepository.releaseExpiredLocks(any())).thenReturn(0);
        when(seatRepository.findByIdInAndTimeslotIdWithLock(List.of(seatId), timeslotId)).thenReturn(List.of(seat));
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Seat> locked = seatService.lockSeats(timeslotId, List.of(seatId), userId);

        assertThat(locked).hasSize(1);
        assertThat(locked.get(0).getStatus()).isEqualTo(SeatStatus.LOCKED);
        assertThat(locked.get(0).getLockedBy()).isEqualTo(userId);
        assertThat(locked.get(0).getLockedAt()).isNotNull();
    }

    @Test
    void lockSeatsWhenUnavailableThrows() {
        UUID timeslotId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Timeslot timeslot = new Timeslot();
        timeslot.setId(timeslotId);

        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setTimeslot(timeslot);
        seat.setStatus(SeatStatus.LOCKED);

        when(seatRepository.releaseExpiredLocks(any())).thenReturn(0);
        when(seatRepository.findByIdInAndTimeslotIdWithLock(List.of(seatId), timeslotId)).thenReturn(List.of(seat));

        assertThatThrownBy(() -> seatService.lockSeats(timeslotId, List.of(seatId), userId))
                .isInstanceOf(SeatLockException.class)
                .hasMessageContaining("no longer available");

        verify(seatRepository, never()).saveAll(anyList());
    }

    @Test
    void releaseExpiredLocksReturnsCount() {
        when(seatRepository.releaseExpiredLocks(any())).thenReturn(3);

        int released = seatService.releaseExpiredLocks();

        assertThat(released).isEqualTo(3);
        verify(seatRepository).releaseExpiredLocks(any(Instant.class));
    }
}
