import { useParams } from "react-router-dom";
import { BookRouteView } from "@/features/bookings/components/BookRouteView";

export function BookRoutePage() {
  const { routeId = "" } = useParams();
  return <BookRouteView routeId={routeId} />;
}
