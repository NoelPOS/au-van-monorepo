import { apiRequest } from "@/api/client";
import type { TimeslotSummary } from "@/types/domain";

type GetTimeslotsParams = {
  routeId: string;
  date: string;
};

export type AdminTimeslotSummary = TimeslotSummary & {
  routeId: string;
  routeFrom: string;
  routeTo: string;
};

export type CreateAdminTimeslotInput = {
  routeId: string;
  date: string;
  time: string;
  totalSeats: number;
};

export type BulkCreateAdminTimeslotsInput = {
  routeId: string;
  dateFrom: string;
  dateTo: string;
  daysOfWeek: number[];
  times: string[];
  totalSeats: number;
};

export type UpdateAdminTimeslotInput = {
  timeslotId: string;
  date?: string;
  time?: string;
  status?: "ACTIVE" | "CANCELLED";
};

type TimeslotDto = {
  id: string;
  routeId: string;
  routeFromLocation: string;
  routeToLocation: string;
  date: string;
  time: string;
  totalSeats: number;
  bookedSeats: number;
  availableSeats: number;
  status?: string;
};

function mapTimeslot(dto: TimeslotDto): TimeslotSummary {
  return {
    id: dto.id,
    date: dto.date,
    time: dto.time,
    totalSeats: dto.totalSeats,
    bookedSeats: dto.bookedSeats,
    availableSeats: dto.availableSeats,
    status: dto.status,
  };
}

function mapAdminTimeslot(dto: TimeslotDto): AdminTimeslotSummary {
  return {
    id: dto.id,
    routeId: dto.routeId,
    routeFrom: dto.routeFromLocation,
    routeTo: dto.routeToLocation,
    date: dto.date,
    time: dto.time,
    totalSeats: dto.totalSeats,
    bookedSeats: dto.bookedSeats,
    availableSeats: dto.availableSeats,
    status: dto.status,
  };
}

export function getTimeslots(params: GetTimeslotsParams) {
  return apiRequest<TimeslotDto[]>("/api/liff/timeslots", {
    query: {
      routeId: params.routeId,
      fromDate: params.date,
    },
  }).then((timeslots) => timeslots.map(mapTimeslot));
}

export function getAdminTimeslots(page = 0, size = 50) {
  return apiRequest<TimeslotDto[]>("/api/admin/timeslots", {
    query: {
      page,
      size,
    },
  }).then((timeslots) => timeslots.map(mapAdminTimeslot));
}

export function createAdminTimeslot(input: CreateAdminTimeslotInput) {
  return apiRequest<TimeslotDto>("/api/admin/timeslots", {
    method: "POST",
    body: JSON.stringify({
      routeId: input.routeId,
      date: input.date,
      time: input.time,
      totalSeats: input.totalSeats,
    }),
  }).then(mapAdminTimeslot);
}

export function bulkCreateAdminTimeslots(input: BulkCreateAdminTimeslotsInput) {
  return apiRequest<TimeslotDto[]>("/api/admin/timeslots/bulk", {
    method: "POST",
    body: JSON.stringify({
      routeId: input.routeId,
      dateFrom: input.dateFrom,
      dateTo: input.dateTo,
      daysOfWeek: input.daysOfWeek,
      times: input.times,
      totalSeats: input.totalSeats,
    }),
  }).then((timeslots) => timeslots.map(mapAdminTimeslot));
}

export function updateAdminTimeslot(input: UpdateAdminTimeslotInput) {
  return apiRequest<TimeslotDto>(`/api/admin/timeslots/${input.timeslotId}`, {
    method: "PUT",
    body: JSON.stringify({
      date: input.date,
      time: input.time,
      status: input.status,
    }),
  }).then(mapAdminTimeslot);
}

export function cancelAdminTimeslot(timeslotId: string) {
  return apiRequest<void>(`/api/admin/timeslots/${timeslotId}`, {
    method: "DELETE",
  });
}
