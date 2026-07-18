import { Suspense } from "react";
import { Header } from "@/components/Header";
import { SearchPageContent } from "@/components/SearchPageContent";
import { LoadingOverlay } from "@/components/LoadingOverlay";

export default function SearchPage() {
  return (
    <main className="min-h-screen bg-[#151515]">
      <Header />

      <Suspense
        fallback={<LoadingOverlay label="Đang chuyển trang tìm kiếm..." />}
      >
        <SearchPageContent />
      </Suspense>
    </main>
  );
}
