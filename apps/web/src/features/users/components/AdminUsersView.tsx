import { Shield, UserRound } from "lucide-react";
import { useMemo, useState } from "react";
import { useAdminUsers } from "@/features/users/hooks/useAdminUsers";
import { useUpdateAdminUser } from "@/features/users/hooks/useUpdateAdminUser";
import type { AuthUser } from "@/types/auth";

type UserForm = {
  name: string;
  phone: string;
  defaultPickupLocation: string;
  profileImageUrl: string;
  isAdmin: boolean;
};

const emptyUserForm: UserForm = {
  name: "",
  phone: "",
  defaultPickupLocation: "",
  profileImageUrl: "",
  isAdmin: false,
};

function toUserForm(user: AuthUser): UserForm {
  return {
    name: user.name || "",
    phone: user.phone || "",
    defaultPickupLocation: user.defaultPickupLocation || "",
    profileImageUrl: user.profileImageUrl || "",
    isAdmin: user.isAdmin,
  };
}

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function roleClass(isAdmin: boolean) {
  return isAdmin
    ? "border-[#d9dcfb] bg-[#eef1ff] text-[#3142a5]"
    : "border-slate-200 bg-slate-100 text-slate-600";
}

function userLabel(user: AuthUser) {
  return user.name || user.displayName || user.email || user.lineUserId || "Unnamed User";
}

