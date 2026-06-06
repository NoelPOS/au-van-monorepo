package com.auvan.backend.route.service;

import com.auvan.backend.route.dto.CreateRouteRequest;
import com.auvan.backend.route.dto.UpdateRouteRequest;
import com.auvan.backend.route.dto.RouteResponse;
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
