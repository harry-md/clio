import { Suspense } from "react";
import { Header } from "@/components/Header";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { SearchPageContent } from "@/components/SearchPageContent";

export default function SearchPage() {
  return (
    <main className="min-h-screen bg-background">
      <Header />

      <Suspense fallback={<LoadingOverlay />}>
        <SearchPageContent />
      </Suspense>
    </main>
  );
}
