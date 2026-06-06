import { LiffAuthGate } from "@/features/auth/components/LiffAuthGate";
import { Outlet } from "react-router-dom";
import { LiffBottomNav } from "@/components/layout/LiffBottomNav";

export function LiffLayout() {
  return (
    <div className="min-h-screen bg-[#edf1fa]">
      <div className="mx-auto min-h-screen w-full max-w-md bg-[#f7f8fc] shadow-[0_0_0_1px_rgba(117,135,214,0.12)]">
        <main className="pb-20">
          <LiffAuthGate>
            <Outlet />
          </LiffAuthGate>
        </main>
      </div>
      <LiffBottomNav />
    </div>
  );
}
