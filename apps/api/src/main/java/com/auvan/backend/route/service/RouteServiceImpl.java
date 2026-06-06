package com.auvan.backend.route.service;

import com.auvan.backend.shared.mapper.EntityMappers;
import com.auvan.backend.route.dto.CreateRouteRequest;
import com.auvan.backend.route.dto.UpdateRouteRequest;
import com.auvan.backend.route.dto.RouteResponse;
import com.auvan.backend.route.entity.Route;
import com.auvan.backend.route.enums.RouteStatus;
import com.auvan.backend.shared.exception.ConflictException;
import com.auvan.backend.shared.exception.ResourceNotFoundException;
import com.auvan.backend.route.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final EntityMappers mappers;

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> listActiveRoutes() {
        return mappers.toRouteList(
                routeRepository.findByStatusOrderByFromLocationAsc(RouteStatus.ACTIVE));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> listAllRoutes() {
        return mappers.toRouteList(routeRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getById(UUID id) {
        return mappers.toRoute(findOrThrow(id));
    }

    @Override
    @Transactional
    public RouteResponse create(CreateRouteRequest request) {
        String slug = generateSlug(request.fromLocation(), request.toLocation());
        if (routeRepository.existsBySlug(slug)) {
            throw new ConflictException("A route between these locations already exists", "ROUTE_EXISTS");
        }

        Route route = new Route();
        route.setFromLocation(request.fromLocation());
        route.setToLocation(request.toLocation());
        route.setSlug(slug);
        route.setPrice(request.price());
        route.setDurationMinutes(request.durationMinutes());
        route.setStatus(RouteStatus.ACTIVE);

        return mappers.toRoute(routeRepository.save(route));
    }

    @Override
    @Transactional
    public RouteResponse update(UUID id, UpdateRouteRequest request) {
        Route route = findOrThrow(id);

        if (StringUtils.hasText(request.fromLocation())) route.setFromLocation(request.fromLocation());
        if (StringUtils.hasText(request.toLocation()))   route.setToLocation(request.toLocation());
        if (request.price() != null)                     route.setPrice(request.price());
        if (request.durationMinutes() != null)           route.setDurationMinutes(request.durationMinutes());
        if (request.status() != null)                    route.setStatus(request.status());

        // Re-generate slug if either location changed
        if (StringUtils.hasText(request.fromLocation()) || StringUtils.hasText(request.toLocation())) {
            route.setSlug(generateSlug(route.getFromLocation(), route.getToLocation()));
        }

        return mappers.toRoute(routeRepository.save(route));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Route route = findOrThrow(id);
        route.setStatus(RouteStatus.INACTIVE);
        routeRepository.save(route);
    }

    private Route findOrThrow(UUID id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Route", id));
    }

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    private String generateSlug(String from, String to) {
        String normalized = Normalizer.normalize(from + "-" + to, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT);
        return NON_ALPHANUMERIC.matcher(normalized).replaceAll("-").replaceAll("-+", "-").replaceAll("^-|-$", "");
    }
}
