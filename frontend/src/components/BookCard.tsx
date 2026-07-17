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
        <div className="relative aspect-2/3 overflow-hidden border border-[#343432] bg-[#232321]">
          {book.thumbnail ? (
            <div
              className="absolute inset-0 bg-cover bg-center transition duration-300 group-hover:scale-[1.025] group-hover:opacity-90"
              style={{ backgroundImage: `url("${book.thumbnail}")` }}
            />
          ) : (
            <div className="absolute inset-0 flex flex-col justify-between bg-[linear-gradient(145deg,#31302d,#191918)] p-5">
              <span className="text-xs uppercase tracking-[0.25em] text-[#aaa9a4]">
                Clio edition
              </span>

              <p className="font-serif text-2xl leading-tight text-[#f1efe9]">
                {book.title}
              </p>

              <span className="h-px bg-[#6f6d67]" />
            </div>
          )}

          <span className="absolute right-0 top-0 bg-[#151515]/90 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-widest text-[#d9d7d1]">
            {book.type}
          </span>
        </div>
      </Link>

      <div className="pt-4">
        <Link
          href={`/books/${book.id}`}
          className="line-clamp-2 font-serif text-lg font-semibold leading-snug text-[#f3f1eb] transition hover:text-[#7eb7e8]"
        >
          {book.title}
        </Link>

        <p className="mt-1 truncate text-sm text-[#9c9b96]">{authorNames}</p>

        <div className="mt-3 flex items-center justify-between gap-3 border-t border-[#30302e] pt-3">
          <span className="text-sm font-semibold text-[#e57a3c]">
            {Number(book.price) === 0
              ? "Miễn phí"
              : priceFormatter.format(Number(book.price))}
          </span>

          {book.rating !== null && (
            <span className="text-xs text-[#aaa9a4]">
              <span className="text-[#dfad55]">★</span>{" "}
              {book.rating.toFixed(1)} ({book.ratingCount})
            </span>
          )}
        </div>
      </div>
    </article>
  );
}
