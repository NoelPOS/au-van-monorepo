import { AlertCircle, CheckCircle2, LogOut, ShieldCheck, UserRound } from "lucide-react";
import { useEffect, useState } from "react";
import { LiffPageHeader } from "@/components/layout/LiffPageHeader";
import { useAuth } from "@/features/auth/AuthProvider";
import { useMyProfile } from "@/features/profile/hooks/useMyProfile";
import { useUpdateMyProfile } from "@/features/profile/hooks/useUpdateMyProfile";

type ProfileForm = {
  name: string;
  phone: string;
  defaultPickupLocation: string;
};

export function ProfileView() {
  const { signOut, updateSessionUser } = useAuth();
  const profileQuery = useMyProfile();
  const updateProfile = useUpdateMyProfile();
  const [form, setForm] = useState<ProfileForm>({
    name: "",
    phone: "",
    defaultPickupLocation: "",
  });
  const [initialized, setInitialized] = useState(false);
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

  const profile = profileQuery.data;

  useEffect(() => {
    if (!profile || initialized) return;

    setForm({
      name: profile.name || "",
      phone: profile.phone || "",
      defaultPickupLocation: profile.defaultPickupLocation || "",
    });
    setInitialized(true);
  }, [initialized, profile]);

  async function handleSubmit() {
    setMessage(null);

    try {
      const updatedUser = await updateProfile.mutateAsync(form);
      updateSessionUser(updatedUser);
      setMessage({
        type: "success",
        text: "Profile updated. Your booking form defaults will use this info now.",
      });
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "Profile update failed",
      });
    }
  }

  if (profileQuery.isLoading) {
    return (
      <div className="px-4 py-8">
        <div className="h-24 animate-pulse rounded-xl border border-[#d6dcf4] bg-white" />
      </div>
    );
  }

  if (profileQuery.isError || !profile) {
    return (
      <div className="px-4 py-8">
        <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          Profile details could not be loaded from the backend.
        </p>
      </div>
    );
  }

  const initials = (profile.name || profile.displayName || profile.email || "U").charAt(0).toUpperCase();
  const emailLabel = profile.email || "No email on file";
  const providerLabel =
    profile.authProvider === "LINE"
      ? "This account is managed by LINE LIFF."
      : profile.authProvider === "GOOGLE"
        ? "This account uses Google sign-in."
        : "This account uses email and password.";

  return (
    <div className="px-4 pb-6 pt-3">
      <LiffPageHeader title="Profile" subtitle="Manage your account and sign-in settings" />

      <header className="rounded-2xl bg-gradient-to-br from-[#4259ce] to-[#2f45b6] px-4 py-4 text-white shadow-[0_16px_30px_rgba(31,47,141,0.25)]">
        <div className="flex items-center gap-3">
          {profile.profileImageUrl ? (
            <img
              src={profile.profileImageUrl}
              alt="Profile"
              className="h-12 w-12 rounded-xl border border-white/25 object-cover"
            />
          ) : (
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-white/20 text-base font-semibold">
              {initials}
            </div>
          )}
          <div>
            <h1 className="mt-1 text-base font-semibold">{profile.name || profile.displayName || "Student Account"}</h1>
            <p className="text-[11px] text-white/80">{emailLabel}</p>
          </div>
        </div>
      </header>

      {message ? (
        <div
          className={`mt-3 flex items-center gap-2 rounded-xl border px-3 py-2 text-[11px] ${
            message.type === "success"
              ? "border-emerald-100 bg-emerald-50 text-emerald-700"
              : "border-amber-200 bg-amber-50 text-amber-700"
          }`}
        >
          {message.type === "success" ? (
            <CheckCircle2 className="h-4 w-4" />
          ) : (
            <AlertCircle className="h-4 w-4" />
          )}
          {message.text}
        </div>
      ) : null}

      <section className="mt-3 rounded-2xl border border-[#d6dcf4] bg-white p-3 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
        <p className="inline-flex items-center gap-1.5 text-[11px] font-semibold text-[#2b3d9e]">
          <UserRound className="h-4 w-4" />
          Account Information
        </p>

        <div className="mt-3 space-y-3">
          <div>
            <label className="text-[10px] font-semibold uppercase tracking-wide text-[#7a86bc]">Email</label>
            <input
              value={profile.email || ""}
              disabled
              className="mt-1 h-9 w-full rounded-md border border-[#d8def5] bg-[#f7f9ff] px-3 text-xs text-[#6470a8] outline-none"
              placeholder="No email on file"
            />
          </div>

          <div>
            <label className="text-[10px] font-semibold uppercase tracking-wide text-[#7a86bc]">Name</label>
            <input
              value={form.name}
              onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
              className="mt-1 h-9 w-full rounded-md border border-[#d8def5] bg-white px-3 text-xs text-[#23349a] outline-none"
            />
          </div>

          <div>
            <label className="text-[10px] font-semibold uppercase tracking-wide text-[#7a86bc]">Phone</label>
            <input
              value={form.phone}
              onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value }))}
              className="mt-1 h-9 w-full rounded-md border border-[#d8def5] bg-white px-3 text-xs text-[#23349a] outline-none"
              placeholder="e.g. 08xxxxxxxx"
            />
          </div>

          <div>
            <label className="text-[10px] font-semibold uppercase tracking-wide text-[#7a86bc]">
              Default Pickup Location
            </label>
            <input
              value={form.defaultPickupLocation}
              onChange={(event) =>
                setForm((current) => ({ ...current, defaultPickupLocation: event.target.value }))
              }
              className="mt-1 h-9 w-full rounded-md border border-[#d8def5] bg-white px-3 text-xs text-[#23349a] outline-none"
              placeholder="e.g. Dorm A, Bang Na"
            />
          </div>

          <button
            type="button"
            onClick={handleSubmit}
            disabled={updateProfile.isPending}
            className="h-9 w-full rounded-xl bg-[#3f53c9] text-[12px] font-semibold text-white transition-colors hover:bg-[#3447b4] disabled:cursor-not-allowed disabled:opacity-70"
          >
            {updateProfile.isPending ? "Saving..." : "Update Profile"}
          </button>
        </div>
      </section>

      <section className="mt-3 rounded-2xl border border-[#d6dcf4] bg-white p-3 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
        <p className="inline-flex items-center gap-1.5 text-[11px] font-semibold text-[#2b3d9e]">
          <ShieldCheck className="h-4 w-4" />
          Sign-In Method
        </p>
        <p className="mt-2 text-[11px] text-[#6f7cb6]">{providerLabel}</p>
      </section>

      <div className="mt-4">
        <button
          type="button"
          onClick={signOut}
          className="inline-flex h-10 w-full items-center justify-center gap-1.5 rounded-xl border border-[#d6dcf4] bg-white text-[11px] font-semibold text-[#6f7cb6] transition-colors hover:bg-[#eef2ff] hover:text-[#2f3f9f]"
        >
          <LogOut className="h-4 w-4" />
          Sign out
        </button>
      </div>
    </div>
  );
}
