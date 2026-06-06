package com.auvan.backend.route.controller;

import com.auvan.backend.shared.dto.ApiResponse;
import com.auvan.backend.route.dto.RouteResponse;
import com.auvan.backend.route.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/liff/routes")
public class LiffRouteController {

    private final RouteService routeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RouteResponse>>> listActiveRoutes() {
        return ResponseEntity.ok(ApiResponse.success(routeService.listActiveRoutes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(routeService.getById(id)));
    }
}
