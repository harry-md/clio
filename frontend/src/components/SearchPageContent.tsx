"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { type SubmitEvent, useCallback, useEffect, useState } from "react";
import { BookCard } from "@/components/BookCard";
import { EmptyState } from "@/components/EmptyState";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import {
  type SearchFilters,
  SearchFiltersForm,
} from "@/components/SearchFiltersForm";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { Book, PageResponse } from "@/lib/types";

const readFilters = (params: Pick<URLSearchParams, "get">): SearchFilters => ({
  title: params.get("title") ?? "",
  authorFullname: params.get("authorFullname") ?? "",
  fromPrice: params.get("fromPrice") ?? "",
  toPrice: params.get("toPrice") ?? "",
  fromRating: params.get("fromRating") ?? "",
  toRating: params.get("toRating") ?? "",
  categoryId: params.get("categoryId") ?? "",
  authorId: params.get("authorId") ?? "",
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

export function SearchPageContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [filters, setFilters] = useState<SearchFilters>(() =>
    readFilters(searchParams),
  );

  const [submittedFilters, setSubmittedFilters] =
    useState<SearchFilters>(filters);

  const [authorName, setAuthorName] = useState(
    () => searchParams.get("authorName") ?? "",
  );

  const [categoryName, setCategoryName] = useState(
    () => searchParams.get("categoryName") ?? "",
  );

  const [sort, setSort] = useState(
    () => searchParams.get("sort") ?? "createdAt,desc",
  );

  const [submittedSort, setSubmittedSort] = useState(sort);
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const updateFilter = useCallback(
    (field: keyof SearchFilters, value: string) => {
      setFilters((current) => {
        if (current[field] === value) {
          return current;
        }

        return {
          ...current,
          [field]: value,
        };
      });
    },
    [],
  );

  const handleSearch = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (isInvalidRange(filters.fromPrice, filters.toPrice)) {
      setError("Khoảng giá không hợp lệ.");
      return;
    }

    if (isInvalidRange(filters.fromRating, filters.toRating)) {
      setError("Khoảng đánh giá không hợp lệ.");
      return;
    }

    setError("");

    const nextSubmittedFilters = {
      ...filters,
    };

    setSubmittedFilters(nextSubmittedFilters);
    setSubmittedSort(sort);

    const nextParams = new URLSearchParams();

    for (const [key, value] of Object.entries(nextSubmittedFilters)) {
      if (value.trim() !== "") {
        nextParams.set(key, value.trim());
      }
    }

    if (nextSubmittedFilters.authorId && authorName) {
      nextParams.set("authorName", authorName);
    }

    if (nextSubmittedFilters.categoryId && categoryName) {
      nextParams.set("categoryName", categoryName);
    }

    nextParams.set("sort", sort);

    router.replace(`/search?${nextParams.toString()}`, {
      scroll: false,
    });
  };

  useEffect(() => {
    if (isInvalidRange(submittedFilters.fromPrice, submittedFilters.toPrice)) {
      setLoading(false);
      setError("Khoảng giá không hợp lệ.");
      return;
    }

    if (
      isInvalidRange(submittedFilters.fromRating, submittedFilters.toRating)
    ) {
      setLoading(false);
      setError("Khoảng đánh giá không hợp lệ.");
      return;
    }

    const controller = new AbortController();

    const fetchBooks = async () => {
      setError("");

      try {
        setLoading(true);
        const { data } = await Api.get<PageResponse<Book>>("/books", {
          signal: controller.signal,
          params: {
            ...getRequestFilters(submittedFilters),
            page: 0,
            size: 24,
            sort: [submittedSort, "id,desc"],
          },
        });

        setBooks(data.content);
      } catch (requestError) {
        if (!controller.signal.aborted) {
          setError(
            getApiErrorMessage(requestError, "Không thể tìm kiếm sách."),
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };

    void fetchBooks();

    return () => {
      controller.abort();
    };
  }, [submittedFilters, submittedSort]);

  const clearFilters = () => {
    const clearedFilters: SearchFilters = {
      title: "",
      authorFullname: "",
      fromPrice: "",
      toPrice: "",
      fromRating: "",
      toRating: "",
      categoryId: "",
      authorId: "",
    };

    setFilters(clearedFilters);
    setSubmittedFilters({ ...clearedFilters });

    setAuthorName("");
    setCategoryName("");

    setSort("createdAt,desc");
    setSubmittedSort("createdAt,desc");

    setError("");
    setLoading(true);

    router.replace("/search", {
      scroll: false,
    });
  };

  const removeScopedFilter = (filter: "author" | "category") => {
    if (filter === "author") {
      updateFilter("authorId", "");
      setAuthorName("");
      return;
    }

    updateFilter("categoryId", "");
    setCategoryName("");
  };

  return (
    <section className="mx-auto max-w-360 px-5 py-12 lg:px-10 lg:py-16">
      {loading && <LoadingOverlay />}

      <div className="border-b border-border pb-8">
        <h1 className="font-serif text-5xl font-bold text-foreground">
          Tìm kiếm sách
        </h1>
      </div>

      <SearchFiltersForm
        filters={filters}
        sort={sort}
        authorName={authorName}
        categoryName={categoryName}
        loading={loading}
        onFilterChangeAction={updateFilter}
        onSortChangeAction={setSort}
        onRemoveScopedFilterAction={removeScopedFilter}
        onSubmitAction={handleSearch}
        onClearAction={clearFilters}
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
      ) : !loading && !error ? (
        <EmptyState title="Không tìm thấy sách phù hợp" />
      ) : null}
    </section>
  );
}
