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

const pendingBooking = {
  id: "booking-pending-1",
  bookingCode: "AUV-260425-PAY01",
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

const reviewBooking = {
  ...pendingBooking,
  status: "PAYMENT_UNDER_REVIEW",
  payment: {
    ...pendingBooking.payment,
    status: "PENDING_REVIEW",
    proofImageUrl: "data:image/png;base64,ZmFrZS1wcm9vZg==",
    proofReference: "KPLUS-123456",
    proofSubmittedAt: "2026-04-25T02:10:00Z",
    paidAt: "2026-04-25T02:09:00Z",
  },
};

async function seedStudentSession(page: Page) {
  await page.addInitScript((session) => {
    window.localStorage.setItem("auvan_auth_session", JSON.stringify(session));
    window.localStorage.setItem("auvan_access_token", session.token);
  }, studentSession);
}

async function mockPaymentProofFlow(page: Page) {
  let proofSubmitted = false;

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

  await page.route("**/api/liff/bookings/booking-pending-1", async (routeRequest) => {
    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: proofSubmitted ? reviewBooking : pendingBooking,
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
        data: [proofSubmitted ? reviewBooking : pendingBooking],
      }),
    });
  });

  await page.route("**/api/liff/bookings/booking-pending-1/payment-proof", async (routeRequest) => {
    proofSubmitted = true;

    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: reviewBooking.payment,
      }),
    });
  });
}

test("student can submit payment proof and see review state in my bookings", async ({ page }) => {
  await seedStudentSession(page);
  await mockPaymentProofFlow(page);

  await page.goto("/payment/booking-pending-1");

  await expect(page.getByRole("heading", { name: "Pay and upload your slip" })).toBeVisible();
  await page.getByPlaceholder("e.g. KPLUS-123456").fill("KPLUS-123456");
  await page.locator('input[type="datetime-local"]').fill("2026-04-25T09:09");
  await page.locator('input[type="file"]').setInputFiles({
    name: "slip.png",
    mimeType: "image/png",
    buffer: Buffer.from("fake-proof"),
  });
  await page.getByRole("button", { name: "Submit for Review" }).click();

  await expect(page).toHaveURL(/\/my-bookings$/);
  await expect(page.getByText("AUV-260425-PAY01")).toBeVisible();
  await expect(page.getByText("Status: Pending Review")).toBeVisible();
  await expect(page.getByText("Payment proof is required for this booking.")).toHaveCount(0);
});
