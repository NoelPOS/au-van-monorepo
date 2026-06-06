import { expect, test, type Page } from "@playwright/test";

const adminSession = {
  token: "playwright-admin-token",
  tokenType: "Bearer",
  expiresIn: 86400,
  user: {
    id: "admin-user-id",
    email: "admin@au.edu",
    isAdmin: true,
    name: "Playwright Admin",
  },
};

const pendingPayment = {
  id: "payment-1",
  bookingId: "booking-1",
  amount: 80,
  method: "PROMPTPAY",
  status: "PENDING_REVIEW",
  transactionId: null,
  proofImageUrl: "data:image/png;base64,ZmFrZS1wcm9vZg==",
  proofReference: "KPLUS-123456",
  proofSubmittedAt: "2026-04-25T02:10:00Z",
  reviewedBy: null,
  reviewedAt: null,
  reviewNote: null,
  paidAt: "2026-04-25T02:09:00Z",
  refundedAt: null,
  createdAt: "2026-04-25T02:00:00Z",
};

const completedPayment = {
  ...pendingPayment,
  status: "COMPLETED",
  transactionId: "TX-REVIEW-001",
  reviewedBy: "admin-user-id",
  reviewedAt: "2026-04-25T02:15:00Z",
  reviewNote: "Approved in Playwright",
};

async function seedAdminSession(page: Page) {
  await page.addInitScript((session) => {
    window.localStorage.setItem("auvan_auth_session", JSON.stringify(session));
    window.localStorage.setItem("auvan_access_token", session.token);
  }, adminSession);
}

async function mockAdminPaymentReview(page: Page) {
  let reviewed = false;

  await page.route("**/api/admin/payments**", async (routeRequest) => {
    if (routeRequest.request().method() !== "GET") {
      await routeRequest.fallback();
      return;
    }

    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: {
          content: [reviewed ? completedPayment : pendingPayment],
          total: 1,
          page: 0,
          size: 20,
          totalPages: 1,
        },
      }),
    });
  });

  await page.route("**/api/admin/payments/payment-1/review", async (routeRequest) => {
    reviewed = true;

    await routeRequest.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: completedPayment,
      }),
    });
  });
}

test("admin can approve a pending payment review", async ({ page }) => {
  await seedAdminSession(page);
  await mockAdminPaymentReview(page);

  await page.goto("/admin/payments");

  await expect(page.getByRole("heading", { name: "Review uploaded payment slips" })).toBeVisible();
  await expect(page.getByText("Pending Review (1)")).toBeVisible();
  await page.getByRole("button", { name: "Review payment" }).click();

  await expect(page.getByText("Payment Review")).toBeVisible();
  await page.getByPlaceholder("Optional banking transaction ID").fill("TX-REVIEW-001");
  await page.getByPlaceholder("Optional context for approval or rejection").fill("Approved in Playwright");
  await page.getByRole("button", { name: "Approve" }).click();

  await expect(page.getByText("No payments in this view")).toBeVisible();
});
