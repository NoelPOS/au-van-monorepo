import { apiRequest } from "@/api/client";
import type { PageResponse } from "@/types/api";
import type { PaymentSummary } from "@/types/domain";

export type SubmitPaymentProofInput = {
  bookingId: string;
  proofImageUrl: string;
  proofReference: string;
  paidAt?: string;
};

export type AdminPaymentsQuery = {
  page?: number;
  size?: number;
};

export type ReviewPaymentInput = {
  paymentId: string;
  status: "COMPLETED" | "FAILED" | "REFUNDED";
  transactionId?: string;
  reviewNote?: string;
};

type PaymentDto = {
  id: string;
  bookingId: string;
  amount: number | string;
  method: "CASH" | "PROMPTPAY" | "BANK_TRANSFER";
  status: string;
  transactionId?: string | null;
  proofImageUrl?: string | null;
  proofReference?: string | null;
  proofSubmittedAt?: string | null;
  reviewedBy?: string | null;
  reviewedAt?: string | null;
  reviewNote?: string | null;
  paidAt?: string | null;
  refundedAt?: string | null;
  createdAt?: string | null;
};

function mapPayment(dto: PaymentDto): PaymentSummary {
  return {
    id: dto.id,
    bookingId: dto.bookingId,
    status: dto.status,
    method: dto.method,
    amount: Number(dto.amount),
    transactionId: dto.transactionId,
    proofImageUrl: dto.proofImageUrl,
    proofReference: dto.proofReference,
    proofSubmittedAt: dto.proofSubmittedAt,
    paidAt: dto.paidAt,
    reviewNote: dto.reviewNote,
    createdAt: dto.createdAt,
  };
}

export function submitPaymentProof(input: SubmitPaymentProofInput) {
  return apiRequest<PaymentDto>(`/api/liff/bookings/${input.bookingId}/payment-proof`, {
    method: "POST",
    body: JSON.stringify({
      proofImageUrl: input.proofImageUrl,
      proofReference: input.proofReference,
      paidAt: input.paidAt,
    }),
  }).then(mapPayment);
}

export function getAdminPayments(query: AdminPaymentsQuery = {}) {
  return apiRequest<PageResponse<PaymentDto>>("/api/admin/payments", {
    query: {
      page: query.page ?? 0,
      size: query.size ?? 20,
    },
  }).then((page) => ({
    ...page,
    content: page.content.map(mapPayment),
  }));
}

export function reviewPayment(input: ReviewPaymentInput) {
  return apiRequest<PaymentDto>(`/api/admin/payments/${input.paymentId}/review`, {
    method: "PUT",
    body: JSON.stringify({
      status: input.status,
      transactionId: input.transactionId,
      reviewNote: input.reviewNote,
    }),
  }).then(mapPayment);
}
