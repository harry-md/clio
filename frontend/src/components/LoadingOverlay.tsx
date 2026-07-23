import { Spinner } from "@/components/Spinner";

export function LoadingOverlay() {
  return (
    <div className="fixed inset-0 z-100 flex items-center justify-center bg-overlay/80 backdrop-blur-sm">
      <Spinner className="size-24" />
    </div>
  );
}
