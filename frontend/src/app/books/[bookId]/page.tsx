"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { SiteHeader } from "@/components/Header";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { BookDetail } from "@/lib/types";

const priceFormatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
});

const languageLabels: Record<string, string> = {
  VI: "Tiếng Việt",
  EN: "Tiếng Anh",
  FR: "Tiếng Pháp",
  RU: "Tiếng Nga",
  DE: "Tiếng Đức",
  JA: "Tiếng Nhật",
  ZH: "Tiếng Trung",
  KO: "Tiếng Hàn",
  ES: "Tiếng Tây Ban Nha",
};

const Rating = ({
  rating,
  count,
}: {
  rating: number | null;
  count: number;
}) => {
  if (rating === null || count === 0) {
    return <span className="text-sm text-[#9a9993]">Chưa có đánh giá</span>;
  }

  const value = Math.max(0, Math.min(5, Math.round(rating)));

  return (
    <div className="flex items-center gap-3">
      <span className="text-lg tracking-[0.15em] text-[#dfad55]">
        {"★".repeat(value)}
        <span className="text-[#55544f]">{"☆".repeat(5 - value)}</span>
      </span>

      <span className="text-sm text-[#9a9993]">
        {rating.toFixed(1)} ({count} đánh giá)
      </span>
    </div>
  );
};

