package com.auvan.backend.payment.controller;

import com.auvan.backend.payment.dto.ReviewPaymentRequest;
import com.auvan.backend.payment.dto.PaymentResponse;
import com.auvan.backend.user.entity.User;
import com.auvan.backend.payment.enums.PaymentMethod;
import com.auvan.backend.payment.enums.PaymentStatus;
import com.auvan.backend.shared.security.CustomUserDetails;
import com.auvan.backend.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPaymentControllerTest {

    @Mock private PaymentService paymentService;
    @Mock private HttpServletRequest httpServletRequest;

    @InjectMocks private AdminPaymentController controller;

    @Test
    void reviewPaymentReturnsOk() {
        UUID paymentId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        CustomUserDetails principal = principal(adminId, true);

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("JUnit");
        when(paymentService.reviewPayment(eq(paymentId), eq(adminId), any(ReviewPaymentRequest.class), eq("127.0.0.1"), eq("JUnit")))
                .thenReturn(new PaymentResponse(
                        paymentId,
                        UUID.randomUUID(),
                        BigDecimal.valueOf(80),
                        PaymentMethod.PROMPTPAY,
                        PaymentStatus.COMPLETED,
                        "TX123",
                        null,
                        null,
                        null,
                        adminId,
                        Instant.now(),
                        "Approved",
                        Instant.now(),
                        null,
                        Instant.now()
                ));

        var response = controller.review(
                principal,
                paymentId,
                new ReviewPaymentRequest(PaymentStatus.COMPLETED, "TX123", "Approved"),
                httpServletRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().status()).isEqualTo(PaymentStatus.COMPLETED);
    }

    private CustomUserDetails principal(UUID userId, boolean admin) {
        User user = new User();
        user.setId(userId);
        user.setName("Admin");
        user.setAdmin(admin);
        return new CustomUserDetails(user);
    }
}
