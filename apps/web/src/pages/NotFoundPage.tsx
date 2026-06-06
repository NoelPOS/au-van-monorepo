import { appRoutes } from "@/app/routes";
import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="max-w-md text-center">
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-[#6d7abc]">404</p>
        <h1 className="mt-2 text-3xl font-semibold text-[#20318d]">Page not found</h1>
        <p className="mt-3 text-sm text-[#6674b0]">
          The new React app is still being migrated, so only a few routes exist right now.
        </p>
        <Link
          to={appRoutes.home}
          className="mt-6 inline-flex rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground"
        >
          Go home
        </Link>
      </div>
    </div>
  );
}
