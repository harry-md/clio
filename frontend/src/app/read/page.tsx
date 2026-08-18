import { Suspense } from "react";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { ReaderRoute } from "@/components/ReaderRoute";

const ReadPage = () => {
  return (
    <main className="min-h-screen bg-background">
      <Suspense fallback={<LoadingOverlay />}>
        <ReaderRoute />
      </Suspense>
    </main>
  );
};

export default ReadPage;
