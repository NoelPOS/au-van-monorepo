import { Bell, BookOpen, Home, User } from "lucide-react";
import { NavLink } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { useAuth } from "@/features/auth/AuthProvider";
import { useUnreadNotificationCount } from "@/features/notifications/hooks/useUnreadNotificationCount";

const navItems = [
  { to: appRoutes.home, label: "Home", icon: Home, match: appRoutes.home },
  { to: appRoutes.myBookings, label: "My Bookings", icon: BookOpen, match: appRoutes.myBookings },
  { to: appRoutes.notifications, label: "Notifications", icon: Bell, match: appRoutes.notifications },
  { to: appRoutes.profile, label: "Profile", icon: User, match: appRoutes.profile },
] as const;

export function LiffBottomNav() {
  const { isAuthenticated } = useAuth();
  const unreadCountQuery = useUnreadNotificationCount();

  if (!isAuthenticated) {
    return null;
  }

  const unreadCount = unreadCountQuery.data ?? 0;

  return (
    <nav className="fixed bottom-0 left-1/2 z-50 w-full max-w-md -translate-x-1/2 border-t border-[#d8dcef] bg-[#3f53c9] px-2 pb-2 pt-1.5 shadow-[0_-8px_30px_rgba(20,38,120,0.2)]">
      <ul className="grid grid-cols-4 items-center">
        {navItems.map((item) => {
          const Icon = item.icon;

          return (
            <li key={item.to}>
              <NavLink
                to={item.to}
                end={item.to === appRoutes.home}
                className={({ isActive }) =>
                  `relative flex flex-col items-center gap-0.5 rounded-md px-1 py-1.5 text-[10px] transition-colors ${
                    isActive ? "text-white" : "text-white/70 hover:text-white/90"
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    <div className="relative">
                      <Icon className="h-3.5 w-3.5" />
                      {item.to === appRoutes.notifications && unreadCount > 0 ? (
                        <span className="absolute -right-2 -top-2 flex h-3.5 min-w-3.5 items-center justify-center rounded-full bg-[#ffda5e] px-1 text-[8px] font-bold text-[#1f2f8d]">
                          {unreadCount > 9 ? "9+" : unreadCount}
                        </span>
                      ) : null}
                    </div>
                    <span className="leading-none">{item.label}</span>
                    {isActive ? <span className="mt-0.5 h-0.5 w-6 rounded-full bg-white" /> : null}
                  </>
                )}
              </NavLink>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
