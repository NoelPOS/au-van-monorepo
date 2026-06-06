package com.auvan.backend.payment.service;

import com.auvan.backend.payment.dto.ReviewPaymentRequest;
import com.auvan.backend.payment.dto.SubmitPaymentProofRequest;
import com.auvan.backend.shared.dto.PageResponse;
import com.auvan.backend.payment.dto.PaymentResponse;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    List<PaymentResponse> getMyPayments(UUID userId);

    PaymentResponse getById(UUID paymentId);

    PaymentResponse submitProof(UUID bookingId, UUID userId, SubmitPaymentProofRequest request);

    PaymentResponse reviewPayment(UUID paymentId, UUID adminId, ReviewPaymentRequest request, String ip, String userAgent);

    PageResponse<PaymentResponse> listAll(int page, int size);
}
