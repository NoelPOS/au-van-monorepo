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

const route = {
  id: "route-1",
  fromLocation: "ABAC",
  toLocation: "BTS Udomsuk",
  slug: "abac-bts-udomsuk",
  price: 80,
  durationMinutes: 35,
  status: "ACTIVE",
};

const timeslot = {
  id: "timeslot-1",
  routeId: "route-1",
  routeFromLocation: "ABAC",
  routeToLocation: "BTS Udomsuk",
  date: "2026-04-25",
  time: "09:00",
  totalSeats: 12,
  bookedSeats: 1,
  availableSeats: 11,
  status: "ACTIVE",
};

const seats = [
  { id: "seat-1", seatNumber: 1, label: "A1", status: "AVAILABLE" },
  { id: "seat-2", seatNumber: 2, label: "A2", status: "BOOKED" },
  { id: "seat-3", seatNumber: 3, label: "B1", status: "AVAILABLE" },
  { id: "seat-4", seatNumber: 4, label: "B2", status: "AVAILABLE" },
];

const createdBooking = {
  id: "booking-1",
  bookingCode: "AUV-260425-ABCDE",
  status: "CONFIRMED",
  passengerName: "Playwright Student",
  passengerPhone: "0812345678",
  pickupLocation: "Dorm A",
  totalPrice: 80,
  paymentDueAt: null,
  route,
  timeslot,
  seats: [{ id: "seat-1", seatNumber: 1, label: "A1", status: "BOOKED" }],
  payment: {
    id: "payment-1",
    status: "COMPLETED",
    method: "CASH",
    amount: 80,
    transactionId: null,
    proofImageUrl: null,
    proofReference: null,
    proofSubmittedAt: null,
    paidAt: "2026-04-25T02:00:00Z",
    reviewNote: null,
    createdAt: "2026-04-25T01:59:00Z",
  },
};

async function seedStudentSession(page: Page) {
  await page.addInitScript((session) => {
    window.localStorage.setItem("auvan_auth_session", JSON.stringify(session));
    window.localStorage.setItem("auvan_access_token", session.token);
  }, studentSession);
}

async function mockStudentBookingFlow(page: Page) {
  let bookingCreated = false;

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

  await page.route("**/api/liff/routes", async (routeRequest) => {
    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: [route],
      }),
    });
  });

  await page.route("**/api/liff/routes/route-1", async (routeRequest) => {
    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: route,
      }),
    });
  });

  await page.route("**/api/liff/timeslots**", async (routeRequest) => {
    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: [timeslot],
      }),
    });
  });

  await page.route("**/api/liff/seats**", async (routeRequest) => {
    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: seats,
      }),
    });
  });

  await page.route("**/api/liff/bookings", async (routeRequest) => {
    if (routeRequest.request().method() === "POST") {
      bookingCreated = true;

      await routeRequest.fulfill({
        status: 201,
        contentType: "application/json",
        body: JSON.stringify({
          success: true,
          data: createdBooking,
        }),
      });
      return;
    }

    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: bookingCreated ? [createdBooking] : [],
      }),
    });
  });
}

test("student can create a booking and see it in my bookings", async ({ page }) => {
  await seedStudentSession(page);
  await mockStudentBookingFlow(page);

  await page.goto("/");

  await expect(page.getByRole("heading", { name: "Choose your trip" })).toBeVisible();
  await page.getByRole("button", { name: "09:00" }).click();
  await page.getByRole("button", { name: "Continue to Seat Selection" }).click();

  await expect(page.getByText("Passenger Details")).toBeVisible();
  await page.getByRole("button", { name: "A1" }).click();
  await page.getByRole("button", { name: "Create booking" }).click();

  await expect(page.getByText("Booking created successfully. Code: AUV-260425-ABCDE")).toBeVisible();
  await page.getByRole("link", { name: "View my bookings" }).click();

  await expect(page).toHaveURL(/\/my-bookings$/);
  await expect(page.getByRole("heading", { name: "Track your active and cancelled trips" })).toBeVisible();
  await expect(page.getByText("AUV-260425-ABCDE")).toBeVisible();
  await expect(page.getByText("ABAC - BTS Udomsuk")).toBeVisible();
  await expect(page.getByText("Status: Completed")).toBeVisible();
});
