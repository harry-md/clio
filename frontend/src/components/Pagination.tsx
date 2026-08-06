import Link from "next/link";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

type PaginationProps = {
  currentPage: number;
  totalPages: number;
  filterKey?: string;
  basePath?: string;
  toSection?: string;
};

const buildPaginationUrl = (
  basePath: string,
  filterKey: string | undefined,
  page: number,
  toSection = "book-list",
) => {
  const params = new URLSearchParams();

  if (filterKey && filterKey !== "newest") {
    params.set("filter", filterKey);
  }

  if (page > 0) {
    params.set("page", String(page));
  }

  const query = params.toString();

  return `${basePath}${query ? `?${query}` : ""}#${toSection}`;
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

export const Pagination = ({
  currentPage,
  totalPages,
  filterKey,
  basePath = "/",
  toSection,
}: PaginationProps) => {
  if (totalPages <= 1) {
    return null;
  }

  const pageItems = buildPageItems(currentPage, totalPages);

  return (
    <nav
      aria-label="Phân trang"
      className="mt-12 flex flex-wrap items-center justify-center gap-2 border-t border-border pt-8"
    >
      <Link
        href={buildPaginationUrl(
          basePath,
          filterKey,
          Math.max(0, currentPage - 1),
          toSection,
        )}
        aria-disabled={currentPage === 0}
        tabIndex={currentPage === 0 ? -1 : undefined}
        className={cn(
          buttonVariants({ variant: "outline" }),
          currentPage === 0 && "pointer-events-none opacity-50",
        )}
      >
        Trước
      </Link>

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
          <Link
            key={item}
            href={buildPaginationUrl(basePath, filterKey, item)}
            aria-label={`Trang ${item + 1}`}
            aria-current={isActive ? "page" : undefined}
            className={cn(
              buttonVariants({
                variant: isActive ? "secondary" : "outline",
                size: "icon",
              }),
              isActive &&
                "border-ring bg-accent text-accent-foreground hover:bg-accent",
            )}
          >
            {item + 1}
          </Link>
        );
      })}

      <Link
        href={buildPaginationUrl(
          basePath,
          filterKey,
          Math.min(totalPages - 1, currentPage + 1),
        )}
        aria-disabled={currentPage >= totalPages - 1}
        tabIndex={currentPage >= totalPages - 1 ? -1 : undefined}
        className={cn(
          buttonVariants({ variant: "outline" }),
          currentPage >= totalPages - 1 && "pointer-events-none opacity-50",
        )}
      >
        Sau
      </Link>
    </nav>
  );
};
