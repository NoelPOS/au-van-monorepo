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

const activeBooking = {
  id: "booking-cancel-1",
  bookingCode: "AUV-260425-CAN01",
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
    id: "timeslot-1",
    routeId: "route-1",
    date: "2026-04-25",
    time: "09:00",
    totalSeats: 12,
    bookedSeats: 1,
    availableSeats: 11,
    status: "ACTIVE",
  },
  seats: [{ id: "seat-1", seatNumber: 1, label: "A1", status: "BOOKED" }],
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

const cancelledBooking = {
  ...activeBooking,
  status: "CANCELLED",
};

async function seedStudentSession(page: Page) {
  await page.addInitScript((session) => {
    window.localStorage.setItem("auvan_auth_session", JSON.stringify(session));
    window.localStorage.setItem("auvan_access_token", session.token);
  }, studentSession);
}

async function mockCancellationFlow(page: Page) {
  let cancelled = false;

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

  await page.route("**/api/liff/bookings/booking-cancel-1", async (routeRequest) => {
    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: cancelled ? cancelledBooking : activeBooking,
      }),
    });
  });

  await page.route("**/api/liff/bookings", async (routeRequest) => {
    if (routeRequest.request().method() !== "GET") {
      await routeRequest.fallback();
      return;
    }

    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: [cancelled ? cancelledBooking : activeBooking],
      }),
    });
  });

  await page.route("**/api/liff/bookings/booking-cancel-1", async (routeRequest) => {
    if (routeRequest.request().method() !== "DELETE") {
      await routeRequest.fallback();
      return;
    }

    cancelled = true;

    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: null,
      }),
    });
  });
}

test("student can cancel an active booking from booking details", async ({ page }) => {
  await seedStudentSession(page);
  await mockCancellationFlow(page);

  await page.goto("/my-bookings/booking-cancel-1");

  await expect(page.getByRole("heading", { name: "AUV-260425-CAN01" })).toBeVisible();
  await expect(page.getByText("Pending Payment")).toBeVisible();
  await page.getByRole("button", { name: "Cancel Booking" }).click();

  await expect(page.getByText("Booking cancelled.")).toBeVisible();
  await expect(page.getByText("Cancelled", { exact: true })).toBeVisible();
});
