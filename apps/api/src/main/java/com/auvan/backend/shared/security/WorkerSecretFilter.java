package com.auvan.backend.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Guards {@code /api/internal/**} endpoints.
 * Requests must carry the {@code X-Worker-Secret} header matching the configured secret.
 * This filter runs before JWT processing for the internal path; non-internal paths are
 * passed through immediately.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerSecretFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME    = "X-Worker-Secret";
    private static final String INTERNAL_PATH  = "/api/internal/";

    @Value("${internal.worker.secret}")
    private String workerSecret;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getRequestURI().startsWith(INTERNAL_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        String provided = request.getHeader(HEADER_NAME);

        if (!StringUtils.hasText(provided) || !provided.equals(workerSecret)) {
            log.warn("Rejected internal request from {} — invalid worker secret",
                    request.getRemoteAddr());
            sendUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                Map.of("success", false, "error", "Unauthorized: invalid worker secret"));
    }
}
