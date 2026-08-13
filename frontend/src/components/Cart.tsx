"use client";

import { ShoppingBagIcon, Trash2Icon } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { Rating } from "@/components/Rating";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button, buttonVariants } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { useCart } from "@/context/CartContext";
import { Api, getApiErrorMessage } from "@/lib/api";
import { cn } from "@/lib/utils";

interface StripeCheckoutResponse {
  orderId: number;
  checkoutUrl: string;
}

const priceFormatter = new Intl.NumberFormat("en-US", {
  maximumFractionDigits: 0,
});

export const Cart = () => {
  const router = useRouter();
  const { user, initialized } = useAuth();
  const { books, removeBook } = useCart();

  const [purchasing, setPurchasing] = useState(false);
  const [error, setError] = useState("");

  const totalPrice = books.reduce(
    (total, book) => total + Number(book.price),
    0,
  );

  const handleCheckout = async () => {
    if (books.length === 0 || purchasing) {
      return;
    }

    if (!user) {
      router.push("/login");
      return;
    }

    try {
      setPurchasing(true);
      setError("");

      const { data } = await Api.post<StripeCheckoutResponse>("/orders", {
        bookIds: books.map((book) => book.id),
      });

      if (!data.checkoutUrl) {
        throw new Error("Checkout URL không hợp lệ");
      }

      window.sessionStorage.setItem(
        "pending-book-checkout",
        JSON.stringify(books.map((book) => book.id)),
      );

      window.location.assign(data.checkoutUrl);
    } catch (error) {
      setError(getApiErrorMessage(error, "Không thể tạo phiên thanh toán."));
      setPurchasing(false);
    }
  };

  return (
    <section className="mx-auto max-w-360 px-5 py-12 lg:px-10 lg:py-16">
      <div className="border-b border-border pb-8">
        <h1 className="mt-3 font-serif text-5xl font-semibold text-foreground">
          Giỏ hàng
        </h1>

        <p className="mt-3 text-muted-foreground">
          {books.length} cuốn sách trong giỏ
        </p>
      </div>

      {error && (
        <Alert variant="destructive" className="mt-8">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {books.length === 0 ? (
        <div className="border-b border-border py-20 text-center">
          <ShoppingBagIcon
            aria-hidden="true"
            className="mx-auto size-10 text-muted-foreground"
          />

          <h2 className="mt-3 text-3xl font-semibold text-foreground">
            Giỏ hàng đang trống
          </h2>
          <Link
            href="/"
            className={cn(
              buttonVariants({
                size: "lg",
              }),
              "mt-7",
            )}
          >
            Trang chủ
          </Link>
        </div>
      ) : (
        <div className="mt-10 grid items-start gap-10 lg:grid-cols-[minmax(0,1fr)_360px]">
          <ul className="self-start divide-y divide-border border-y border-border">
            {books.map((book) => {
              const authorNames =
                book.authors
                  ?.map((author) => author.authorFullname)
                  .join(", ") || "Chưa cập nhật tác giả";

              return (
                <li key={book.id} className="flex items-start gap-6 py-7">
                  <Link
                    href={`/books/${book.id}`}
                    className="relative aspect-2/3 w-28 shrink-0 overflow-hidden border border-border-strong bg-muted sm:w-32"
                  >
                    {book.thumbnail ? (
                      <Image
                        src={book.thumbnail}
                        alt={`Bìa sách ${book.title}`}
                        fill
                        sizes="(min-width: 640px) 128px, 112px"
                        className="object-cover"
                      />
                    ) : (
                      <span className="grid h-full place-items-center px-3 text-center text-sm text-muted-foreground"></span>
                    )}
                  </Link>

                  <div className="min-w-0 flex-1">
                    <Link
                      href={`/books/${book.id}`}
                      className="line-clamp-2 text-2xl font-bold leading-tight text-foreground transition hover:text-link"
                    >
                      {book.title}
                    </Link>

                    <p className="mt-2 truncate text-lg text-muted-foreground">
                      {authorNames}
                    </p>

                    <div className="mt-3">
                      <Rating rating={book.rating} count={book.ratingCount} />
                    </div>

                    <p className="mt-5 text-lg font-semibold text-price">
                      {`${priceFormatter.format(Number(book.price))} VND`}
                    </p>
                  </div>

                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    aria-label={`Xóa ${book.title} khỏi giỏ hàng`}
                    onClick={() => removeBook(book.id)}
                    className="shrink-0"
                  >
                    <Trash2Icon aria-hidden="true" />
                  </Button>
                </li>
              );
            })}
          </ul>

          <aside className="h-fit border border-border-strong bg-card p-7">
            <h2 className="text-4xl font-semibold text-foreground">
              Thanh toán
            </h2>

            <div className="mt-7 flex items-center justify-between border-y border-border py-5">
              <span className="text-lg text-muted-foreground">Tổng cộng</span>
              <span className="text-3xl font-semibold text-price">
                {`${priceFormatter.format(totalPrice)} VND`}
              </span>
            </div>

            <Button
              type="button"
              size="lg"
              disabled={!initialized || purchasing}
              onClick={handleCheckout}
              className="mt-7 w-full"
            >
              {purchasing && <Spinner data-icon="inline-start" />}
              {purchasing ? "Đang chuyển đến thanh toán..." : "Mua sách"}
            </Button>

            {!user && initialized && (
              <p className="mt-4 text-center text-sm text-muted-foreground">
                Bạn cần đăng nhập trước khi mua sách.
              </p>
            )}
          </aside>
        </div>
      )}
    </section>
  );
};
