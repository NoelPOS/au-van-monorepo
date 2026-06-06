package com.auvan.backend.service;

import com.auvan.backend.dto.request.BulkCreateTimeslotRequest;
import com.auvan.backend.dto.request.CreateTimeslotRequest;
import com.auvan.backend.dto.request.UpdateTimeslotRequest;
import com.auvan.backend.dto.response.TimeslotResponse;
import com.auvan.backend.entity.Timeslot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TimeslotService {

    List<TimeslotResponse> listAvailable(UUID routeId, LocalDate fromDate);

    TimeslotResponse getById(UUID id);

    Timeslot getEntityById(UUID id);

    List<TimeslotResponse> listAll(int page, int size);

    TimeslotResponse create(CreateTimeslotRequest request);

    List<TimeslotResponse> bulkCreate(BulkCreateTimeslotRequest request);

    TimeslotResponse update(UUID id, UpdateTimeslotRequest request);

    void delete(UUID id);
}
