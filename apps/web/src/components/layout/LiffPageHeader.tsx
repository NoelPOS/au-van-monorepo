import type { ReactNode } from "react";
import { ArrowLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";

type LiffPageHeaderProps = {
  title: string;
  subtitle?: string;
  showBack?: boolean;
  backHref?: string;
  onBack?: () => void;
  rightSlot?: ReactNode;
};

export function LiffPageHeader({
  title,
  subtitle,
  showBack = false,
  backHref,
  onBack,
  rightSlot,
}: LiffPageHeaderProps) {
  const navigate = useNavigate();

  function handleBack() {
    if (onBack) {
      onBack();
      return;
    }

    if (backHref) {
      navigate(backHref);
      return;
    }

    navigate(-1);
  }

  return (
    <div className="mb-3 rounded-xl border border-[#d6dcf4] bg-white px-3 py-2.5 shadow-[0_1px_0_rgba(108,127,205,0.08)]">
      <div className="flex items-start justify-between gap-3">
        <div className="flex min-w-0 items-start gap-2.5">
          {showBack ? (
            <button
              type="button"
              onClick={handleBack}
              className="mt-0.5 flex h-8 w-8 items-center justify-center rounded-md border border-[#d8ddf2] bg-[#f8f9ff] text-[#3f53c9] transition-colors hover:bg-[#edf1ff]"
              aria-label="Go back"
            >
              <ArrowLeft className="h-4 w-4" />
            </button>
          ) : null}

          <div className="min-w-0">
            <h1 className="truncate text-sm font-semibold text-[#1f2f8d]">{title}</h1>
            {subtitle ? <p className="mt-0.5 text-[11px] text-[#6f7cb6]">{subtitle}</p> : null}
          </div>
        </div>

        {rightSlot ? <div className="shrink-0">{rightSlot}</div> : null}
      </div>
    </div>
  );
}
