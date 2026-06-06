import { useParams } from "react-router-dom";
import { BookingDetailsView } from "@/features/bookings/components/BookingDetailsView";

export function BookingDetailsPage() {
  const { bookingId = "" } = useParams();

  return <BookingDetailsView bookingId={bookingId} />;
}
