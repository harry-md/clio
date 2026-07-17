"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { BookCard } from "@/components/BookCard";
import { SiteHeader } from "@/components/Header";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { Book, PageResponse } from "@/lib/types";

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
      fromRating: 4,
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
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadBooks = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const selectedFilter = filters[activeFilter];

      const { data } = await Api.get<PageResponse<Book>>("/books", {
        params: {
          page: 0,
          size: 12,
          ...selectedFilter.params,
          ...(search ? { title: search } : {}),
        },
      });

      setBooks(data.content);
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, "Không thể tải danh sách sách."),
      );
    } finally {
      setLoading(false);
    }
  }, [activeFilter, search]);

  useEffect(() => {
    loadBooks();
  }, [loadBooks]);

  const featuredBook = books[0];
  const featuredAuthors =
    featuredBook?.authors?.map((author) => author.authorFullname).join(", ") ||
    "Clio Editorial";

  return (
    <main className="min-h-screen bg-[#151515]">
      <SiteHeader onSearch={setSearch} />

      <section className="relative overflow-hidden border-b border-[#343432] bg-[#1d1d1b]">
        {featuredBook?.thumbnail && (
          <div
            className="absolute inset-0 bg-cover bg-center opacity-15 blur-[2px]"
            style={{
              backgroundImage: `url("${featuredBook.thumbnail}")`,
            }}
          />
        )}

        <div className="absolute inset-0 bg-[linear-gradient(90deg,#181818_10%,rgba(24,24,24,.94)_42%,rgba(24,24,24,.5)_100%)]" />
        <div className="relative mx-auto grid min-h-120 max-w-360 items-center gap-12 px-5 py-16 md:grid-cols-[1fr_260px] lg:px-10">
          <div className="max-w-2xl">
            <h1 className="font-serif text-4xl font-semibold leading-tight text-[#f5f2eb] sm:text-5xl lg:text-6xl">
              {featuredBook?.title ?? "Hệ thống đọc và phân phối ebook"}
            </h1>
            <div className="mt-9 flex flex-wrap items-center gap-3">
              {featuredBook ? (
                <>
                  <Link
                    href={`/books/${featuredBook.id}`}
                    className="primary-button min-w-36"
                  >
                    Xem sách
                  </Link>

                  <span className="border-l border-[#4a4945] pl-5 text-sm font-semibold text-[#e57a3c]">
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
                    Tôi đã có tài khoản
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

      <section className="mx-auto max-w-360 px-5 py-14 lg:px-10 lg:py-20">
        <div className="flex flex-col justify-between gap-6 border-b border-[#343432] pb-7 md:flex-row md:items-end">
          <div>
            <h2 className="mt-2 font-serif text-3xl font-semibold text-[#f0eee8]">
              {search ? `Kết quả cho “${search}”` : "Khám phá thư viện"}
            </h2>
          </div>

          <div className="flex flex-wrap gap-2">
            {filters.map((filter, index) => (
              <button
                key={filter.label}
                type="button"
                onClick={() => setActiveFilter(index)}
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

        {loading ? (
          <div className="grid grid-cols-2 gap-x-5 gap-y-10 pt-10 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6">
            {Array.from({ length: 12 }).map((_, index) => (
              <div key={index} className="animate-pulse">
                <div className="aspect-2/3 bg-[#262624]" />
                <div className="mt-4 h-4 w-4/5 bg-[#292927]" />
                <div className="mt-3 h-3 w-1/2 bg-[#242422]" />
              </div>
            ))}
          </div>
        ) : books.length > 0 ? (
          <div className="grid grid-cols-2 gap-x-5 gap-y-11 pt-10 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6">
            {books.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>
        ) : (
          <div className="border-b border-[#343432] py-24 text-center">
            <p className="font-sans text-2xl text-[#d8d6cf]">
              Chưa tìm thấy sách phù hợp
            </p>
            <p className="mt-2 text-sm text-[#85847f]">
              Hãy thử một tên sách hoặc bộ lọc khác.
            </p>
          </div>
        )}
      </section>

      <footer className="border-t border-[#343432] bg-[#111111]">
        <div className="mx-auto flex max-w-360 flex-col gap-3 px-5 py-8 text-sm text-[#777671] sm:flex-row sm:items-center sm:justify-between lg:px-10">
          <p className="font-serif text-lg text-[#c4c2bb]">Clio</p>
          <p>&copy;2026 Clio</p>
        </div>
      </footer>
    </main>
  );
}
