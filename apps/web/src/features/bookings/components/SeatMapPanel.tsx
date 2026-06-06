import type { SeatSummary } from "@/types/domain";
import { SeatLegend } from "@/features/bookings/components/SeatLegend";

type SeatMapPanelProps = {
  seats: SeatSummary[];
  selectedSeatIds: string[];
  onSelectionChange: (seatIds: string[]) => void;
  maxSeats?: number;
  disabled?: boolean;
};

function chunkSeats(seats: SeatSummary[], size: number) {
  const rows: SeatSummary[][] = [];

  for (let index = 0; index < seats.length; index += size) {
    rows.push(seats.slice(index, index + size));
  }

  return rows;
}

function getSeatClasses(seat: SeatSummary, isSelected: boolean) {
  if (isSelected) {
    return "border-[#3145b8] bg-[#3f53c9] text-white";
  }

  if (seat.status === "BOOKED") {
    return "border-[#cdd5f8] bg-[#e6ebff] text-[#6b78b6]";
  }

  if (seat.status === "LOCKED") {
    return "border-[#f2cc75] bg-[#fff3dc] text-[#9a7216]";
  }

  return "border-[#ccd5fb] bg-[#f4f7ff] text-[#3145b8] hover:border-[#aebcf7]";
}

export function SeatMapPanel({
  seats,
  selectedSeatIds,
  onSelectionChange,
  maxSeats = 4,
  disabled = false,
}: SeatMapPanelProps) {
  const rows = chunkSeats(seats, 4);
  const availableSeats = seats.filter((seat) => seat.status === "AVAILABLE").length;
  const lockedSeats = seats.filter((seat) => seat.status === "LOCKED").length;
  const bookedSeats = seats.filter((seat) => seat.status === "BOOKED").length;

  function toggleSeat(seatId: string) {
    if (disabled) return;

    const seat = seats.find((item) => item.id === seatId);
    if (!seat || seat.status !== "AVAILABLE") return;

    if (selectedSeatIds.includes(seatId)) {
      onSelectionChange(selectedSeatIds.filter((id) => id !== seatId));
      return;
    }

    if (selectedSeatIds.length < maxSeats) {
      onSelectionChange([...selectedSeatIds, seatId]);
    }
  }

  return (
    <div className="flex w-full flex-col items-center gap-3">
      <div className="relative w-full rounded-xl border border-[#d6dcf4] bg-[#fbfcff] p-3.5">
        <div className="mb-3 flex items-center justify-between text-[11px]">
          <span className="rounded bg-[#e9edfd] px-2 py-0.5 font-semibold text-[#3f53c9]">
            Choose Seat
          </span>
          <span className="text-[#6f7cb6]">Pick up to {maxSeats}</span>
        </div>

        <div className="mb-3 rounded-lg border border-[#d9e0f8] bg-white px-3 py-1.5 text-center text-[10px] font-semibold text-[#5262ad]">
          Front (Driver)
        </div>

        <div className="flex flex-col gap-2.5">
          {rows.map((row, rowIndex) => (
            <div key={`${rowIndex}-${row.length}`} className="flex items-center justify-center gap-3">
              <div className="flex gap-2">
                {row.slice(0, 2).map((seat) => {
                  const isSelected = selectedSeatIds.includes(seat.id);

                  return (
                    <button
                      key={seat.id}
                      type="button"
                      onClick={() => toggleSeat(seat.id)}
                      disabled={disabled || seat.status !== "AVAILABLE"}
                      className={`flex h-11 w-11 items-center justify-center rounded-lg border text-[11px] font-semibold transition-colors ${getSeatClasses(
                        seat,
                        isSelected
                      )}`}
                    >
                      {seat.label}
                    </button>
                  );
                })}
              </div>

              <div className="h-[2px] w-7 rounded-full bg-[#d6dcf4]" />

              <div className="flex gap-2">
                {row.slice(2, 4).map((seat) => {
                  const isSelected = selectedSeatIds.includes(seat.id);

                  return (
                    <button
                      key={seat.id}
                      type="button"
                      onClick={() => toggleSeat(seat.id)}
                      disabled={disabled || seat.status !== "AVAILABLE"}
                      className={`flex h-11 w-11 items-center justify-center rounded-lg border text-[11px] font-semibold transition-colors ${getSeatClasses(
                        seat,
                        isSelected
                      )}`}
                    >
                      {seat.label}
                    </button>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="grid w-full grid-cols-3 gap-2 text-center">
        <div className="rounded-lg border border-[#d8dff8] bg-[#f4f7ff] px-2 py-1.5">
          <p className="text-[11px] font-semibold text-[#3145b8]">{availableSeats}</p>
          <p className="text-[10px] text-[#6f7cb6]">Available</p>
        </div>
        <div className="rounded-lg border border-[#f2dca4] bg-[#fff6e3] px-2 py-1.5">
          <p className="text-[11px] font-semibold text-[#ac7d0c]">{lockedSeats}</p>
          <p className="text-[10px] text-[#8f7328]">Locked</p>
        </div>
        <div className="rounded-lg border border-[#d8dff8] bg-[#e9edff] px-2 py-1.5">
          <p className="text-[11px] font-semibold text-[#6b78b6]">{bookedSeats}</p>
          <p className="text-[10px] text-[#6f7cb6]">Booked</p>
        </div>
      </div>

      <SeatLegend />

      <p className="text-xs font-semibold text-[#3041a1]">
        {selectedSeatIds.length}
        <span className="font-normal text-[#6f7cb6]">/{maxSeats} seats selected</span>
      </p>
    </div>
  );
}

