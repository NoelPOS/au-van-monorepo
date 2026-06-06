package com.auvan.backend.timeslot.service;

import com.auvan.backend.timeslot.dto.BulkCreateTimeslotRequest;
import com.auvan.backend.timeslot.dto.CreateTimeslotRequest;
import com.auvan.backend.timeslot.dto.UpdateTimeslotRequest;
import com.auvan.backend.timeslot.dto.TimeslotResponse;
import com.auvan.backend.timeslot.entity.Timeslot;
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
