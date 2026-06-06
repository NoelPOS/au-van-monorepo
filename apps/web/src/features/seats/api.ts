import { apiRequest } from "@/api/client";
import type { SeatSummary } from "@/types/domain";

type SeatDto = {
  id: string;
  seatNumber: number;
  label: string;
  status: string;
};

function mapSeat(dto: SeatDto): SeatSummary {
  return {
    id: dto.id,
    seatNumber: dto.seatNumber,
    label: dto.label,
    status: dto.status,
  };
}

export function getSeatMap(timeslotId: string) {
  return apiRequest<SeatDto[]>("/api/liff/seats", {
    query: { timeslotId },
  }).then((seats) => seats.map(mapSeat));
}

