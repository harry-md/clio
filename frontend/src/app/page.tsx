"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useState } from "react";
import { BookCard } from "@/components/BookCard";
import { EmptyState } from "@/components/EmptyState";
import { Header } from "@/components/Header";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { Pagination } from "@/components/Pagination";
import { Rating } from "@/components/Rating";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button, buttonVariants } from "@/components/ui/button";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { Book, PageResponse } from "@/lib/types";
import { cn } from "@/lib/utils";

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

const priceFormatter = new Intl.NumberFormat("en-US", {
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
      setError("");

      try {
        setLoading(true);
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
    <main className="min-h-screen bg-background">
      {loading && <LoadingOverlay />}

      <Header />

      <section className="relative overflow-hidden border-b border-border bg-card">
        {featuredBook?.thumbnail && (
          <Image
            src={featuredBook.thumbnail}
            alt=""
            fill
            sizes="100vw"
            className="object-cover object-center opacity-50"
          />
        )}

        <div
          className="absolute inset-0"
          style={{
            background:
              "linear-gradient(90deg, var(--hero-shade) 2%, var(--hero-overlay-strong) 50%, var(--hero-overlay-soft) 100%)",
          }}
        />

        <div className="relative mx-auto grid min-h-120 max-w-360 items-center gap-12 px-5 py-16 md:grid-cols-[1fr_260px] lg:px-10">
          <div className="max-w-2xl">
            <h1 className="font-serif text-5xl font-semibold leading-tight text-foreground sm:text-6xl lg:text-7xl">
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
                    className={cn(
                      buttonVariants({ size: "lg" }),
                      "min-w-36 text-xl",
                    )}
                  >
                    Xem chi tiết
                  </Link>

                  <span className="border-l border-border-strong pl-5 text-2xl font-semibold text-price">
                    {`${priceFormatter.format(Number(featuredBook.price))} VND`}
                  </span>
                </>
              ) : (
                <>
                  <Link
                    href="/register"
                    className={buttonVariants({ size: "lg" })}
                  >
                    Bắt đầu đọc
                  </Link>

                  <Link
                    href="/login"
                    className={buttonVariants({
                      variant: "outline",
                      size: "lg",
                    })}
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
              <div className="relative aspect-2/3 w-57.5 overflow-hidden border border-border-strong bg-muted">
                <Image
                  src={featuredBook.thumbnail}
                  alt={`Bìa sách ${featuredBook.title}`}
                  fill
                  sizes="300px"
                  className="object-cover"
                />
              </div>
            </Link>
          )}
        </div>
      </section>

      <section
        id="book-list"
        className="mx-auto max-w-360 scroll-mt-20 px-5 py-14 lg:px-10 lg:py-20"
      >
        <div className="flex flex-col justify-between gap-6 border-b border-border pb-7 md:flex-row md:items-end">
          <h2 className="mt-2 text-5xl font-semibold text-foreground">
            Khám phá thư viện
          </h2>

          <div className="flex flex-wrap gap-2">
            {filters.map((filter, index) => {
              const isActive = activeFilter === index;

              return (
                <Button
                  key={filter.label}
                  type="button"
                  variant={isActive ? "secondary" : "outline"}
                  aria-pressed={isActive}
                  onClick={() => {
                    setActiveFilter(index);
                    setCurrentPage(0);
                  }}
                  className={cn(
                    isActive &&
                      "border-ring bg-accent text-accent-foreground hover:bg-accent",
                  )}
                >
                  {filter.label}
                </Button>
              );
            })}
          </div>
        </div>

        {error && (
          <Alert variant="destructive" className="mt-8">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {books.length > 0 ? (
          <div className="grid grid-cols-2 gap-x-5 gap-y-11 pt-10 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6">
            {books.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>
        ) : !loading && !error ? (
          <EmptyState
            className="mt-8"
            title="Chưa tìm thấy sách phù hợp"
            description="Hãy thử một bộ lọc khác."
          />
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

      <footer className="border-t border-border bg-overlay">
        <div className="mx-auto flex max-w-360 flex-col gap-3 px-5 py-8 text-sm text-subtle-foreground sm:flex-row sm:items-center sm:justify-between lg:px-10">
          <p className="text-lg text-secondary-foreground">Clio</p>
          <p>&copy;2026 Clio</p>
        </div>
      </footer>
    </main>
  );
}
