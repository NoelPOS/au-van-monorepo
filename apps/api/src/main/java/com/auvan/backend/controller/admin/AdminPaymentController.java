package com.auvan.backend.controller.admin;

import com.auvan.backend.dto.request.ReviewPaymentRequest;
import com.auvan.backend.dto.response.ApiResponse;
import com.auvan.backend.dto.response.PageResponse;
import com.auvan.backend.dto.response.PaymentResponse;
import com.auvan.backend.exception.UnauthorizedException;
import com.auvan.backend.security.CustomUserDetails;
import com.auvan.backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.listAll(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getById(id)));
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<ApiResponse<PaymentResponse>> review(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody ReviewPaymentRequest request,
            HttpServletRequest httpRequest) {
        PaymentResponse response = paymentService.reviewPayment(
                id,
                currentUserId(principal),
                request,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Payment review completed"));
    }

    private UUID currentUserId(CustomUserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal.getUserId();
    }
}
