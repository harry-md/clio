import { Suspense } from "react";
import { Header } from "@/components/Header";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { SearchPageContent } from "@/components/SearchPageContent";

interface SearchPageProps {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}

const SearchPage = ({ searchParams }: SearchPageProps) => {
  return (
    <main className="min-h-screen bg-background">
      <Header />

      <Suspense fallback={<LoadingOverlay />}>
        <SearchPageContent searchParams={searchParams} />
      </Suspense>
    </main>
  );
};
export default SearchPage;
