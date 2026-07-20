"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { type SubmitEvent, useCallback, useEffect, useState } from "react";
import { BookCard } from "@/components/BookCard";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { Book, PageResponse } from "@/lib/types";

type SearchFilters = {
  title: string;
  authorFullname: string;
  fromPrice: string;
  toPrice: string;
  fromRating: string;
  toRating: string;

  categoryId: string;
  authorId: string;
};

const ratingOptions = [0, 1, 2, 3, 4, 5];

const inputClassName =
  "h-11 w-full border border-[#41413e] bg-[#1d1d1c] px-3 text-sm text-white outline-none placeholder:text-[#777671] focus:border-[#6d9fc9]";

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
  const [totalElements, setTotalElements] = useState(0);
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

    const loadBooks = async () => {
      setLoading(true);
      setError("");

      try {
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
        setTotalElements(data.totalElements);
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

    void loadBooks();

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

    setSubmittedFilters({
      ...clearedFilters,
    });

    setAuthorName("");
    setCategoryName("");

    setSort("createdAt,desc");
    setSubmittedSort("createdAt,desc");

    setError("");
    setLoading(true);

    router.replace("/search?sort=createdAt%2Cdesc", {
      scroll: false,
    });
  };

  return (
    <section className="mx-auto max-w-360 px-5 py-12 lg:px-10 lg:py-16">
      {loading && <LoadingOverlay label="Đang tìm kiếm sách..." />}
      <div className="border-b border-[#343432] pb-8">
        <h1 className="mt-2 font-serif text-5xl font-bold text-[#f0eee8]">
          Tìm kiếm sách
        </h1>
      </div>

      {(filters.authorId || filters.categoryId) && (
        <div className="flex flex-wrap gap-2 border-b border-[#343432] py-5">
          {filters.authorId && (
            <button
              type="button"
              onClick={() => {
                updateFilter("authorId", "");
                setAuthorName("");
              }}
              className="border border-[#537b9c] bg-[#202f3a] px-3 py-2 text-sm text-[#9ac8eb] hover:border-[#81b3da]"
            >
              Tác giả: {authorName || `#${filters.authorId}`} ×
            </button>
          )}

          {filters.categoryId && (
            <button
              type="button"
              onClick={() => {
                updateFilter("categoryId", "");
                setCategoryName("");
              }}
              className="border border-[#665c43] bg-[#2d291f] px-3 py-2 text-sm text-[#d5bd83] hover:border-[#b69a5c]"
            >
              Danh mục: {categoryName || `#${filters.categoryId}`} ×
            </button>
          )}
        </div>
      )}

      <form
        onSubmit={handleSearch}
        className="grid gap-5 border-b border-[#343432] py-8 md:grid-cols-2 xl:grid-cols-4"
      >
        <label className="block md:col-span-2">
          <span className="mb-2 block text-sm font-medium text-[#b8b6b0]">
            Tên sách
          </span>

          <input
            type="text"
            value={filters.title}
            onChange={(event) => updateFilter("title", event.target.value)}
            placeholder="Nhập tên sách..."
            className={inputClassName}
          />
        </label>

        <label className="block md:col-span-2">
          <span className="mb-2 block text-sm font-medium text-[#b8b6b0]">
            Tên tác giả
          </span>

          <input
            type="text"
            value={filters.authorFullname}
            onChange={(event) =>
              updateFilter("authorFullname", event.target.value)
            }
            placeholder="Nhập tên tác giả..."
            className={inputClassName}
          />
        </label>

        <div className="md:col-span-2">
          <span className="mb-2 block text-sm font-medium text-[#b8b6b0]">
            Khoảng giá
          </span>

          <div className="grid grid-cols-2 gap-3">
            <input
              type="number"
              min="0"
              step="0.01"
              value={filters.fromPrice}
              onChange={(event) =>
                updateFilter("fromPrice", event.target.value)
              }
              placeholder="Giá thấp nhất"
              aria-label="Giá thấp nhất"
              className={inputClassName}
            />

            <input
              type="number"
              min="0"
              step="0.01"
              value={filters.toPrice}
              onChange={(event) => updateFilter("toPrice", event.target.value)}
              placeholder="Giá cao nhất"
              aria-label="Giá cao nhất"
              className={inputClassName}
            />
          </div>
        </div>

        <div className="md:col-span-2">
          <span className="mb-2 block text-sm font-medium text-[#b8b6b0]">
            Khoảng đánh giá
          </span>

          <div className="grid grid-cols-2 gap-3">
            <select
              value={filters.fromRating}
              onChange={(event) =>
                updateFilter("fromRating", event.target.value)
              }
              aria-label="Điểm thấp nhất"
              className={inputClassName}
            >
              <option value="">Từ điểm</option>

              {ratingOptions.map((rating) => (
                <option key={rating} value={rating}>
                  {rating} sao
                </option>
              ))}
            </select>

            <select
              value={filters.toRating}
              onChange={(event) => updateFilter("toRating", event.target.value)}
              aria-label="Điểm cao nhất"
              className={inputClassName}
            >
              <option value="">Đến điểm</option>

              {ratingOptions.map((rating) => (
                <option key={rating} value={rating}>
                  {rating} sao
                </option>
              ))}
            </select>
          </div>
        </div>

        <label className="block md:col-span-2">
          <span className="mb-2 block text-sm font-medium text-[#b8b6b0]">
            Sắp xếp
          </span>

          <select
            value={sort}
            onChange={(event) => setSort(event.target.value)}
            className={inputClassName}
          >
            <option value="createdAt,desc">Mới phát hành</option>
            <option value="ratingCount,desc">Phổ biến nhất</option>
            <option value="rating,desc">Đánh giá cao nhất</option>
            <option value="price,asc">Giá thấp đến cao</option>
            <option value="price,desc">Giá cao đến thấp</option>
            <option value="title,asc">Tên sách A–Z</option>
          </select>
        </label>

        <div className="flex items-end gap-3 md:col-span-2">
          <button
            type="submit"
            disabled={loading}
            className="h-11 bg-[#e06f32] px-6 text-sm font-bold text-[#161616] transition hover:bg-[#f08243] disabled:cursor-not-allowed disabled:opacity-60"
          >
            Tìm kiếm
          </button>

          <button
            type="button"
            onClick={clearFilters}
            className="h-11 border border-[#555550] px-5 text-sm font-semibold text-[#aaa9a4] transition hover:border-[#85857f] hover:text-white"
          >
            Xóa bộ lọc
          </button>
        </div>
      </form>

      <div className="flex items-center justify-between py-7">
        <h2 className="font-sans text-2xl font-semibold text-[#f0eee8]">
          Kết quả tìm kiếm
        </h2>

        {!loading && !error && (
          <span className="text-sm text-[#85847f]">{totalElements} sách</span>
        )}
      </div>

      {error && (
        <div
          role="alert"
          className="border border-[#83483d] bg-[#2b1d1a] p-4 text-sm text-[#e5a394]"
        >
          {error}
        </div>
      )}

      {books.length > 0 ? (
        <div className="grid grid-cols-2 gap-x-5 gap-y-11 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6">
          {books.map((book) => (
            <BookCard key={book.id} book={book} />
          ))}
        </div>
      ) : !loading ? (
        <div className="border-y border-[#343432] py-20 text-center">
          <p className="text-xl text-[#d8d6cf]">Không tìm thấy sách phù hợp</p>
        </div>
      ) : null}
    </section>
  );
}
