import { format } from "date-fns";
import { CalendarDays, Clock3, CreditCard, ImagePlus, MapPin, ReceiptText } from "lucide-react";
import { ChangeEvent, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { LiffPageHeader } from "@/components/layout/LiffPageHeader";
import { useBooking } from "@/features/bookings/hooks/useBooking";
import { useSubmitPaymentProof } from "@/features/payments/hooks/useSubmitPaymentProof";
import { env } from "@/lib/env";

type PaymentProofViewProps = {
  bookingId: string;
};

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function readFileAsDataUrl(file: File) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = () => {
      if (typeof reader.result === "string") {
        resolve(reader.result);
        return;
      }

      reject(new Error("Image could not be read"));
    };

    reader.onerror = () => reject(new Error("Image could not be read"));
    reader.readAsDataURL(file);
  });
}

export function PaymentProofView({ bookingId }: PaymentProofViewProps) {
  const navigate = useNavigate();
  const bookingQuery = useBooking(bookingId);
  const submitProof = useSubmitPaymentProof();

  const [proofReference, setProofReference] = useState("");
  const [paidAt, setPaidAt] = useState("");
  const [proofImage, setProofImage] = useState<File | null>(null);
  const [proofPreviewUrl, setProofPreviewUrl] = useState("");
  const [submitError, setSubmitError] = useState("");

  const booking = bookingQuery.data;
  const payment = booking?.payment ?? null;

  const canSubmitProof = payment?.status === "PENDING" && payment.method !== "CASH";
  const paymentInstructions = useMemo(
    () =>
      [
        `Transfer exactly ${booking?.totalPrice ?? 0} THB.`,
        "Use the transaction reference from your banking app.",
        "Upload the payment slip so admin can review it.",
      ],
    [booking?.totalPrice]
  );

  async function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const nextFile = event.target.files?.[0] || null;
    setProofImage(nextFile);

    if (!nextFile) {
      setProofPreviewUrl("");
      return;
    }

    try {
      const dataUrl = await readFileAsDataUrl(nextFile);
      setProofPreviewUrl(dataUrl);
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Image preview failed");
      setProofPreviewUrl("");
    }
  }

  async function handleSubmit() {
    if (!proofImage || !proofReference || !booking || !canSubmitProof) return;

    setSubmitError("");

    try {
      const proofImageUrl = await readFileAsDataUrl(proofImage);

      await submitProof.mutateAsync({
        bookingId: booking.id,
        proofImageUrl,
        proofReference,
        paidAt: paidAt ? new Date(paidAt).toISOString() : undefined,
      });

      navigate(appRoutes.myBookings);
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Payment proof submission failed");
    }
  }

  if (bookingQuery.isLoading) {
    return (
      <div className="px-4 py-8">
        <div className="h-24 animate-pulse rounded-xl border border-[#d6dcf4] bg-white" />
      </div>
    );
  }

  if (bookingQuery.isError || !booking) {
    return (
      <div className="px-4 py-8">
        <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          Booking payment details could not be loaded.
        </p>
      </div>
    );
  }

  return (
    <div className="px-4 pb-6 pt-3">
      <LiffPageHeader
        title="Pay and Upload Proof"
        subtitle="Complete payment and submit slip for admin review"
        showBack
        backHref={appRoutes.myBookings}
      />

      <section className="mt-4 rounded-2xl border border-[#d6dcf4] bg-white p-3 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7380ba]">
              {booking.bookingCode}
            </p>
            <p className="mt-1 text-sm font-semibold text-[#2f3f9f]">
              {booking.route.from} - {booking.route.to}
            </p>
          </div>
          <p className="text-sm font-semibold text-[#3041a1]">{booking.totalPrice} THB</p>
        </div>

        <div className="mt-3 grid gap-2 text-[11px] text-[#6f7cb6]">
          <p className="inline-flex items-center gap-1.5">
            <CalendarDays className="h-3.5 w-3.5 text-[#8f9bd7]" />
            {format(new Date(booking.timeslot.date), "EEE, MMM d")}
          </p>
          <p className="inline-flex items-center gap-1.5">
            <Clock3 className="h-3.5 w-3.5 text-[#8f9bd7]" />
            {booking.timeslot.time}
          </p>
          <p className="inline-flex items-center gap-1.5">
            <MapPin className="h-3.5 w-3.5 text-[#8f9bd7]" />
            {booking.pickupLocation}
          </p>
        </div>

        {payment ? (
          <div className="mt-3 rounded-xl bg-[#f7f8fd] px-3 py-2">
            <p className="inline-flex items-center gap-1.5 text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">
              <CreditCard className="h-3.5 w-3.5" />
              Payment Status
            </p>
            <p className="mt-1 text-[11px] text-[#5564ab]">Method: {formatLabel(payment.method)}</p>
            <p className="mt-1 text-[11px] font-semibold text-[#3041a1]">
              Status: {formatLabel(payment.status)}
            </p>
            {booking.paymentDueAt ? (
              <p className="mt-1 text-[10px] text-[#7b87be]">
                Due by {format(new Date(booking.paymentDueAt), "MMM d, HH:mm")}
              </p>
            ) : null}
          </div>
        ) : null}
      </section>

      <section className="mt-4 rounded-2xl border border-[#d6dcf4] bg-white p-3 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
        <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">How to pay</p>
        <ol className="mt-2 list-decimal space-y-1 pl-4 text-[11px] text-[#3041a1]">
          {paymentInstructions.map((instruction) => (
            <li key={instruction}>{instruction}</li>
          ))}
        </ol>

        {env.paymentQrImageUrl ? (
          <div className="mt-3 rounded-xl border border-[#dbe1f7] bg-[#f8f9ff] p-3">
            <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">QR Code</p>
            <img
              src={env.paymentQrImageUrl}
              alt="Payment QR code"
              className="mx-auto mt-2 w-full max-w-[240px] rounded-lg border border-[#d7dcf3] bg-white object-contain"
            />
          </div>
        ) : null}

        {env.paymentAccountName || env.paymentAccountNumber || env.paymentBankName ? (
          <div className="mt-3 rounded-xl border border-[#dbe1f7] bg-[#f8f9ff] p-3 text-[11px] text-[#3041a1]">
            {env.paymentAccountName ? <p>Account Name: {env.paymentAccountName}</p> : null}
            {env.paymentAccountNumber ? <p>Account Number: {env.paymentAccountNumber}</p> : null}
            {env.paymentBankName ? <p>Bank: {env.paymentBankName}</p> : null}
          </div>
        ) : null}
      </section>

      {!canSubmitProof ? (
        <div className="mt-4 rounded-xl border border-amber-200 bg-amber-50 px-3 py-3 text-xs text-amber-700">
          This booking is not waiting for proof upload right now. If payment is already under review or completed,
          you can return to My Bookings.
        </div>
      ) : null}

      <section className="mt-4 rounded-2xl border border-[#d6dcf4] bg-white p-3 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
        <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">
          Submit payment proof
        </p>

        <div className="mt-3 space-y-3">
          <div>
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
              Transfer Reference
            </label>
            <input
              value={proofReference}
              onChange={(event) => setProofReference(event.target.value)}
              className="h-9 w-full rounded-md border border-[#d7dcf3] bg-white px-3 text-xs text-[#26368f] outline-none"
              placeholder="e.g. KPLUS-123456"
            />
          </div>

          <div>
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
              Paid At (optional)
            </label>
            <input
              type="datetime-local"
              value={paidAt}
              onChange={(event) => setPaidAt(event.target.value)}
              className="h-9 w-full rounded-md border border-[#d7dcf3] bg-white px-3 text-xs text-[#26368f] outline-none"
            />
          </div>

          <div>
            <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
              Slip Image (PNG, JPG, WEBP)
            </label>
            <label className="flex min-h-[120px] cursor-pointer flex-col items-center justify-center rounded-xl border border-dashed border-[#cdd5f3] bg-[#fafbff] px-4 py-4 text-center">
              <ImagePlus className="h-5 w-5 text-[#7f8cd0]" />
              <span className="mt-2 text-xs font-semibold text-[#3041a1]">
                {proofImage ? proofImage.name : "Choose a payment slip image"}
              </span>
              <span className="mt-1 text-[11px] text-[#6f7cb6]">
                Choose a clear image of the transferred slip.
              </span>
              <input
                type="file"
                accept="image/png,image/jpeg,image/webp"
                className="hidden"
                onChange={handleFileChange}
              />
            </label>

            {proofPreviewUrl ? (
              <div className="mt-3 rounded-xl border border-[#dbe1f7] bg-[#f8f9ff] p-2">
                <img
                  src={proofPreviewUrl}
                  alt="Payment slip preview"
                  className="w-full rounded-lg border border-[#d7dcf3] bg-white object-cover"
                />
              </div>
            ) : null}
          </div>
        </div>

        {submitError ? (
          <p className="mt-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
            {submitError}
          </p>
        ) : null}

        {payment?.proofReference ? (
          <div className="mt-3 rounded-xl border border-[#dbe1f7] bg-[#f8f9ff] px-3 py-2 text-[11px] text-[#3041a1]">
            <p className="inline-flex items-center gap-1.5 font-semibold">
              <ReceiptText className="h-3.5 w-3.5" />
              Existing proof on record
            </p>
            <p className="mt-1">Reference: {payment.proofReference}</p>
          </div>
        ) : null}

        <button
          type="button"
          onClick={handleSubmit}
          disabled={!canSubmitProof || !proofReference || !proofImage || submitProof.isPending}
          className="mt-4 h-10 w-full rounded-xl bg-[#3f53c9] text-sm font-semibold text-white transition-colors hover:bg-[#3447b4] disabled:cursor-not-allowed disabled:opacity-70"
        >
          {submitProof.isPending ? "Submitting..." : "Submit for Review"}
        </button>
      </section>
    </div>
  );
}
