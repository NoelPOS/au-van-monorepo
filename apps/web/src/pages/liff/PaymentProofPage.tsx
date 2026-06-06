import { useParams } from "react-router-dom";
import { PaymentProofView } from "@/features/payments/components/PaymentProofView";

export function PaymentProofPage() {
  const { bookingId = "" } = useParams();

  return <PaymentProofView bookingId={bookingId} />;
}
