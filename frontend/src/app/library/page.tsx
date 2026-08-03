"use client";

import { DownloadIcon } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { EmptyState } from "@/components/EmptyState";
import { Header } from "@/components/Header";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { Pagination } from "@/components/Pagination";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { Api, getApiErrorMessage } from "@/lib/api";
import { downloadBookForOffline, getDownloadedBookIds } from "@/lib/offline";
import type { LibraryItem, PageResponse } from "@/lib/types";
import { cn } from "@/lib/utils";

const libraryTypeMap = {
  PURCHASED: "Đã mua",
  SUBSCRIBED: "Gói đọc",
  UPLOADED: "Đã tải lên",
} as const;

interface LibraryCardProps {
  library: LibraryItem;
  downloaded: boolean;
  downloading: boolean;
  downloadDisabled: boolean;
  onDownloadAction: (bookId: number) => void;
}

function LibraryCard({
  library,
  downloaded,
  downloading,
  downloadDisabled,
  onDownloadAction,
}: LibraryCardProps) {
  const authorNames =
    library.authors?.map((author) => author.authorFullname).join(", ") ||
    "Chưa cập nhật tác giả";

  return (
    <article className="group min-w-0">
      <Link href={`/books/${library.bookId}`} className="block">
        <div
          className={cn(
            "relative aspect-2/3 overflow-hidden border border-border bg-muted",
            !downloaded && "grayscale",
          )}
        >
          {library.thumbnail ? (
            <Image
              src={library.thumbnail}
              alt={`Bìa sách ${library.title}`}
              fill
              sizes="(min-width: 1280px) 16vw, (min-width: 1024px) 25vw, (min-width: 640px) 33vw, 50vw"
              className={cn(
                "object-cover transition duration-300 group-hover:scale-[1.025]",
                !downloaded && "opacity-55",
              )}
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

              <p className="font-serif text-2xl leading-tight text-foreground">
                {library.title}
              </p>

              <span className="h-px bg-placeholder" />
            </div>
          )}

          {!downloaded && (
            <div className="absolute inset-0 flex items-end bg-background/35 p-3">
              <span className="border border-border-strong bg-overlay/90 px-3 py-1.5 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {libraryTypeMap[library.type]}
              </span>
            </div>
          )}
        </div>
      </Link>

      <div className={cn("pt-4", !downloaded && "opacity-65")}>
        <Link
          href={`/books/${library.bookId}`}
          className="line-clamp-2 text-xl font-semibold leading-snug text-foreground transition hover:text-link"
        >
          {library.title}
        </Link>

        <p className="mt-1 truncate text-muted-foreground">{authorNames}</p>
        <div className="mt-3 border-t border-border pt-3">
          {downloaded ? (
            <p className="text-sm font-semibold text-link">Đã tải xuống</p>
          ) : (
            <Button
              type="button"
              size="sm"
              variant="outline"
              disabled={downloadDisabled}
              onClick={() => onDownloadAction(library.bookId)}
              className="w-full"
            >
              {downloading ? (
                <Spinner data-icon="inline-start" />
              ) : (
                <DownloadIcon data-icon="inline-start" />
              )}

              {downloading ? "Đang tải..." : "Tải xuống"}
            </Button>
          )}
        </div>
      </div>
    </article>
  );
}

export default function LibraryPage() {
  const router = useRouter();
  const { user, initialized } = useAuth();

  const [libraries, setLibraries] = useState<LibraryItem[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [downloadedBookIds, setDownloadedBookIds] = useState<Set<number>>(
    new Set(),
  );

  const [downloadingBookId, setDownloadingBookId] = useState<number | null>(
    null,
  );

  useEffect(() => {
    if (!initialized || !user) {
      return;
    }

    let active = true;

    const loadDownloadedBooks = async () => {
      try {
        const bookIds = await getDownloadedBookIds(user.username);

        if (active) {
          setDownloadedBookIds(bookIds);
        }
      } catch (storageError) {
        if (active) {
          setError(
            storageError instanceof Error
              ? storageError.message
              : "Không thể đọc dữ liệu offline.",
          );
        }
      }
    };

    void loadDownloadedBooks();

    return () => {
      active = false;
    };
  }, [initialized, user]);

  useEffect(() => {
    if (!initialized) {
      return;
    }

    if (!user) {
      router.replace("/login");
      return;
    }

    const controller = new AbortController();

    const fetchLibraries = async () => {
      try {
        setLoading(true);
        setError("");

        const { data } = await Api.get<PageResponse<LibraryItem>>(
          "/libraries",
          {
            signal: controller.signal,
            params: {
              page: currentPage,
              size: 12,
              sort: ["createdAt,desc", "id,desc"],
            },
          },
        );

        setLibraries(data.content);
        setCurrentPage(data.page.number);
        setTotalPages(data.page.totalPages);
        setTotalElements(data.page.totalElements);
      } catch (requestError) {
        if (!controller.signal.aborted) {
          setError(
            getApiErrorMessage(requestError, "Không thể tải thư viện của bạn."),
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };

    void fetchLibraries();

    return () => {
      controller.abort();
    };
  }, [currentPage, initialized, router, user]);

  const handleDownload = async (bookId: number) => {
    if (!user || downloadingBookId !== null) {
      return;
    }

    try {
      setDownloadingBookId(bookId);
      setError("");

      await downloadBookForOffline(user.username, bookId);

      setDownloadedBookIds((current) => {
        const next = new Set(current);
        next.add(bookId);
        return next;
      });
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          requestError instanceof Error
            ? requestError.message
            : "Không thể tải sách.",
        ),
      );
    } finally {
      setDownloadingBookId(null);
    }
  };

  const handlePageChange = (page: number) => {
    if (page < 0 || page >= totalPages || page === currentPage) {
      return;
    }

    setCurrentPage(page);

    document.getElementById("library-list")?.scrollIntoView({
      behavior: "smooth",
      block: "start",
    });
  };

  return (
    <main className="min-h-screen bg-background">
      {loading && <LoadingOverlay />}

      <Header />

      <section
        id="library-list"
        className="mx-auto max-w-360 scroll-mt-20 px-5 py-12 lg:px-10 lg:py-16"
      >
        <div className="border-b border-border pb-8">
          <h1 className="font-serif text-5xl font-semibold text-foreground">
            Thư viện
          </h1>

          <p className="mt-3 text-muted-foreground">
            {totalElements} cuốn sách trong thư viện
          </p>
        </div>

        {error && (
          <Alert variant="destructive" className="mt-8">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {libraries.length > 0 ? (
          <div className="grid grid-cols-2 gap-x-5 gap-y-11 pt-10 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6">
            {libraries.map((library) => (
              <LibraryCard
                key={library.id}
                library={library}
                downloaded={downloadedBookIds.has(library.bookId)}
                downloading={downloadingBookId === library.bookId}
                downloadDisabled={downloadingBookId !== null}
                onDownloadAction={handleDownload}
              />
            ))}
          </div>
        ) : !loading && !error ? (
          <EmptyState
            className="mt-8"
            title="Thư viện đang trống"
            description="Sách bạn mua hoặc thêm bằng gói đọc sẽ xuất hiện tại đây."
          />
        ) : null}

        {libraries.length > 0 && (
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            disabled={loading}
            onPageChangeAction={handlePageChange}
          />
        )}
      </section>
    </main>
  );
}
