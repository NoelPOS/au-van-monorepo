type LiffProfile = {
  displayName: string;
  userId?: string;
  pictureUrl?: string;
};

type LiffSdk = {
  init: (config: {
    liffId: string;
    withLoginOnExternalBrowser?: boolean;
  }) => Promise<void>;
  isLoggedIn: () => boolean;
  logout?: () => void;
  login: (config?: { redirectUri?: string }) => void;
  getIDToken: () => string | null;
  getProfile: () => Promise<LiffProfile>;
};

declare global {
  interface Window {
    liff?: LiffSdk;
  }
}

type JwtPayload = {
  exp?: number;
};

export async function loadLiffSdk() {
  if (typeof window === "undefined") {
    throw new Error("LIFF SDK requires a browser environment");
  }

  if (window.liff) return;

  await new Promise<void>((resolve, reject) => {
    const existing = document.querySelector("script[data-liff-sdk='true']");

    if (existing) {
      existing.addEventListener("load", () => resolve(), { once: true });
      existing.addEventListener("error", () => reject(new Error("Failed to load LIFF SDK")), {
        once: true,
      });
      return;
    }

    const script = document.createElement("script");
    script.src = "https://static.line-scdn.net/liff/edge/2/sdk.js";
    script.async = true;
    script.dataset.liffSdk = "true";
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("Failed to load LIFF SDK"));
    document.head.appendChild(script);
  });
}

function parseJwtPayload(idToken: string): JwtPayload | null {
  try {
    const parts = idToken.split(".");
    if (parts.length < 2) return null;

    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
    return JSON.parse(atob(padded)) as JwtPayload;
  } catch {
    return null;
  }
}

export function isLiffIdTokenExpired(idToken: string) {
  const payload = parseJwtPayload(idToken);
  if (!payload?.exp) return false;

  const nowInSeconds = Math.floor(Date.now() / 1000);
  return payload.exp <= nowInSeconds;
}

export function reloginWithLine() {
  if (typeof window === "undefined" || !window.liff) return;

  try {
    if (window.liff.isLoggedIn() && window.liff.logout) {
      window.liff.logout();
    }
  } catch {
    // Ignore logout issues and continue with login redirect.
  }

  window.liff.login({ redirectUri: window.location.href });
}

export async function bootstrapLiff(liffId: string) {
  await loadLiffSdk();

  if (!window.liff) {
    throw new Error("LIFF SDK is unavailable");
  }

  await window.liff.init({
    liffId,
    withLoginOnExternalBrowser: true,
  });

  if (!window.liff.isLoggedIn()) {
    reloginWithLine();
    return null;
  }

  const idToken = window.liff.getIDToken();
  if (!idToken) {
    throw new Error("Unable to read LIFF ID token");
  }

  if (isLiffIdTokenExpired(idToken)) {
    reloginWithLine();
    return null;
  }

  const profile = await window.liff.getProfile().catch(() => null);

  return {
    idToken,
    profile,
  };
}

