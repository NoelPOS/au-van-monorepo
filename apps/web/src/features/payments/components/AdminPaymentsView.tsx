import { format } from "date-fns";
import { CheckCircle2, Eye, ReceiptText, XCircle } from "lucide-react";
import { useMemo, useState } from "react";
import { useAdminPayments } from "@/features/payments/hooks/useAdminPayments";
import { useReviewPayment } from "@/features/payments/hooks/useReviewPayment";
import type { PaymentSummary } from "@/types/domain";

type PaymentTab = "pendingReview" | "all";

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatDateTime(value?: string | null) {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "-";
  return format(date, "MMM d, yyyy HH:mm");
}

function statusClass(status: string) {
  switch (status) {
    case "COMPLETED":
      return "border-emerald-200 bg-emerald-50 text-emerald-700";
    case "FAILED":
    case "REFUNDED":
      return "border-rose-200 bg-rose-50 text-rose-700";
    case "PENDING_REVIEW":
      return "border-amber-200 bg-amber-50 text-amber-700";
    default:
      return "border-slate-200 bg-slate-100 text-slate-600";
  }
}

type ReviewPanelProps = {
  payment: PaymentSummary;
  onClose: () => void;
};

function ReviewPanel({ payment, onClose }: ReviewPanelProps) {
  const reviewMutation = useReviewPayment();
  const [reviewNote, setReviewNote] = useState(payment.reviewNote || "");
  const [transactionId, setTransactionId] = useState(payment.transactionId || "");
  const [submitError, setSubmitError] = useState("");
  const isReviewable = payment.status === "PENDING_REVIEW";

  async function handleReview(status: "COMPLETED" | "FAILED") {
    setSubmitError("");

    try {
      await reviewMutation.mutateAsync({
        paymentId: payment.id,
        status,
        transactionId: transactionId || undefined,
        reviewNote: reviewNote || undefined,
      });
      onClose();
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Payment review failed");
    }
  }

  return (
    <div className="rounded-2xl border border-[#d6dcf4] bg-white p-4 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7380ba]">Payment Review</p>
          <h2 className="mt-1 text-lg font-semibold text-[#22339a]">{payment.amount ?? 0} THB</h2>
        </div>
        <button
          type="button"
          onClick={onClose}
          className="rounded-full bg-[#eef1fa] px-3 py-1.5 text-xs font-semibold text-[#6875b0]"
        >
          Close
        </button>
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
        <div className="space-y-3 rounded-xl bg-[#f7f8fd] p-3 text-sm text-[#4c5ca7]">
          <p><span className="font-semibold text-[#22339a]">Booking ID:</span> {payment.bookingId || "-"}</p>
          <p><span className="font-semibold text-[#22339a]">Payment ID:</span> {payment.id}</p>
          <p><span className="font-semibold text-[#22339a]">Method:</span> {formatLabel(payment.method)}</p>
          <p><span className="font-semibold text-[#22339a]">Status:</span> {formatLabel(payment.status)}</p>
          <p><span className="font-semibold text-[#22339a]">Reference:</span> {payment.proofReference || payment.transactionId || "-"}</p>
          <p><span className="font-semibold text-[#22339a]">Submitted:</span> {formatDateTime(payment.proofSubmittedAt || payment.createdAt)}</p>
          <p><span className="font-semibold text-[#22339a]">Paid At:</span> {formatDateTime(payment.paidAt)}</p>
        </div>

        <div className="rounded-xl border border-[#dbe1f7] bg-[#f8f9ff] p-3">
          <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">Slip Image</p>
          {payment.proofImageUrl ? (
            <img
              src={payment.proofImageUrl}
              alt="Payment proof"
              className="mt-2 w-full rounded-lg border border-[#d7dcf3] bg-white object-contain"
            />
          ) : (
            <div className="mt-2 rounded-lg border border-dashed border-[#ccd4f3] px-3 py-8 text-center text-xs text-[#6f7cb6]">
              No proof image uploaded
            </div>
          )}
        </div>
      </div>

      <div className="mt-4 grid gap-3 md:grid-cols-2">
        <div>
          <label className="mb-1 block text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">
            Transaction ID
          </label>
          <input
            value={transactionId}
            onChange={(event) => setTransactionId(event.target.value)}
            disabled={!isReviewable || reviewMutation.isPending}
            className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none disabled:cursor-not-allowed disabled:bg-[#f4f6fd]"
            placeholder="Optional banking transaction ID"
          />
        </div>

        <div>
          <label className="mb-1 block text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">
            Review Note
          </label>
          <textarea
            value={reviewNote}
            onChange={(event) => setReviewNote(event.target.value)}
            disabled={!isReviewable || reviewMutation.isPending}
            rows={4}
            className="w-full rounded-xl border border-[#d7dcf3] bg-white px-3 py-2 text-sm text-[#26368f] outline-none disabled:cursor-not-allowed disabled:bg-[#f4f6fd]"
            placeholder="Optional context for approval or rejection"
          />
        </div>
      </div>

      {submitError ? (
        <p className="mt-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          {submitError}
        </p>
      ) : null}

      {!isReviewable ? (
        <p className="mt-3 rounded-lg border border-sky-200 bg-sky-50 px-3 py-2 text-xs text-sky-700">
          This payment has already been reviewed. You can inspect the details here, but review actions are disabled.
        </p>
      ) : null}

      <div className="mt-4 flex flex-wrap justify-end gap-2">
        <button
          type="button"
          onClick={() => handleReview("FAILED")}
          disabled={!isReviewable || reviewMutation.isPending}
          className="inline-flex h-10 items-center justify-center gap-1.5 rounded-xl border border-rose-200 bg-rose-50 px-4 text-sm font-semibold text-rose-700 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-70"
        >
          <XCircle className="h-4 w-4" />
          {reviewMutation.isPending ? "Saving..." : "Reject"}
        </button>
        <button
          type="button"
          onClick={() => handleReview("COMPLETED")}
          disabled={!isReviewable || reviewMutation.isPending}
          className="inline-flex h-10 items-center justify-center gap-1.5 rounded-xl bg-[#3f53c9] px-4 text-sm font-semibold text-white transition-colors hover:bg-[#3447b4] disabled:cursor-not-allowed disabled:opacity-70"
        >
          <CheckCircle2 className="h-4 w-4" />
          {reviewMutation.isPending ? "Saving..." : "Approve"}
        </button>
      </div>
    </div>
  );
}

export function AdminPaymentsView() {
  const [page, setPage] = useState(0);
  const [activeTab, setActiveTab] = useState<PaymentTab>("pendingReview");
  const [selectedPayment, setSelectedPayment] = useState<PaymentSummary | null>(null);
  const paymentsQuery = useAdminPayments({ page, size: 20 });

  const pageData = paymentsQuery.data;
  const payments = pageData?.content ?? [];

  const visiblePayments = useMemo(() => {
    if (activeTab === "all") return payments;
    return payments.filter((payment) => payment.status === "PENDING_REVIEW");
  }, [activeTab, payments]);

  const pendingReviewCount = payments.filter((payment) => payment.status === "PENDING_REVIEW").length;

  return (
    <section>
      <header className="rounded-2xl bg-gradient-to-br from-[#4259ce] to-[#2f45b6] px-5 py-5 text-white shadow-[0_16px_30px_rgba(31,47,141,0.25)]">
        <p className="text-[11px] uppercase tracking-wide text-white/70">Admin Payments</p>
        <h1 className="mt-1 text-xl font-semibold">Review uploaded payment slips</h1>
        <p className="mt-1 text-sm text-white/80">
          This screen is intentionally tied to the Java API we have now: payment queue, proof image, note, approve or reject.
        </p>
      </header>

      <div className="mt-5 grid gap-4 lg:grid-cols-[1.15fr_0.85fr]">
        <div>
          <div className="mb-4 flex flex-wrap items-center gap-2">
            {[
              { value: "pendingReview", label: `Pending Review (${pendingReviewCount})` },
              { value: "all", label: `All on Page (${payments.length})` },
            ].map((tab) => {
              const isActive = activeTab === tab.value;

              return (
                <button
                  key={tab.value}
                  type="button"
                  onClick={() => setActiveTab(tab.value as PaymentTab)}
                  className={`rounded-full px-4 py-2 text-sm font-semibold transition-colors ${
                    isActive
                      ? "bg-[#445bd0] text-white"
                      : "bg-white text-[#6674b0] hover:bg-[#eef2fb]"
                  }`}
                >
                  {tab.label}
                </button>
              );
            })}
          </div>

          {paymentsQuery.isLoading ? (
            <div className="space-y-3">
              <div className="h-24 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
              <div className="h-24 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
            </div>
          ) : null}

          {paymentsQuery.isError ? (
            <div className="rounded-xl border border-amber-200 bg-amber-50 px-3 py-3 text-sm text-amber-700">
              Admin payments could not be loaded from the backend.
            </div>
          ) : null}

          {!paymentsQuery.isLoading && !paymentsQuery.isError && visiblePayments.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-10 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
              <ReceiptText className="mx-auto h-5 w-5 text-[#98a5da]" />
              <p className="mt-2 text-sm font-semibold text-[#3041a1]">No payments in this view</p>
              <p className="mt-1 text-xs text-[#6f7cb6]">
                Change tabs or wait for students to submit payment proof.
              </p>
            </div>
          ) : null}

          {!paymentsQuery.isLoading && !paymentsQuery.isError && visiblePayments.length > 0 ? (
            <div className="space-y-3">
              {visiblePayments.map((payment) => (
                <button
                  key={payment.id}
                  type="button"
                  onClick={() => setSelectedPayment(payment)}
                  className="w-full rounded-2xl border border-[#d6dcf4] bg-white p-4 text-left shadow-[0_8px_20px_rgba(57,85,194,0.06)] transition-colors hover:border-[#c1cbee]"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7380ba]">
                        Booking Payment
                      </p>
                      <p className="mt-1 text-sm font-semibold text-[#22339a]">
                        {payment.amount ?? 0} THB
                      </p>
                      <p className="mt-1 text-xs text-[#6f7cb6]">Booking: {payment.bookingId || "-"}</p>
                    </div>
                    <span
                      className={`rounded-full border px-2 py-1 text-[10px] font-semibold ${statusClass(
                        payment.status
                      )}`}
                    >
                      {formatLabel(payment.status)}
                    </span>
                  </div>

                  <div className="mt-3 grid gap-1 text-[11px] text-[#5564ab]">
                    <p>Method: {formatLabel(payment.method)}</p>
                    <p>Reference: {payment.proofReference || payment.transactionId || "-"}</p>
                    <p>Submitted: {formatDateTime(payment.proofSubmittedAt || payment.createdAt)}</p>
                  </div>

                  <div className="mt-3 inline-flex items-center gap-1.5 text-xs font-semibold text-[#4054c5]">
                    <Eye className="h-3.5 w-3.5" />
                    {payment.status === "PENDING_REVIEW" ? "Review payment" : "View details"}
                  </div>
                </button>
              ))}
            </div>
          ) : null}

          {!paymentsQuery.isLoading && !paymentsQuery.isError && (pageData?.totalPages ?? 0) > 1 ? (
            <div className="mt-4 flex items-center justify-between rounded-2xl border border-[#d6dcf4] bg-white px-4 py-3">
              <button
                type="button"
                onClick={() => setPage((current) => Math.max(0, current - 1))}
                disabled={page === 0}
                className="rounded-full bg-[#eef1fa] px-3 py-1.5 text-sm font-semibold text-[#6875b0] disabled:cursor-not-allowed disabled:opacity-60"
              >
                Previous
              </button>
              <p className="text-sm text-[#6f7cb6]">
                Page {page + 1} of {pageData?.totalPages ?? 1}
              </p>
              <button
                type="button"
                onClick={() =>
                  setPage((current) =>
                    Math.min((pageData?.totalPages ?? 1) - 1, current + 1)
                  )
                }
                disabled={page >= (pageData?.totalPages ?? 1) - 1}
                className="rounded-full bg-[#eef1fa] px-3 py-1.5 text-sm font-semibold text-[#6875b0] disabled:cursor-not-allowed disabled:opacity-60"
              >
                Next
              </button>
            </div>
          ) : null}
        </div>

        <div>
          {selectedPayment ? (
            <ReviewPanel payment={selectedPayment} onClose={() => setSelectedPayment(null)} />
          ) : (
            <div className="rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-12 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
              <ReceiptText className="mx-auto h-5 w-5 text-[#98a5da]" />
              <p className="mt-2 text-sm font-semibold text-[#3041a1]">Choose a payment to inspect</p>
              <p className="mt-1 text-xs text-[#6f7cb6]">
                The review panel on the right will show the proof image and approval controls.
              </p>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
