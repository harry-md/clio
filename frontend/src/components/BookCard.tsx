import Link from "next/link";
import type { Book } from "@/lib/types";

type BookCardProps = {
  book: Book;
};

const priceFormatter = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
});

export function BookCard({ book }: BookCardProps) {
  const authorNames =
    book.authors?.map((author) => author.authorFullname).join(", ") ||
    "Chưa cập nhật tác giả";

  return (
    <article className="group min-w-0">
      <Link href={`/books/${book.id}`} className="block">
        <div className="relative aspect-2/3 overflow-hidden border border-border bg-muted">
          {book.thumbnail ? (
            <div
              className="absolute inset-0 bg-cover bg-center transition duration-300 group-hover:scale-[1.025] group-hover:opacity-90"
              style={{ backgroundImage: `url("${book.thumbnail}")` }}
            />
          ) : (
            <div
              className="absolute inset-0 flex flex-col justify-between p-5"
              style={{
                background:
                  "linear-gradient(145deg, var(--cover-from), var(--cover-to))",
              }}
            >
              <span className="text-xs uppercase tracking-[0.25em] text-muted-foreground">
                Clio edition
              </span>

              <p className="font-sans text-2xl leading-tight text-foreground">
                {book.title}
              </p>

              <span className="h-px bg-placeholder" />
            </div>
          )}
        </div>
      </Link>

      <div className="pt-4">
        <Link
          href={`/books/${book.id}`}
          className="line-clamp-2 font-sans text-xl font-semibold leading-snug text-foreground transition hover:text-link"
        >
          {book.title}
        </Link>

        <p className="mt-1 truncate text-base text-muted-foreground">
          {authorNames}
        </p>

        <div className="mt-3 flex items-center justify-between gap-3 border-t border-border pt-3">
          <span className="font-semibold text-price">
            {Number(book.price) === 0
              ? "Miễn phí"
              : priceFormatter.format(Number(book.price))}
          </span>

          {book.rating !== null && (
            <span className="text-muted-foreground">
              <span className="text-rating">★</span> {book.rating.toFixed(1)} (
              {book.ratingCount})
            </span>
          )}
        </div>
      </div>
    </article>
  );
}
