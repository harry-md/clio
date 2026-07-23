"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { Header } from "@/components/Header";
import { Rating } from "@/components/Rating";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { BookDetail } from "@/lib/types";
import { LoadingOverlay } from "@/components/LoadingOverlay";

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

    const fetchBookDetail = async () => {
      try {
        setLoading(true);
        setError("");
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

    fetchBookDetail();
  }, [bookId]);

  return (
    <main className="min-h-screen bg-[#151515]">
      <Header />

      <div className="mx-auto max-w-360 px-5 py-8 lg:px-10 lg:py-12">
        <Link
          href="/"
          className="group inline-flex items-center gap-2 text-base text-[#81b3da] hover:text-white"
        >
          <span>
            <svg
              role="img"
              aria-hidden="true"
              className="w-5 h-5 transition-colors text-[#81b3da] group-hover:text-white"
              viewBox="0 0 1024 1024"
              xmlns="http://www.w3.org/2000/svg"
              fill="currentColor"
            >
              <path
                fill="currentColor"
                d="M224 480h640a32 32 0 1 1 0 64H224a32 32 0 0 1 0-64z"
              />
              <path
                fill="currentColor"
                d="m237.248 512 265.408 265.344a32 32 0 0 1-45.312 45.312l-288-288a32 32 0 0 1 0-45.312l288-288a32 32 0 1 1 45.312 45.312L237.248 512z"
              />
            </svg>
          </span>
          Quay lại
        </Link>

        {loading && <LoadingOverlay label="Đang tải thông tin sách..." />}

        {!loading && error && (
          <div
            role="alert"
            className="mt-10 border border-[#2b1d1a] bg-[#2b1d1a] p-5 text-[#e5a394] text-lg text-center"
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
                <h1 className="mt-2 font-serif text-4xl font-semibold leading-tight text-[#f3f0e9] sm:text-5xl lg:text-6xl">
                  {book.title}
                </h1>
                <div className="flex flex-wrap gap-2 text-lg">
                  {book.authors?.length > 0 ? (
                    book.authors.map((author) => (
                      <Link
                        key={author.authorId}
                        href={{
                          pathname: "/search",
                          query: {
                            authorId: String(author.authorId),
                            authorName: author.authorFullname,
                          },
                        }}
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

                <div className="mt-2">
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
                    className="primary-button min-w-40 opacity-60 text-xl!"
                  >
                    Mua sách
                  </button>
                </div>

                {book.categories?.length > 0 && (
                  <div className="mt-8 flex flex-wrap gap-3">
                    {book.categories.map((category) => (
                      <Link
                        key={category.id}
                        href={{
                          pathname: "/search",
                          query: {
                            categoryId: String(category.id),
                            categoryName: category.name,
                          },
                        }}
                        className="border border-[#4a4945] px-3 py-1.5 text-xl text-[#b7b5ae] transition hover:border-[#81b3da] hover:text-[#81b3da]"
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
