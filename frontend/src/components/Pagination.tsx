"use client";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

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
      className="mt-12 flex flex-wrap items-center justify-center gap-2 border-t border-border pt-8"
    >
      <Button
        type="button"
        variant="outline"
        disabled={disabled || currentPage === 0}
        onClick={() => onPageChangeAction(currentPage - 1)}
      >
        Trước
      </Button>

      {pageItems.map((item) => {
        if (typeof item === "string") {
          return (
            <span
              key={item}
              aria-hidden="true"
              className="grid size-10 place-items-center text-subtle-foreground"
            >
              …
            </span>
          );
        }

        const isActive = item === currentPage;

        return (
          <Button
            key={item}
            type="button"
            size="icon"
            variant={isActive ? "secondary" : "outline"}
            disabled={disabled}
            aria-label={`Trang ${item + 1}`}
            aria-current={isActive ? "page" : undefined}
            onClick={() => onPageChangeAction(item)}
            className={cn(
              isActive &&
                "border-ring bg-accent text-accent-foreground hover:bg-accent",
            )}
          >
            {item + 1}
          </Button>
        );
      })}

      <Button
        type="button"
        variant="outline"
        disabled={disabled || currentPage >= totalPages - 1}
        onClick={() => onPageChangeAction(currentPage + 1)}
      >
        Sau
      </Button>
    </nav>
  );
}
