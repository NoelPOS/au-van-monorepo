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

async function mockDashboardStats(page: Page) {
  await page.route("**/api/admin/dashboard/stats", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: {
          totalBookings: 42,
          confirmedBookings: 30,
          pendingPaymentBookings: 5,
          cancelledBookings: 7,
          totalPassengers: 30,
          totalRevenue: 2400,
          averageOccupancyPercent: 75.5,
          totalUsers: 18,
        },
      }),
    });
  });
}

async function seedAdminSession(page: Page) {
  await page.addInitScript((session) => {
    window.localStorage.setItem("auvan_auth_session", JSON.stringify(session));
    window.localStorage.setItem("auvan_access_token", session.token);
  }, adminSession);
}

test("admin routes redirect unauthenticated users to sign-in", async ({ page }) => {
  await page.goto("/admin");

  await expect(page).toHaveURL(/\/auth$/);
  await expect(page.getByRole("heading", { name: "Admin Sign In" })).toBeVisible();
});

test("admin sign-in stores the session and opens the dashboard", async ({ page }) => {
  await mockDashboardStats(page);
  await page.route("**/api/auth/login", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: adminSession,
      }),
    });
  });

  await page.goto("/auth");
  await page.getByLabel("Email").fill("admin@au.edu");
  await page.getByLabel("Password").fill("secret123");
  await page.getByRole("button", { name: "Continue" }).click();

  await expect(page).toHaveURL(/\/admin$/);
  await expect(page.getByRole("heading", { name: "Operational snapshot" })).toBeVisible();
  await expect(page.getByText("Total Bookings")).toBeVisible();
  await expect(page.getByText("42")).toBeVisible();
});

test("seeded admin session can open dashboard quick links", async ({ page }) => {
  await seedAdminSession(page);
  await mockDashboardStats(page);

  await page.goto("/admin");

  await expect(page.getByRole("heading", { name: "Operational snapshot" })).toBeVisible();
  await expect(page.getByRole("link", { name: "Open payments" })).toBeVisible();
  await expect(page.getByRole("link", { name: "Open routes" })).toBeVisible();
});
