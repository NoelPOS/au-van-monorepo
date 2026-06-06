import { BookOpen, Clock, CreditCard, LayoutDashboard, LogOut, Route, Users } from "lucide-react";
import { Link, NavLink } from "react-router-dom";
import { adminNavItems, appRoutes } from "@/app/routes";
import { useAuth } from "@/features/auth/AuthProvider";

const adminIcons = {
  [appRoutes.admin]: LayoutDashboard,
  [appRoutes.adminBookings]: BookOpen,
  [appRoutes.adminPayments]: CreditCard,
  [appRoutes.adminRoutes]: Route,
  [appRoutes.adminTimeslots]: Clock,
  [appRoutes.adminUsers]: Users,
} as const;

export function AdminNavbar() {
  const { session, signOut } = useAuth();

  return (
    <header className="sticky top-0 z-40 border-b border-border/60 bg-white/95 backdrop-blur">
      <div className="mx-auto flex h-14 max-w-7xl items-center justify-between gap-2 px-4">
        <div className="flex items-center gap-2">
          <Link to={appRoutes.admin} className="text-sm font-bold tracking-tight text-foreground">
            AU Van Admin
          </Link>
          <span className="rounded-full bg-primary/10 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wider text-primary">
            Portal
          </span>
        </div>

        <nav className="hidden items-center gap-1 md:flex">
          {adminNavItems
            .filter((item) => item.to !== appRoutes.adminAuditLogs)
            .map((item) => {
              const Icon = adminIcons[item.to as keyof typeof adminIcons];

              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === appRoutes.admin}
                  className={({ isActive }) =>
                    `flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
                      isActive
                        ? "bg-primary/10 text-primary"
                        : "text-muted-foreground hover:bg-muted hover:text-foreground"
                    }`
                  }
                >
                  <Icon className="h-3.5 w-3.5" />
                  {item.label}
                </NavLink>
              );
            })}
        </nav>

        <div className="flex items-center gap-2">
          <Link to={appRoutes.home} className="hidden text-xs text-muted-foreground sm:block">
            Back to LIFF
          </Link>
          <span className="hidden text-xs text-muted-foreground sm:block">
            {session?.user?.name || "Admin"}
          </span>
          <button
            type="button"
            onClick={signOut}
            className="rounded-lg p-2 text-muted-foreground transition-colors hover:bg-destructive/10 hover:text-destructive"
            title="Sign out"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </div>
    </header>
  );
}
