import { cacheLife } from "next/cache";
import { BookCard } from "@/components/BookCard";
import { EmptyState } from "@/components/EmptyState";
import {
  type SearchFilters,
  SearchFiltersForm,
} from "@/components/SearchFiltersForm";
import { Alert, AlertDescription } from "@/components/ui/alert";
import type { Book, PageResponse } from "@/lib/types";

type SearchParams = Record<string, string | string[] | undefined>;

interface SearchPageContentProps {
  searchParams: Promise<SearchParams>;
}

const getParam = (value: string | string[] | undefined) => {
  if (Array.isArray(value)) {
    return value[0] ?? "";
  }

  return value ?? "";
};

const readFilters = (params: SearchParams): SearchFilters => ({
  title: getParam(params.title),
  authorFullname: getParam(params.authorFullname),
  fromPrice: getParam(params.fromPrice),
  toPrice: getParam(params.toPrice),
  fromRating: getParam(params.fromRating),
  toRating: getParam(params.toRating),
  categoryId: getParam(params.categoryId),
  authorId: getParam(params.authorId),
});

const getRequestFilters = (filters: SearchFilters): Record<string, string> => {
  const params: Record<string, string> = {};

  for (const key of Object.keys(filters) as Array<keyof SearchFilters>) {
    const value = filters[key].trim();

    if (value !== "") {
      params[key] = value;
    }
  }

  return params;
};

const isInvalidRange = (from: string, to: string) => {
  if (from === "" || to === "") {
    return false;
  }
  return Number(from) > Number(to);
};

const fetchSearchBooks = async (
  filters: Record<string, string>,
  sort: string,
): Promise<PageResponse<Book>> => {
  "use cache";
  cacheLife({ revalidate: 300 });

  const params = new URLSearchParams(filters);
  params.append("page", "0");
  params.append("sort", sort);
  params.append("sort", "id,desc");

  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/books?${params.toString()}`,
  );
  if (!res.ok) {
    throw new Error("Không thể tìm kiếm sách.");
  }

  const json = await res.json();
  return json.data ?? json;
};

export const SearchPageContent = async ({
  searchParams,
}: SearchPageContentProps) => {
  const params = await searchParams;

  const filters = readFilters(params);

  const authorName = getParam(params.authorName);
  const categoryName = getParam(params.categoryName);

  const sort = getParam(params.sort) || "createdAt,desc";

  let books: Book[] = [];
  let error = "";

  if (isInvalidRange(filters.fromPrice, filters.toPrice)) {
    error = "Khoảng giá không hợp lệ.";
  } else if (isInvalidRange(filters.fromRating, filters.toRating)) {
    error = "Khoảng đánh giá không hợp lệ.";
  } else {
    try {
      const data = await fetchSearchBooks(getRequestFilters(filters), sort);
      books = data.content;
    } catch (requestError: unknown) {
      if (requestError instanceof Error) {
        error = requestError.message ?? "Có lỗi khi tìm kiếm sách.";
      }
    }
  }

  return (
    <section className="mx-auto max-w-360 px-5 py-12 lg:px-10 lg:py-16">
      <div className="border-b border-border pb-8">
        <h1 className="font-serif text-5xl font-bold text-foreground">
          Tìm kiếm sách
        </h1>
      </div>

      <SearchFiltersForm
        key={JSON.stringify({
          filters,
          sort,
          authorName,
          categoryName,
        })}
        initialFilters={filters}
        initialSort={sort}
        initialAuthorName={authorName}
        initialCategoryName={categoryName}
      />

      <div className="flex items-center justify-between py-7">
        <h2 className="text-3xl font-semibold text-foreground">
          Kết quả tìm kiếm
        </h2>
      </div>

      {error && (
        <Alert className="mb-5" variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {books.length > 0 ? (
        <div className="grid grid-cols-2 gap-x-5 gap-y-11 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6">
          {books.map((book) => (
            <BookCard key={book.id} book={book} />
          ))}
        </div>
      ) : !error ? (
        <EmptyState title="Không tìm thấy sách phù hợp" />
      ) : null}
    </section>
  );
};
