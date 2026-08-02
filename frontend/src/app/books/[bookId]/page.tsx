"use client";

import { ArrowLeftIcon, LibraryIcon, ShoppingCartIcon } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { Header } from "@/components/Header";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { Rating } from "@/components/Rating";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button, buttonVariants } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";
import { useCart } from "@/context/CartContext";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { BookDetail } from "@/lib/types";
import { cn } from "@/lib/utils";

const priceFormatter = new Intl.NumberFormat("en-US", {
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

  const { user } = useAuth();
  const { addBook, hasBook } = useCart();
  const [book, setBook] = useState<BookDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const bookInCart = book ? hasBook(book.id) : false;

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

    void fetchBookDetail();
  }, [bookId]);

  const metadataItems = book
    ? [
        {
          label: "Ngôn ngữ",
          value:
            languageLabels[book.bookInfo?.language] ??
            book.bookInfo?.language ??
            "Chưa cập nhật",
        },
        {
          label: "ISBN",
          value: book.bookInfo?.isbn || "Chưa cập nhật",
        },
        {
          label: "Dung lượng",
          value: formatFileSize(book.bookInfo?.fileSize ?? 0),
        },
        {
          label: "Số trang",
          value: book.bookInfo?.wordCount
            ? Math.ceil(book.bookInfo.wordCount / 300)
            : "Chưa cập nhật",
        },
      ]
    : [];

  return (
    <main className="min-h-screen bg-background">
      <Header />

      <div className="mx-auto max-w-360 px-5 py-8 lg:px-10 lg:py-12">
        <Link
          href="/"
          className={cn(
            buttonVariants({ variant: "link" }),
            "group h-auto gap-2 px-0 text-base",
          )}
        >
          <ArrowLeftIcon
            aria-hidden="true"
            className="size-5 transition-transform group-hover:-translate-x-0.5"
          />
          Quay lại
        </Link>

        {loading && <LoadingOverlay />}

        {!loading && error && (
          <Alert variant="destructive" className="mt-10">
            <AlertDescription className="text-center text-lg">
              {error}
            </AlertDescription>
          </Alert>
        )}

        {!loading && book && (
          <>
            <section className="mt-10 grid gap-10 border-b border-border pb-14 lg:grid-cols-[300px_minmax(0,1fr)] lg:gap-16">
              <div className="relative aspect-2/3 w-full max-w-75 overflow-hidden border border-border-strong bg-muted">
                {book.thumbnail ? (
                  <Image
                    src={book.thumbnail}
                    alt={`Sách ${book.title}`}
                    loading="eager"
                    fill
                    sizes="300px"
                    className="object-cover"
                  />
                ) : (
                  <div
                    className="flex h-full flex-col justify-between p-7"
                    style={{
                      background:
                        "linear-gradient(145deg, var(--cover-from), var(--cover-to))",
                    }}
                  >
                    <span className="text-xs uppercase tracking-[0.25em] text-muted-foreground">
                      Clio edition
                    </span>

                    <h2 className="font-serif text-3xl leading-tight text-foreground">
                      {book.title}
                    </h2>
                  </div>
                )}
              </div>

              <div className="max-w-4xl">
                <h1 className="mt-2 font-serif text-4xl font-semibold leading-tight text-foreground sm:text-5xl lg:text-6xl">
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
                        className="text-link transition hover:text-foreground hover:underline"
                      >
                        {author.authorFullname}
                      </Link>
                    ))
                  ) : (
                    <span className="text-muted-foreground">
                      Chưa cập nhật tác giả
                    </span>
                  )}
                </div>

                <div className="mt-2">
                  <Rating rating={book.rating} count={book.ratingCount} />
                </div>

                <div className="mt-9 flex flex-wrap items-center gap-4 border-y border-border py-6">
                  <span className="text-3xl font-semibold text-price">
                    {`${priceFormatter.format(Number(book.price))} VND`}
                  </span>

                  <div className="flex flex-wrap gap-3">
                    <Button
                      type="button"
                      size="lg"
                      disabled={bookInCart}
                      onClick={() => addBook(book)}
                      className="min-w-52"
                    >
                      <ShoppingCartIcon data-icon="inline-start" />
                      {bookInCart ? "Đã có trong giỏ" : "Thêm sách vào giỏ"}
                    </Button>

                    {user?.isSubscribed && (
                      <Button
                        type="button"
                        size="lg"
                        variant="outline"
                        className="min-w-48"
                      >
                        <LibraryIcon data-icon="inline-start" />
                        Thêm vào thư viện
                      </Button>
                    )}
                  </div>
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
                        className={cn(
                          buttonVariants({
                            variant: "outline",
                            size: "sm",
                          }),
                          "text-muted-foreground hover:text-link",
                        )}
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
                <h2 className="font-serif text-5xl font-semibold text-foreground">
                  Giới thiệu
                </h2>

                <div className="mt-6 max-w-3xl whitespace-pre-line text-2xl leading-9 text-foreground">
                  {book.bookInfo?.description ||
                    "Chưa có phần giới thiệu cho sách này."}
                </div>
              </div>

              <aside className="border-t border-border pt-6 lg:border-l lg:border-t-0 lg:pl-8 lg:pt-0">
                <h3 className="text-3xl font-semibold text-foreground">
                  Thông tin sách
                </h3>

                <dl className="mt-8 space-y-5 text-lg">
                  {metadataItems.map((item) => (
                    <div
                      key={item.label}
                      className="flex justify-between gap-5 border-b border-border pb-4"
                    >
                      <dt className="text-base text-muted-foreground">
                        {item.label}
                      </dt>

                      <dd className="text-right text-lg text-secondary-foreground">
                        {item.value}
                      </dd>
                    </div>
                  ))}
                </dl>
              </aside>
            </section>
          </>
        )}
      </div>
    </main>
  );
}