const formatFileSize = (bytes: number) => {
  if (!bytes) {
    return "Chưa cập nhật";
  }

  if (bytes < 1024 * 1024) {
    return `${Math.round(bytes / 1024)} KB`;
  }

  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

export default function BookDetailPage() {
  const params = useParams<{ bookId: string }>();
  const bookId = params.bookId;

  const [book, setBook] = useState<BookDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!bookId) {
      return;
    }

    const loadBook = async () => {
      setLoading(true);
      setError("");

      try {
        const { data } = await Api.get<BookDetail>(`/books/${bookId}`);
        setBook(data);
      } catch (requestError) {
        setError(
          getApiErrorMessage(requestError, "Không thể tải thông tin sách."),
        );
      } finally {
        setLoading(false);
      }
    };

    loadBook();
  }, [bookId]);

  return (
    <main className="min-h-screen bg-[#151515]">
      <SiteHeader />

      <div className="mx-auto max-w-360 px-5 py-8 lg:px-10 lg:py-12">
        <Link
          href="/"
          className="inline-flex items-center gap-2 text-base font-semibold text-[#81b3da] hover:text-white"
        >
          <span aria-hidden="true">←</span>
          Quay lại
        </Link>

        {loading && (
          <div className="mt-10 grid animate-pulse gap-12 lg:grid-cols-[300px_1fr]">
            <div className="aspect-2/3 bg-[#262624]" />
            <div>
              <div className="h-12 w-3/4 bg-[#292927]" />
              <div className="mt-5 h-5 w-1/3 bg-[#242422]" />
              <div className="mt-10 h-32 bg-[#242422]" />
            </div>
          </div>
        )}

        {!loading && error && (
          <div
            role="alert"
            className="mt-10 border border-[#83483d] bg-[#2b1d1a] p-5 text-sm text-[#e5a394]"
          >
            {error}
          </div>
        )}

        {!loading && book && (
          <>
            <section className="mt-10 grid gap-10 border-b border-[#343432] pb-14 lg:grid-cols-[300px_minmax(0,1fr)] lg:gap-16">
              <div
                className="relative aspect-2/3 w-full max-w-75 border border-[#514f49] bg-[#242422] bg-cover bg-center"
                style={
                  book.thumbnail
                    ? {
                        backgroundImage: `url("${book.thumbnail}")`,
                      }
                    : undefined
                }
              >
                {!book.thumbnail && (
                  <div className="flex h-full flex-col justify-between p-7">
                    <span className="text-xs uppercase tracking-[0.25em] text-[#aaa9a4]">
                      Clio edition
                    </span>

                    <h2 className="font-serif text-3xl leading-tight text-[#f1efe9]">
                      {book.title}
                    </h2>
                  </div>
                )}
              </div>

              <div className="max-w-4xl">
                <h1 className="mt-5 font-serif text-5xl font-semibold leading-tight text-[#f3f0e9] sm:text-6xl lg:text-7xl">
                  {book.title}
                </h1>
                <div className="flex flex-wrap gap-2 text-lg">
                  {book.authors?.length > 0 ? (
                    book.authors.map((author) => (
                      <Link
                        key={author.authorId}
                        href={`/authors/${author.authorId}`}
                        className="text-[#81b3da] transition hover:text-white hover:underline"
                      >
                        {author.authorFullname}
                      </Link>
                    ))
                  ) : (
                    <span className="text-[#aaa9a4]">
                      Chưa cập nhật tác giả
                    </span>
                  )}
                </div>

                <div className="mt-7">
                  <Rating rating={book.rating} count={book.ratingCount} />
                </div>

                <div className="mt-9 flex flex-wrap items-center gap-4 border-y border-[#343432] py-6">
                  <span className="font-sans text-3xl font-semibold text-[#e57a3c]">
                    {Number(book.price) === 0
                      ? "Miễn phí"
                      : priceFormatter.format(Number(book.price))}
                  </span>

                  <button
                    type="button"
                    disabled
                    className="primary-button min-w-40 opacity-60"
                  >
                    Mua sách
                  </button>
                </div>

                {book.categories?.length > 0 && (
                  <div className="mt-8 flex flex-wrap gap-3">
                    {book.categories.map((category) => (
                      <Link
                        key={category.id}
                        href={`/categories/${category.id}`}
                        className="border border-[#4a4945] px-3 py-1.5 text-1xl text-[#b7b5ae] transition hover:border-[#81b3da] hover:text-[#81b3da]"
                      >
                        {category.name}
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            </section>

            <section className="grid gap-12 py-14 lg:grid-cols-[minmax(0,1fr)_300px]">
              <div>
                <h2 className="font-serif text-5xl font-semibold text-[#f0eee8]">
                  Giới thiệu
                </h2>

                <div className="mt-6 max-w-3xl whitespace-pre-line text-2xl leading-9 text-[#f0eee8]">
                  {book.bookInfo?.description ||
                    "Chưa có phần giới thiệu cho sách này."}
                </div>
              </div>

              <aside className="border-t border-[#343432] pt-6 lg:border-l lg:border-t-0 lg:pl-8 lg:pt-0">
                <h3 className="font-sans text-3xl font-semibold text-[#f0eee8]">
                  Thông tin sách
                </h3>

                <dl className="mt-8 space-y-5 text-lg">
                  <div className="flex justify-between gap-5 border-b border-[#2f2f2d] pb-4">
                    <dt className="text-base text-[#8d8c86]">Ngôn ngữ</dt>

                    <dd className="text-right text-lg text-[#d0cec7]">
                      {languageLabels[book.bookInfo?.language] ??
                        book.bookInfo?.language ??
                        "Chưa cập nhật"}
                    </dd>
                  </div>

                  <div className="flex justify-between gap-5 border-b border-[#2f2f2d] pb-4">
                    <dt className="text-base text-[#8d8c86]">ISBN</dt>

                    <dd className="text-right text-lg text-[#d0cec7]">
                      {book.bookInfo?.isbn || "Chưa cập nhật"}
                    </dd>
                  </div>

                  <div className="flex justify-between gap-5 border-b border-[#2f2f2d] pb-4">
                    <dt className="text-base text-[#8d8c86]">Dung lượng</dt>

                    <dd className="text-right text-lg text-[#d0cec7]">
                      {formatFileSize(book.bookInfo?.fileSize ?? 0)}
                    </dd>
                  </div>

                  <div className="flex justify-between gap-5 border-b border-[#2f2f2d] pb-4">
                    <dt className="text-base text-[#8d8c86]">Số trang</dt>

                    <dd className="text-right text-lg text-[#d0cec7]">
                      {book.bookInfo?.wordCount
                        ? Math.ceil(book.bookInfo.wordCount / 300)
                        : "Chưa cập nhật"}
                    </dd>
                  </div>
                </dl>
              </aside>
            </section>
          </>
        )}
      </div>
    </main>
  );
}
