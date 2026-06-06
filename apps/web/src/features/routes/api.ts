import { apiRequest } from "@/api/client";
import type { RouteSummary } from "@/types/domain";

export type CreateRouteInput = {
  fromLocation: string;
  toLocation: string;
  price: number;
  durationMinutes?: number;
};

export type UpdateRouteInput = {
  routeId: string;
  fromLocation: string;
  toLocation: string;
  price: number;
  durationMinutes?: number;
  status: "ACTIVE" | "INACTIVE";
};

type RouteDto = {
  id: string;
  fromLocation: string;
  toLocation: string;
  slug?: string;
  price: number | string;
  durationMinutes?: number | null;
  status?: string;
};

function mapRoute(dto: RouteDto): RouteSummary {
  return {
    id: dto.id,
    from: dto.fromLocation,
    to: dto.toLocation,
    slug: dto.slug,
    price: Number(dto.price),
    durationMinutes: dto.durationMinutes,
    status: dto.status,
  };
}

export function getRoutes() {
  return apiRequest<RouteDto[]>("/api/liff/routes").then((routes) => routes.map(mapRoute));
}

export function getRouteById(routeId: string) {
  return apiRequest<RouteDto>(`/api/liff/routes/${routeId}`).then(mapRoute);
}

export function getAdminRoutes() {
  return apiRequest<RouteDto[]>("/api/admin/routes").then((routes) => routes.map(mapRoute));
}

export function createRoute(input: CreateRouteInput) {
  return apiRequest<RouteDto>("/api/admin/routes", {
    method: "POST",
    body: JSON.stringify({
      fromLocation: input.fromLocation,
      toLocation: input.toLocation,
      price: input.price,
      durationMinutes: input.durationMinutes,
    }),
  }).then(mapRoute);
}

export function updateRoute(input: UpdateRouteInput) {
  return apiRequest<RouteDto>(`/api/admin/routes/${input.routeId}`, {
    method: "PUT",
    body: JSON.stringify({
      fromLocation: input.fromLocation,
      toLocation: input.toLocation,
      price: input.price,
      durationMinutes: input.durationMinutes,
      status: input.status,
    }),
  }).then(mapRoute);
}

export function deactivateRoute(routeId: string) {
  return apiRequest<void>(`/api/admin/routes/${routeId}`, {
    method: "DELETE",
  });
}
