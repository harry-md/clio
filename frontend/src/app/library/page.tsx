import { Suspense } from "react";
import { Header } from "@/components/Header";
import { LibraryPageClient } from "@/components/LibraryPageClient";
import { LoadingOverlay } from "@/components/LoadingOverlay";

const LibraryPage = () => {
  return (
    <main className="min-h-screen bg-background">
      <Header />

      <section
        id="library-list"
        className="mx-auto max-w-360 scroll-mt-20 px-5 py-12 lg:px-10 lg:py-16"
      >
        <Suspense fallback={<LoadingOverlay />}>
          <LibraryPageClient />
        </Suspense>
      </section>
    </main>
  );
};

export default LibraryPage;
