package com.auvan.backend.integration;

import com.auvan.backend.route.entity.Route;
import com.auvan.backend.seat.entity.Seat;
import com.auvan.backend.timeslot.entity.Timeslot;
import com.auvan.backend.user.entity.User;
import com.auvan.backend.auth.enums.AuthProvider;
import com.auvan.backend.route.enums.RouteStatus;
import com.auvan.backend.seat.enums.SeatStatus;
import com.auvan.backend.timeslot.enums.TimeslotStatus;
import com.auvan.backend.shared.exception.SeatLockException;
import com.auvan.backend.route.repository.RouteRepository;
import com.auvan.backend.seat.repository.SeatRepository;
import com.auvan.backend.timeslot.repository.TimeslotRepository;
import com.auvan.backend.user.repository.UserRepository;
import com.auvan.backend.seat.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThat;

class SeatLockingIntegrationTest extends AbstractIntegrationTest {

    @Autowired private SeatService seatService;
    @Autowired private UserRepository userRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private TimeslotRepository timeslotRepository;
    @Autowired private SeatRepository seatRepository;

    @BeforeEach
    void cleanDatabase() {
        resetDatabase();
    }

    @Test
    void concurrentLockRequestsOnlyOneShouldSucceed() {
        User userA = new User();
        userA.setName("User A");
        userA.setEmail("a@auvan.app");
        userA.setAuthProvider(AuthProvider.LOCAL);
        userA = userRepository.save(userA);

        User userB = new User();
        userB.setName("User B");
        userB.setEmail("b@auvan.app");
        userB.setAuthProvider(AuthProvider.LOCAL);
        userB = userRepository.save(userB);

        Route route = new Route();
        route.setFromLocation("ABAC");
        route.setToLocation("BTS Udomsuk");
        route.setSlug("abac-bts-locking");
        route.setPrice(BigDecimal.valueOf(80));
        route.setStatus(RouteStatus.ACTIVE);
        route = routeRepository.save(route);

        Timeslot timeslot = new Timeslot();
        timeslot.setRoute(route);
        timeslot.setDate(LocalDate.now().plusDays(1));
        timeslot.setTime(LocalTime.of(10, 0));
        timeslot.setTotalSeats(10);
        timeslot.setStatus(TimeslotStatus.ACTIVE);
        timeslot = timeslotRepository.save(timeslot);

        Seat seat = new Seat();
        seat.setTimeslot(timeslot);
        seat.setSeatNumber(1);
        seat.setLabel(Seat.buildLabel(1));
        seat.setStatus(SeatStatus.AVAILABLE);
        seat = seatRepository.save(seat);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            UUID seatId = seat.getId();
            UUID timeslotId = timeslot.getId();
            UUID userAId = userA.getId();
            UUID userBId = userB.getId();

            CompletableFuture<Boolean> lockByA = CompletableFuture.supplyAsync(
                    () -> tryLock(timeslotId, seatId, userAId), executor);
            CompletableFuture<Boolean> lockByB = CompletableFuture.supplyAsync(
                    () -> tryLock(timeslotId, seatId, userBId), executor);

            boolean aSuccess = lockByA.join();
            boolean bSuccess = lockByB.join();

            assertThat(aSuccess ^ bSuccess).isTrue();

            Seat refreshed = seatRepository.findById(seatId).orElseThrow();
            assertThat(refreshed.getStatus()).isEqualTo(SeatStatus.LOCKED);
            assertThat(refreshed.getLockedBy()).isIn(userAId, userBId);
        } finally {
            executor.shutdown();
        }
    }

    private boolean tryLock(UUID timeslotId, UUID seatId, UUID userId) {
        try {
            seatService.lockSeats(timeslotId, List.of(seatId), userId);
            return true;
        } catch (SeatLockException ex) {
            return false;
        }
    }
}
