package com.auvan.backend.route;

import com.auvan.backend.route.Route;
import com.auvan.backend.route.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {

    List<Route> findByStatusOrderByFromLocationAsc(RouteStatus status);

    Optional<Route> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
