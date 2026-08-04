import Image from "next/image";
import Link from "next/link";
import { BookCard } from "@/components/BookCard";
import { EmptyState } from "@/components/EmptyState";
import { Header } from "@/components/Header";
import { Pagination } from "@/components/Pagination";
import { Rating } from "@/components/Rating";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { buttonVariants } from "@/components/ui/button";
import type { Book, PageResponse } from "@/lib/types";
import { cn } from "@/lib/utils";

const filters = [
  {
    key: "newest",
    label: "Mới phát hành",
    params: {
      sort: ["createdAt,desc", "id,desc"],
    },
  },
  {
    key: "popular",
    label: "Phổ biến",
    params: {
      sort: ["ratingCount,desc", "id,desc"],
    },
  },
  {
    key: "rating",
    label: "Đánh giá cao",
    params: {
      sort: ["rating,desc", "id,desc"],
    },
  },
] as const;

const getFilter = (filterKey: string | undefined) => {
  return filters.find((filter) => filter.key === filterKey) ?? filters[0];
};

const parsePage = (value: string | undefined) => {
  const page = Number(value);

  if (!Number.isInteger(page) || page < 0) {
    return 0;
  }

  return page;
};

const buildHomeUrl = (filterKey: string, page: number) => {
  const params = new URLSearchParams();

  if (filterKey !== "newest") {
    params.set("filter", filterKey);
  }

  if (page > 0) {
    params.set("page", String(page));
  }

  const query = params.toString();

  return `/${query ? `?${query}` : ""}#book-list`;
};

const priceFormatter = new Intl.NumberFormat("en-US", {
  maximumFractionDigits: 0,
});

const fetchBooks = async (
  page: number,
  sortParams: readonly string[],
): Promise<PageResponse<Book>> => {
  const params = new URLSearchParams();
  params.append("page", String(page));
  params.append("size", "12");

  sortParams.forEach((s) => {
    params.append("sort", s);
  });

  const url = `${process.env.NEXT_PUBLIC_API_URL}/books?${params.toString()}`;
  const res = await fetch(url, {
    next: { revalidate: 3600 },
  });

  if (!res.ok) {
    throw new Error(`Lỗi tải danh sách sách: ${res.status}`);
  }

  const json = await res.json();
  return json.data || json;
};

const HomePage = async ({
  searchParams,
}: {
  searchParams: Promise<{
    filter?: string;
    page?: string;
  }>;
}) => {
  const params = await searchParams;

  const selectedFilter = getFilter(params.filter);
  const currentPage = parsePage(params.page);

  let books: Book[] = [];
  let totalPages = 0;
  let error = "";

  try {
    const data = await fetchBooks(currentPage, selectedFilter.params.sort);
    books = data.content;
    totalPages = data.page.totalPages;
  } catch (requestError: any) {
    error = requestError.message || "Không thể tải danh sách sách.";
  }

  const featuredBook = books[0];

  return (
    <main className="min-h-screen bg-background">
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
            <h1 className="font-serif text-4xl font-semibold leading-tight text-foreground lg:text-6xl">
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
                    className={buttonVariants({ size: "lg" })}
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
          <h2 className="mt-2 text-3xl font-semibold text-foreground">
            Khám phá sách
          </h2>

          <div className="flex flex-wrap gap-2">
            {filters.map((filter) => {
              const isActive = filter.key === selectedFilter.key;

              return (
                <Link
                  key={filter.key}
                  href={buildHomeUrl(filter.key, 0)}
                  className={cn(
                    buttonVariants({
                      variant: isActive ? "secondary" : "outline",
                    }),
                    isActive &&
                      "border-ring bg-accent text-accent-foreground hover:bg-accent",
                  )}
                  aria-current={isActive ? "page" : undefined}
                >
                  {filter.label}
                </Link>
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
        ) : !error ? (
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
            filterKey={selectedFilter.key}
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
};
export default HomePage;