export function AdminUsersView() {
  const [page, setPage] = useState(0);
  const usersQuery = useAdminUsers(page, 50);
  const updateUser = useUpdateAdminUser();
  const [selectedUser, setSelectedUser] = useState<AuthUser | null>(null);
  const [form, setForm] = useState<UserForm>(emptyUserForm);
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

  const pageData = usersQuery.data;
  const users = pageData?.content ?? [];

  const sortedUsers = useMemo(
    () =>
      [...users].sort((left, right) => {
        if (left.isAdmin !== right.isAdmin) {
          return left.isAdmin ? -1 : 1;
        }
        return userLabel(left).localeCompare(userLabel(right));
      }),
    [users]
  );

  function selectUser(user: AuthUser) {
    setSelectedUser(user);
    setForm(toUserForm(user));
    setMessage(null);
  }

  async function handleSave() {
    if (!selectedUser) return;

    setMessage(null);

    try {
      const updatedUser = await updateUser.mutateAsync({
        userId: selectedUser.id,
        name: form.name,
        phone: form.phone,
        defaultPickupLocation: form.defaultPickupLocation,
        profileImageUrl: form.profileImageUrl,
        isAdmin: form.isAdmin,
      });

      setSelectedUser(updatedUser);
      setForm(toUserForm(updatedUser));
      setMessage({ type: "success", text: "User updated." });
    } catch (error) {
      setMessage({
        type: "error",
        text: error instanceof Error ? error.message : "User update failed",
      });
    }
  }

  return (
    <section>
      <header className="rounded-2xl bg-gradient-to-br from-[#4259ce] to-[#2f45b6] px-5 py-5 text-white shadow-[0_16px_30px_rgba(31,47,141,0.25)]">
        <p className="text-[11px] uppercase tracking-wide text-white/70">Admin Users</p>
        <h1 className="mt-1 text-xl font-semibold">Manage accounts and admin access</h1>
        <p className="mt-1 text-sm text-white/80">
          Edit profile basics and promote or demote admin access without introducing a separate role system.
        </p>
      </header>

      {message ? (
        <p
          className={`mt-4 rounded-lg border px-3 py-2 text-sm ${
            message.type === "success"
              ? "border-emerald-200 bg-emerald-50 text-emerald-700"
              : "border-amber-200 bg-amber-50 text-amber-700"
          }`}
        >
          {message.text}
        </p>
      ) : null}

      <div className="mt-5 grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
        <div>
          <div className="mb-4">
            <p className="text-sm font-semibold text-[#22339a]">Users</p>
            <p className="text-xs text-[#6f7cb6]">{pageData?.total ?? users.length} account(s)</p>
          </div>

          {usersQuery.isLoading ? (
            <div className="space-y-3">
              <div className="h-24 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
              <div className="h-24 animate-pulse rounded-2xl border border-[#dbe2fb] bg-white" />
            </div>
          ) : null}

          {usersQuery.isError ? (
            <div className="rounded-xl border border-amber-200 bg-amber-50 px-3 py-3 text-sm text-amber-700">
              Admin users could not be loaded from the backend.
            </div>
          ) : null}

          {!usersQuery.isLoading && !usersQuery.isError && sortedUsers.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-10 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
              <UserRound className="mx-auto h-5 w-5 text-[#98a5da]" />
              <p className="mt-2 text-sm font-semibold text-[#3041a1]">No users found</p>
              <p className="mt-1 text-xs text-[#6f7cb6]">
                User records will appear here once students or admins authenticate through the system.
              </p>
            </div>
          ) : null}

          {!usersQuery.isLoading && !usersQuery.isError && sortedUsers.length > 0 ? (
            <div className="space-y-3">
              {sortedUsers.map((user) => (
                <button
                  key={user.id}
                  type="button"
                  onClick={() => selectUser(user)}
                  className="w-full rounded-2xl border border-[#d6dcf4] bg-white p-4 text-left shadow-[0_8px_20px_rgba(57,85,194,0.06)] transition-colors hover:border-[#c1cbee]"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <p className="text-sm font-semibold text-[#22339a]">
                        {userLabel(user)}
                      </p>
                      <p className="mt-1 truncate text-[11px] text-[#5564ab]">
                        {user.email || user.lineUserId || "-"}
                      </p>
                    </div>
                    <span
                      className={`rounded-full border px-2 py-1 text-[10px] font-semibold ${roleClass(
                        user.isAdmin
                      )}`}
                    >
                      {user.isAdmin ? "Admin" : "User"}
                    </span>
                  </div>

                  <div className="mt-3 grid gap-1 text-[11px] text-[#5564ab]">
                    <p>Phone: {user.phone || "-"}</p>
                    <p>Pickup: {user.defaultPickupLocation || "-"}</p>
                    <p>Provider: {user.authProvider ? formatLabel(user.authProvider) : "-"}</p>
                  </div>
                </button>
              ))}
            </div>
          ) : null}

          {!usersQuery.isLoading && !usersQuery.isError && (pageData?.totalPages ?? 0) > 1 ? (
            <div className="mt-4 flex items-center justify-between rounded-2xl border border-[#d6dcf4] bg-white px-4 py-3">
              <button
                type="button"
                onClick={() => setPage((current) => Math.max(0, current - 1))}
                disabled={page === 0}
                className="rounded-full bg-[#eef1fa] px-3 py-1.5 text-sm font-semibold text-[#6875b0] disabled:cursor-not-allowed disabled:opacity-60"
              >
                Previous
              </button>
              <p className="text-sm text-[#6f7cb6]">
                Page {page + 1} of {pageData?.totalPages ?? 1}
              </p>
              <button
                type="button"
                onClick={() =>
                  setPage((current) =>
                    Math.min((pageData?.totalPages ?? 1) - 1, current + 1)
                  )
                }
                disabled={page >= (pageData?.totalPages ?? 1) - 1}
                className="rounded-full bg-[#eef1fa] px-3 py-1.5 text-sm font-semibold text-[#6875b0] disabled:cursor-not-allowed disabled:opacity-60"
              >
                Next
              </button>
            </div>
          ) : null}
        </div>

        <div className="rounded-2xl border border-[#d6dcf4] bg-white p-4 shadow-[0_8px_20px_rgba(57,85,194,0.06)]">
          <p className="text-[10px] font-semibold uppercase tracking-wide text-[#7682bb]">
            {selectedUser ? "Edit User" : "Choose a User"}
          </p>

          {selectedUser ? (
            <div className="mt-4 space-y-3">
              <div className="rounded-xl bg-[#f7f8fd] p-3 text-sm text-[#4c5ca7]">
                <p><span className="font-semibold text-[#22339a]">Email:</span> {selectedUser.email || "-"}</p>
                <p><span className="font-semibold text-[#22339a]">Provider:</span> {selectedUser.authProvider ? formatLabel(selectedUser.authProvider) : "-"}</p>
                <p><span className="font-semibold text-[#22339a]">Line User ID:</span> {selectedUser.lineUserId || "-"}</p>
              </div>

              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">Name</label>
                <input
                  value={form.name}
                  onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                />
              </div>

              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">Phone</label>
                <input
                  value={form.phone}
                  onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value }))}
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                />
              </div>

              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                  Default Pickup Location
                </label>
                <input
                  value={form.defaultPickupLocation}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, defaultPickupLocation: event.target.value }))
                  }
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                />
              </div>

              <div>
                <label className="mb-1 block text-[10px] font-semibold uppercase text-[#7682bb]">
                  Profile Image URL
                </label>
                <input
                  value={form.profileImageUrl}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, profileImageUrl: event.target.value }))
                  }
                  className="h-10 w-full rounded-xl border border-[#d7dcf3] bg-white px-3 text-sm text-[#26368f] outline-none"
                  placeholder="Optional"
                />
              </div>

              <label className="flex items-center gap-3 rounded-xl border border-[#d7dcf3] bg-[#f8f9ff] px-3 py-3 text-sm text-[#3142a5]">
                <input
                  type="checkbox"
                  checked={form.isAdmin}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, isAdmin: event.target.checked }))
                  }
                  className="h-4 w-4 rounded border-[#c7d0f2]"
                />
                <span className="inline-flex items-center gap-1.5 font-semibold">
                  <Shield className="h-4 w-4" />
                  Grant admin access
                </span>
              </label>

              <button
                type="button"
                onClick={handleSave}
                disabled={updateUser.isPending}
                className="h-10 w-full rounded-xl bg-[#3f53c9] text-sm font-semibold text-white transition-colors hover:bg-[#3447b4] disabled:cursor-not-allowed disabled:opacity-70"
              >
                {updateUser.isPending ? "Saving..." : "Save User"}
              </button>
            </div>
          ) : (
            <div className="mt-4 rounded-2xl border border-dashed border-[#cad3f1] bg-white px-4 py-10 text-center shadow-[0_8px_20px_rgba(57,85,194,0.05)]">
              <UserRound className="mx-auto h-5 w-5 text-[#98a5da]" />
              <p className="mt-2 text-sm font-semibold text-[#3041a1]">Choose a user to inspect</p>
              <p className="mt-1 text-xs text-[#6f7cb6]">
                The editor on the right lets you update profile basics and admin access.
              </p>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
