type SpinnerProps = {
  label?: string;
  className?: string;
};

export function Spinner({
  label = "Đang tải...",
  className = "",
}: SpinnerProps) {
  return (
    <div
      role="status"
      aria-live="polite"
      className={`flex items-center justify-center gap-3 text-sm text-[#aaa9a4] ${className}`}
    >
      <span
        aria-hidden="true"
        className="size-6 animate-spin rounded-full border-2 border-[#494946] border-t-[#81b3da]"
      />

      <span>{label}</span>
    </div>
  );
}
