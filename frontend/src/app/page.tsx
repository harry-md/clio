"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { BookCard } from "@/components/BookCard";
import { Header } from "@/components/Header";
import { Rating } from "@/components/Rating";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { Book, PageResponse } from "@/lib/types";
import { Pagination } from "@/components/Pagination";

const filters = [
  {
    label: "Mới phát hành",
    params: {
      sort: ["createdAt,desc", "id,desc"],
    },
  },
  {
    label: "Phổ biến",
    params: {
      sort: ["ratingCount,desc", "id,desc"],
    },
  },
  {
    label: "Đánh giá cao",
    params: {
      sort: ["rating,desc", "id,desc"],
    },
  },
];

const priceFormatter = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
});

export default function HomePage() {
  const [books, setBooks] = useState<Book[]>([]);
  const [activeFilter, setActiveFilter] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    const fetchBooks = async () => {
      setLoading(true);
      setError("");

      try {
        const selectedFilter = filters[activeFilter];

        const { data } = await Api.get<PageResponse<Book>>("/books", {
          signal: controller.signal,
          params: {
            page: currentPage,
            size: 12,
            ...selectedFilter.params,
          },
        });

        setBooks(data.content);
        setTotalPages(data.totalPages);
      } catch (requestError) {
        if (!controller.signal.aborted) {
          setError(
            getApiErrorMessage(requestError, "Không thể tải danh sách sách."),
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
  }, [activeFilter, currentPage]);

  const handlePageChange = (page: number) => {
    if (page < 0 || page >= totalPages || page === currentPage) {
      return;
    }

    setCurrentPage(page);

    document.getElementById("book-list")?.scrollIntoView({
      behavior: "smooth",
      block: "start",
    });
  };
  const featuredBook = books[0];

  return (
    <main className="min-h-screen bg-[#151515]">
      {loading && <LoadingOverlay label="Đang tải danh sách sách..." />}
      <Header />

      <section className="relative overflow-hidden border-b border-[#343432] bg-[#1d1d1b]">
        {featuredBook?.thumbnail && (
          <div
            className="absolute inset-0 bg-cover bg-center opacity-50"
            style={{
              backgroundImage: `url("${featuredBook.thumbnail}")`,
            }}
          />
        )}

        <div className="absolute inset-0 bg-[linear-gradient(90deg,#181818_8%,rgba(24,24,24,.8)_50%,rgba(24,24,24,.25)_100%)]" />
        <div className="relative mx-auto grid min-h-120 max-w-360 items-center gap-12 px-5 py-16 md:grid-cols-[1fr_260px] lg:px-10">
          <div className="max-w-2xl">
            <h1 className="font-serif text-7xl font-semibold leading-tight text-[#f5f2eb] sm:text-5xl lg:text-7xl">
              {featuredBook?.title ?? "Hệ thống đọc và phân phối ebook"}
            </h1>
            <div className="mt-1">
              <Rating
                rating={featuredBook?.rating ?? null}
                count={featuredBook?.ratingCount ?? 0}
              />
            </div>
            <div className="mt-9 flex flex-wrap items-center gap-3">
              {featuredBook ? (
                <>
                  <Link
                    href={`/books/${featuredBook.id}`}
                    className="primary-button min-w-36 text-2xl"
                  >
                    Xem chi tiết
                  </Link>

                  <span className="border-l border-[#4a4945] pl-5 text-2xl font-semibold text-[#e57a3c]">
                    {Number(featuredBook.price) === 0
                      ? "Miễn phí"
                      : priceFormatter.format(Number(featuredBook.price))}
                  </span>
                </>
              ) : (
                <>
                  <Link href="/register" className="primary-button">
                    Bắt đầu đọc
                  </Link>

                  <Link
                    href="/login"
                    className="inline-flex h-12 items-center border border-[#555550] px-5 text-sm font-semibold text-[#e9e7e0] hover:border-[#85857f]"
                  >
                    Đã có tài khoản
                  </Link>
                </>
              )}
            </div>
          </div>

          {featuredBook?.thumbnail && (
            <Link
              href={`/books/${featuredBook.id}`}
              className="hidden justify-self-end md:block"
            >
              <div
                className="aspect-2/3 w-57.5 border border-[#514f49] bg-cover bg-center"
                style={{
                  backgroundImage: `url("${featuredBook.thumbnail}")`,
                }}
              />
            </Link>
          )}
        </div>
      </section>

      <section
        id="book-list"
        className="mx-auto max-w-360 scroll-mt-20 px-5 py-14 lg:px-10 lg:py-20"
      >
        <div className="flex flex-col justify-between gap-6 border-b border-[#343432] pb-7 md:flex-row md:items-end">
          <div>
            <h2 className="mt-2 font-sans text-5xl font-semibold text-[#f0eee8]">
              Khám phá thư viện
            </h2>
          </div>

          <div className="flex flex-wrap gap-2">
            {filters.map((filter, index) => (
              <button
                key={filter.label}
                type="button"
                onClick={() => {
                  setActiveFilter(index);
                  setCurrentPage(0);
                }}
                className={`h-10 border px-4 text-sm font-semibold transition ${
                  activeFilter === index
                    ? "border-[#75a9d3] bg-[#263745] text-white"
                    : "border-[#41413e] text-[#aaa9a4] hover:border-[#6b6a65] hover:text-white"
                }`}
              >
                {filter.label}
              </button>
            ))}
          </div>
        </div>

        {error && (
          <div className="mt-8 border border-[#83483d] bg-[#2b1d1a] p-4 text-sm text-[#e5a394]">
            {error}
          </div>
        )}

        {books.length > 0 ? (
          <div className="grid grid-cols-2 gap-x-5 gap-y-11 pt-10 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6">
            {books.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>
        ) : !loading ? (
          <div className="border-b border-[#343432] py-24 text-center">
            <p className="font-sans text-2xl text-[#d8d6cf]">
              Chưa tìm thấy sách phù hợp
            </p>

            <p className="mt-2 text-sm text-[#85847f]">
              Hãy thử một bộ lọc khác.
            </p>
          </div>
        ) : null}
        {books.length > 0 && (
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            disabled={loading}
            onPageChangeAction={handlePageChange}
          />
        )}
      </section>

      <footer className="border-t border-[#343432] bg-[#111111]">
        <div className="mx-auto flex max-w-360 flex-col gap-3 px-5 py-8 text-sm text-[#777671] sm:flex-row sm:items-center sm:justify-between lg:px-10">
          <p className="font-sans text-lg text-[#c4c2bb]">Clio</p>
          <p>&copy;2026 Clio</p>
        </div>
      </footer>
    </main>
  );
}
