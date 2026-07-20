import { Spinner } from "@/components/Spinner";

type LoadingOverlayProps = {
  label?: string;
};

export function LoadingOverlay({ label = "Đang tải..." }: LoadingOverlayProps) {
  return (
    <div className="fixed inset-0 z-100 flex items-center justify-center bg-[#111111]/80 backdrop-blur-sm">
      <div className="border border-[#41413e] bg-[#191919] px-8 py-6 shadow-2xl">
        <Spinner label={label} />
      </div>
    </div>
  );
}
