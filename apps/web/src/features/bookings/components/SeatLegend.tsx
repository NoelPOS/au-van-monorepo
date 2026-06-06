const legendItems = [
  { label: "Available", className: "border-[#ccd5fb] bg-[#f4f7ff]" },
  { label: "Selected", className: "border-[#3145b8] bg-[#3f53c9]" },
  { label: "Locked", className: "border-[#f2cc75] bg-[#fff3dc]" },
  { label: "Booked", className: "border-[#cdd5f8] bg-[#e6ebff]" },
];

export function SeatLegend() {
  return (
    <div className="flex flex-wrap items-center justify-center gap-3 text-[11px] text-[#5f6eb2]">
      {legendItems.map((item) => (
        <div key={item.label} className="flex items-center gap-1.5">
          <div className={`h-4 w-4 rounded border ${item.className}`} />
          <span>{item.label}</span>
        </div>
      ))}
    </div>
  );
}

