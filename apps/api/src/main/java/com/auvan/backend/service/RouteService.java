package com.auvan.backend.service;

import com.auvan.backend.dto.request.CreateRouteRequest;
import com.auvan.backend.dto.request.UpdateRouteRequest;
import com.auvan.backend.dto.response.RouteResponse;

import java.util.List;
import java.util.UUID;

public interface RouteService {

    List<RouteResponse> listActiveRoutes();

    List<RouteResponse> listAllRoutes();

    RouteResponse getById(UUID id);

    RouteResponse create(CreateRouteRequest request);

    RouteResponse update(UUID id, UpdateRouteRequest request);

    void delete(UUID id);
}
