package com.auvan.backend.service.impl;

import com.auvan.backend.mapper.EntityMappers;

import com.auvan.backend.dto.request.BulkCreateTimeslotRequest;
import com.auvan.backend.dto.request.CreateTimeslotRequest;
import com.auvan.backend.dto.request.UpdateTimeslotRequest;
import com.auvan.backend.dto.response.TimeslotResponse;
import com.auvan.backend.entity.Route;
import com.auvan.backend.entity.Seat;
import com.auvan.backend.entity.Timeslot;
import com.auvan.backend.enums.RouteStatus;
import com.auvan.backend.enums.SeatStatus;
import com.auvan.backend.enums.TimeslotStatus;
import com.auvan.backend.exception.ConflictException;
import com.auvan.backend.exception.ResourceNotFoundException;

import com.auvan.backend.repository.RouteRepository;
import com.auvan.backend.repository.SeatRepository;
import com.auvan.backend.repository.TimeslotRepository;
import com.auvan.backend.service.TimeslotService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeslotServiceImpl implements TimeslotService {

    private final TimeslotRepository timeslotRepository;
    private final RouteRepository    routeRepository;
    private final SeatRepository     seatRepository;
    private final EntityMappers mappers;

    @Override
    @Transactional(readOnly = true)
    public List<TimeslotResponse> listAvailable(UUID routeId, LocalDate fromDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now();
        return mappers.toTimeslotList(
                timeslotRepository.findByRouteIdAndDateGreaterThanEqualAndStatusOrderByDateAscTimeAsc(
                        routeId, from, TimeslotStatus.ACTIVE));
    }

    @Override
    @Transactional(readOnly = true)
    public TimeslotResponse getById(UUID id) {
        return mappers.toTimeslot(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Timeslot getEntityById(UUID id) {
        return timeslotRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Timeslot", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeslotResponse> listAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("date").descending().and(Sort.by("time")));
        return mappers.toTimeslotList(timeslotRepository.findAll(pageable).getContent());
    }

    @Override
    @Transactional
    public TimeslotResponse create(CreateTimeslotRequest request) {
        Route route = routeRepository.findById(request.routeId())
                .filter(r -> r.getStatus() == RouteStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.of("Route", request.routeId()));

        Timeslot timeslot = buildTimeslot(route, request.date(), request.time(), request.totalSeats());
        timeslot = timeslotRepository.save(timeslot);
        generateSeats(timeslot);
        return mappers.toTimeslot(timeslot);
    }

    @Override
    @Transactional
    public List<TimeslotResponse> bulkCreate(BulkCreateTimeslotRequest request) {
        Route route = routeRepository.findById(request.routeId())
                .filter(r -> r.getStatus() == RouteStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.of("Route", request.routeId()));

        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new ConflictException("dateFrom must not be after dateTo");
        }

        List<Timeslot> created = new ArrayList<>();
        LocalDate current = request.dateFrom();

        while (!current.isAfter(request.dateTo())) {
            if (request.daysOfWeek().contains(current.getDayOfWeek().getValue())) {
                for (var time : request.times()) {
                    Timeslot timeslot = buildTimeslot(route, current, time, request.totalSeats());
                    timeslot = timeslotRepository.save(timeslot);
                    generateSeats(timeslot);
                    created.add(timeslot);
                }
            }
            current = current.plusDays(1);
        }

        return mappers.toTimeslotList(created);
    }

    @Override
    @Transactional
    public TimeslotResponse update(UUID id, UpdateTimeslotRequest request) {
        Timeslot timeslot = getEntityById(id);

        if (request.date() != null)       timeslot.setDate(request.date());
        if (request.time() != null)       timeslot.setTime(request.time());
        if (request.totalSeats() != null) timeslot.setTotalSeats(request.totalSeats());
        if (request.status() != null)     timeslot.setStatus(request.status());

        return mappers.toTimeslot(timeslotRepository.save(timeslot));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Timeslot timeslot = getEntityById(id);
        timeslot.setStatus(TimeslotStatus.CANCELLED);
        timeslotRepository.save(timeslot);
    }

    private Timeslot buildTimeslot(Route route, LocalDate date,
                                   LocalTime time, int totalSeats) {
        Timeslot t = new Timeslot();
        t.setRoute(route);
        t.setDate(date);
        t.setTime(time);
        t.setTotalSeats(totalSeats);
        t.setStatus(TimeslotStatus.ACTIVE);
        return t;
    }

    private void generateSeats(Timeslot timeslot) {
        List<Seat> seats = new ArrayList<>(timeslot.getTotalSeats());
        for (int i = 1; i <= timeslot.getTotalSeats(); i++) {
            Seat seat = new Seat();
            seat.setTimeslot(timeslot);
            seat.setSeatNumber(i);
            seat.setLabel(Seat.buildLabel(i));
            seat.setStatus(SeatStatus.AVAILABLE);
            seats.add(seat);
        }
        seatRepository.saveAll(seats);
    }
}
