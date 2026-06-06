import { expect, test, type Page } from "@playwright/test";

const studentSession = {
  token: "playwright-student-token",
  tokenType: "Bearer",
  expiresIn: 86400,
  user: {
    id: "student-user-id",
    email: null,
    lineUserId: "line-user-001",
    authProvider: "LINE",
    isAdmin: false,
    name: "Playwright Student",
    phone: "0812345678",
    defaultPickupLocation: "Dorm A",
  },
};

const currentBooking = {
  id: "booking-reschedule-1",
  bookingCode: "AUV-260425-OLD01",
  status: "PENDING_PAYMENT",
  passengerName: "Playwright Student",
  passengerPhone: "0812345678",
  pickupLocation: "Dorm A",
  totalPrice: 80,
  paymentDueAt: "2026-04-25T03:00:00Z",
  route: {
    id: "route-1",
    fromLocation: "ABAC",
    toLocation: "BTS Udomsuk",
    slug: "abac-bts-udomsuk",
    price: 80,
    durationMinutes: 35,
    status: "ACTIVE",
  },
  timeslot: {
    id: "timeslot-old",
    routeId: "route-1",
    date: "2026-04-25",
    time: "09:00",
    totalSeats: 12,
    bookedSeats: 1,
    availableSeats: 11,
    status: "ACTIVE",
  },
  seats: [{ id: "seat-old", seatNumber: 1, label: "A1", status: "BOOKED" }],
  payment: {
    id: "payment-1",
    status: "PENDING",
    method: "PROMPTPAY",
    amount: 80,
    transactionId: null,
    proofImageUrl: null,
    proofReference: null,
    proofSubmittedAt: null,
    paidAt: null,
    reviewNote: null,
    createdAt: "2026-04-25T01:59:00Z",
  },
};

const replacementBooking = {
  ...currentBooking,
  id: "booking-reschedule-2",
  bookingCode: "AUV-260425-NEW01",
  timeslot: {
    id: "timeslot-new",
    routeId: "route-1",
    date: "2026-04-25",
    time: "11:00",
    totalSeats: 12,
    bookedSeats: 1,
    availableSeats: 11,
    status: "ACTIVE",
  },
  seats: [{ id: "seat-new", seatNumber: 2, label: "A2", status: "BOOKED" }],
};

const rescheduleTimeslots = [
  {
    id: "timeslot-old",
    routeId: "route-1",
    routeFromLocation: "ABAC",
    routeToLocation: "BTS Udomsuk",
    date: "2026-04-25",
    time: "09:00",
    totalSeats: 12,
    bookedSeats: 1,
    availableSeats: 11,
    status: "ACTIVE",
  },
  {
    id: "timeslot-new",
    routeId: "route-1",
    routeFromLocation: "ABAC",
    routeToLocation: "BTS Udomsuk",
    date: "2026-04-25",
    time: "11:00",
    totalSeats: 12,
    bookedSeats: 1,
    availableSeats: 11,
    status: "ACTIVE",
  },
];

const rescheduleSeatMap = [
  { id: "seat-new", seatNumber: 2, label: "A2", status: "AVAILABLE" },
  { id: "seat-booked", seatNumber: 3, label: "A3", status: "BOOKED" },
  { id: "seat-free-1", seatNumber: 4, label: "B1", status: "AVAILABLE" },
  { id: "seat-free-2", seatNumber: 5, label: "B2", status: "AVAILABLE" },
];

async function seedStudentSession(page: Page) {
  await page.addInitScript((session) => {
    window.localStorage.setItem("auvan_auth_session", JSON.stringify(session));
    window.localStorage.setItem("auvan_access_token", session.token);
  }, studentSession);
}

async function mockRescheduleFlow(page: Page) {
  await page.route("**/api/liff/notifications/unread-count", async (routeRequest) => {
    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: { unread: 0 },
      }),
    });
  });

  await page.route("**/api/liff/bookings/booking-reschedule-1", async (routeRequest) => {
    if (routeRequest.request().method() !== "GET") {
      await routeRequest.fallback();
      return;
    }

    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: currentBooking,
      }),
    });
  });

  await page.route("**/api/liff/bookings/booking-reschedule-2", async (routeRequest) => {
    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: replacementBooking,
      }),
    });
  });

  await page.route("**/api/liff/timeslots**", async (routeRequest) => {
    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: rescheduleTimeslots,
      }),
    });
  });

  await page.route("**/api/liff/seats**", async (routeRequest) => {
    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: rescheduleSeatMap,
      }),
    });
  });

  await page.route("**/api/liff/bookings/booking-reschedule-1/reschedule", async (routeRequest) => {
    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: replacementBooking,
      }),
    });
  });
}

test("student can reschedule from booking details", async ({ page }) => {
  await seedStudentSession(page);
  await mockRescheduleFlow(page);

  await page.goto("/my-bookings/booking-reschedule-1");

  await expect(page.getByRole("heading", { name: "AUV-260425-OLD01" })).toBeVisible();
  await page.getByRole("button", { name: "Show" }).click();
  await expect(page.getByText("Choose a new date, timeslot, and the same number of seats.")).toBeVisible();
  await page.getByRole("button", { name: "A2" }).click();
  await page.getByRole("button", { name: "Confirm Reschedule" }).click();

  await expect(page).toHaveURL(/\/my-bookings\/booking-reschedule-2$/);
  await expect(page.getByRole("heading", { name: "AUV-260425-NEW01" })).toBeVisible();
  await expect(page.getByText("11:00", { exact: true })).toBeVisible();
});
