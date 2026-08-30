import { BookCard } from "@/components/BookCard";
import { EmptyState } from "@/components/EmptyState";
import {
  type SearchFilters,
  SearchFiltersForm,
} from "@/components/SearchFiltersForm";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Api, getApiErrorMessage } from "@/lib/api";
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
  keyword: getParam(params.keyword),
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
  const { data } = await Api.get<PageResponse<Book>>("/books", {
    baseURL: `${process.env.BACKEND_URL}/api`,
    params: {
      ...filters,
      page: 0,
      sort: [sort, "id,desc"],
    },
  });

  return data;
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
      error = getApiErrorMessage(requestError, "Có lỗi khi tìm kiếm sách.");
      console.log(requestError);
    }
  }

  return (
    <section className="mx-auto max-w-360 px-5 py-12 lg:px-10 lg:py-16">
      <div className="border-b border-border pb-8">
        <h1 className="text-5xl font-bold text-foreground">Tìm kiếm</h1>
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
