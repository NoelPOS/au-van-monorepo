import { FormEvent, useState } from "react";
import { ArrowRight, Shield } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { login } from "@/features/auth/api";
import { useAuth } from "@/features/auth/AuthProvider";

export function AdminSignInView() {
  const navigate = useNavigate();
  const { setSession, signOut } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      const session = await login({ email, password });

      if (!session.user.isAdmin) {
        signOut();
        setError("This account does not have admin access.");
        return;
      }

      setSession(session);
      navigate(appRoutes.admin);
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : "Login failed");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen">
      <div className="relative hidden flex-1 overflow-hidden bg-au-gradient-hero lg:flex lg:flex-col lg:justify-between">
        <div className="absolute inset-0 bg-dots opacity-[0.12]" />
        <div className="absolute -bottom-20 -right-20 h-72 w-72 rounded-full bg-white/5 blur-3xl" />

        <div className="relative flex flex-1 flex-col justify-center px-12 xl:px-16">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-white/15 backdrop-blur">
            <Shield className="h-6 w-6 text-white" />
          </div>
          <h2 className="mt-6 text-3xl font-bold leading-tight text-white xl:text-4xl">
            AU Van
            <br />
            <span className="text-[hsl(38,80%,65%)]">Admin Portal</span>
          </h2>
          <p className="mt-4 max-w-sm text-sm leading-relaxed text-white/50">
            Sign in against the Spring Boot backend. This frontend keeps auth state simple: one token, one provider, one route guard.
          </p>
        </div>
      </div>

      <div className="flex flex-1 items-center justify-center px-4 py-12 lg:max-w-xl">
        <div className="w-full max-w-sm">
          <div className="mb-8">
            <h1 className="text-2xl font-bold tracking-tight text-foreground">Admin Sign In</h1>
            <p className="mt-1.5 text-sm text-muted-foreground">
              Use your admin account to access the Java-backed admin portal.
            </p>
          </div>

          <form className="space-y-4" onSubmit={handleSubmit}>
            <div>
              <label htmlFor="email" className="text-xs font-medium">
                Email
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="admin@au.edu"
                className="mt-1.5 h-10 w-full rounded-lg border border-input bg-white px-3 text-sm outline-none"
                required
              />
            </div>

            <div>
              <label htmlFor="password" className="text-xs font-medium">
                Password
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Enter your password"
                className="mt-1.5 h-10 w-full rounded-lg border border-input bg-white px-3 text-sm outline-none"
                required
              />
            </div>

            {error ? (
              <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-700">
                {error}
              </p>
            ) : null}

            <button
              type="submit"
              disabled={isSubmitting}
              className="flex h-10 w-full items-center justify-center rounded-lg bg-primary text-sm font-semibold text-primary-foreground disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isSubmitting ? "Signing in..." : "Continue"}
              {!isSubmitting ? <ArrowRight className="ml-2 h-4 w-4" /> : null}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

