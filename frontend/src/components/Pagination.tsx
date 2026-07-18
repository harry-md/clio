"use client";

type PaginationProps = {
  currentPage: number;
  totalPages: number;
  disabled?: boolean;
  onPageChangeAction: (page: number) => void;
};

const buildPageItems = (
  currentPage: number,
  totalPages: number,
): Array<number | string> => {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index);
  }

  const visiblePages = Array.from(
    new Set([0, currentPage - 1, currentPage, currentPage + 1, totalPages - 1]),
  )
    .filter((page) => page >= 0 && page < totalPages)
    .sort((first, second) => first - second);

  const items: Array<number | string> = [];

  for (const page of visiblePages) {
    const previousItem = items.at(-1);

    if (typeof previousItem === "number" && page - previousItem > 1) {
      items.push(`ellipsis-${previousItem}-${page}`);
    }

    items.push(page);
  }

  return items;
};

export function Pagination({
  currentPage,
  totalPages,
  disabled = false,
  onPageChangeAction,
}: PaginationProps) {
  if (totalPages <= 1) {
    return null;
  }

  const pageItems = buildPageItems(currentPage, totalPages);

  return (
    <nav
      aria-label="Phân trang"
      className="mt-12 flex flex-wrap items-center justify-center gap-2 border-t border-[#343432] pt-8"
    >
      <button
        type="button"
        disabled={disabled || currentPage === 0}
        onClick={() => onPageChangeAction(currentPage - 1)}
        className="h-10 border border-[#41413e] px-4 text-sm font-semibold text-[#aaa9a4] transition hover:border-[#75a9d3] hover:text-white disabled:cursor-not-allowed disabled:opacity-40"
      >
        Trước
      </button>

      {pageItems.map((item) => {
        if (typeof item === "string") {
          return (
            <span
              key={item}
              aria-hidden="true"
              className="grid size-10 place-items-center text-[#777671]"
            >
              …
            </span>
          );
        }

        const isActive = item === currentPage;

        return (
          <button
            key={item}
            type="button"
            disabled={disabled}
            aria-label={`Trang ${item + 1}`}
            aria-current={isActive ? "page" : undefined}
            onClick={() => onPageChangeAction(item)}
            className={`size-10 border text-sm font-semibold transition ${
              isActive
                ? "border-[#75a9d3] bg-[#263745] text-white"
                : "border-[#41413e] text-[#aaa9a4] hover:border-[#75a9d3] hover:text-white"
            }`}
          >
            {item + 1}
          </button>
        );
      })}

      <button
        type="button"
        disabled={disabled || currentPage >= totalPages - 1}
        onClick={() => onPageChangeAction(currentPage + 1)}
        className="h-10 border border-[#41413e] px-4 text-sm font-semibold text-[#aaa9a4] transition hover:border-[#75a9d3] hover:text-white disabled:cursor-not-allowed disabled:opacity-40"
      >
        Sau
      </button>
    </nav>
  );
}
