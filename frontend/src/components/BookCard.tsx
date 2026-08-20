import Image from "next/image";
import Link from "next/link";
import type { Book } from "@/lib/types";

interface BookCardProps {
  book: Book;
}

const priceFormatter = new Intl.NumberFormat("en-US", {
  maximumFractionDigits: 0,
});

export const BookCard = ({ book }: BookCardProps) => {
  const authorNames =
    book.authors?.map((author) => author.authorFullname).join(", ") ||
    "Chưa cập nhật tác giả";

  return (
    <Link
      href={`/books/${book.id}`}
      className="group block min-w-0 cursor-pointer"
    >
      <article>
        <div className="relative aspect-2/3 overflow-hidden border border-border bg-muted">
          {book.thumbnail ? (
            <Image
              src={book.thumbnail}
              alt={`Bìa sách ${book.title}`}
              fill
              sizes="(min-width: 1280px) 16vw, (min-width: 1024px) 25vw, (min-width: 640px) 33vw, 50vw"
              className="object-cover group-hover:opacity-70"
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

              <p className="text-3xl leading-tight text-foreground">
                {book.title}
              </p>

              <span className="h-px bg-placeholder" />
            </div>
          )}
        </div>

        <div className="pt-4">
          <h3
            title={book.title}
            className="text-xl line-clamp-2 font-semibold leading-snug text-foreground transition group-hover:text-link"
          >
            {book.title}
          </h3>

          <p className="mt-1 text-sm truncate text-muted-foreground transition group-hover:text-link">
            {authorNames}
          </p>

          <div className="mt-3 font flex items-center justify-between gap-3 border-t border-border pt-3">
            <span className="font-semibold text-price">
              {priceFormatter.format(Number(book.price))}đ
            </span>

            {book.rating !== null && (
              <span className="flex shrink-0 items-center gap-1 whitespace-nowrap text-muted-foreground">
                <Image
                  src="/star.svg"
                  alt=""
                  aria-hidden="true"
                  width={16}
                  height={16}
                  className="size-4 shrink-0"
                />

                <span>
                  {book.rating.toFixed(1)} ({book.ratingCount})
                </span>
              </span>
            )}
          </div>
        </div>
      </article>
    </Link>
  );
};
