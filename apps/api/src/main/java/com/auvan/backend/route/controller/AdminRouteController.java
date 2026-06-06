package com.auvan.backend.route.controller;

import com.auvan.backend.route.dto.CreateRouteRequest;
import com.auvan.backend.route.dto.UpdateRouteRequest;
import com.auvan.backend.shared.dto.ApiResponse;
import com.auvan.backend.route.dto.RouteResponse;
import com.auvan.backend.route.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/admin/routes")
public class AdminRouteController {

    private final RouteService routeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RouteResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(routeService.listAllRoutes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(routeService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> create(@Valid @RequestBody CreateRouteRequest request) {
        RouteResponse response = routeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Route created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRouteRequest request) {
        RouteResponse response = routeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Route updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        routeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Route deleted"));
    }
}
