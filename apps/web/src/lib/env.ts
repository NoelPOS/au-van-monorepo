function readEnv(name: string, fallback = ""): string {
  const value = import.meta.env[name];
  return typeof value === "string" ? value : fallback;
}

export const env = {
  apiBaseUrl: readEnv("VITE_API_BASE_URL", "http://localhost:8080"),
  liffId: readEnv("VITE_LINE_LIFF_ID"),
  devBypassAuth: readEnv("VITE_DEV_BYPASS_AUTH") === "true",
  devAuthEmail: readEnv("VITE_DEV_AUTH_EMAIL"),
  devAuthPassword: readEnv("VITE_DEV_AUTH_PASSWORD"),
  paymentQrImageUrl: readEnv("VITE_PAYMENT_QR_IMAGE_URL"),
  paymentAccountName: readEnv("VITE_PAYMENT_ACCOUNT_NAME"),
  paymentAccountNumber: readEnv("VITE_PAYMENT_ACCOUNT_NUMBER"),
  paymentBankName: readEnv("VITE_PAYMENT_BANK_NAME"),
  appName: "AU Van",
};
