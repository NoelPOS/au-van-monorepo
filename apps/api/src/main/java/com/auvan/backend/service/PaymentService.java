package com.auvan.backend.service;

import com.auvan.backend.dto.request.ReviewPaymentRequest;
import com.auvan.backend.dto.request.SubmitPaymentProofRequest;
import com.auvan.backend.dto.response.PageResponse;
import com.auvan.backend.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    List<PaymentResponse> getMyPayments(UUID userId);

    PaymentResponse getById(UUID paymentId);

    PaymentResponse submitProof(UUID bookingId, UUID userId, SubmitPaymentProofRequest request);

    PaymentResponse reviewPayment(UUID paymentId, UUID adminId, ReviewPaymentRequest request, String ip, String userAgent);

    PageResponse<PaymentResponse> listAll(int page, int size);
}
