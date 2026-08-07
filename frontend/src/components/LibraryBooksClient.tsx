"use client";

import { DownloadIcon } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import { useEffect, useState } from "react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import type { LibraryItem, UserLibraryType } from "@/lib/types";
import { cn } from "@/lib/utils";

const libraryType: Record<UserLibraryType, string> = {
  PURCHASED: "Đã mua",
  SUBSCRIBED: "Gói đọc",
  UPLOADED: "Đã tải lên",
};

interface LibraryCardProps {
  library: LibraryItem;
  downloaded: boolean;
  checkingOfflineData: boolean;
  downloading: boolean;
  downloadDisabled: boolean;
  onDownloadAction: (bookId: number) => void;
}

const LibraryCard = ({
  library,
  downloaded,
  checkingOfflineData,
  downloading,
  downloadDisabled,
  onDownloadAction,
}: LibraryCardProps) => {
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
              <span className="text-xs uppercase tracking-[0.25em] text-muted-foreground" />

              <p className="font-serif text-2xl leading-tight text-foreground">
                {library.title}
              </p>

              <span className="h-px bg-placeholder" />
            </div>
          )}

          {!downloaded && (
            <div className="absolute inset-0 flex items-end bg-background/35 p-3">
              <span className="border border-border-strong bg-overlay/90 px-3 py-1.5 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {libraryType[library.type]}
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
              {checkingOfflineData || downloading ? (
                <Spinner data-icon="inline-start" />
              ) : (
                <DownloadIcon data-icon="inline-start" />
              )}

              {checkingOfflineData
                ? "Đang kiểm tra..."
                : downloading
                  ? "Đang tải..."
                  : "Tải xuống"}
            </Button>
          )}
        </div>
      </div>
    </article>
  );
};

interface LibraryBooksClientProps {
  libraries: LibraryItem[];
}

export const LibraryBooksClient = ({ libraries }: LibraryBooksClientProps) => {
  const { user, initialized } = useAuth();
  const owner = user?.username;

  const [downloadedBookIds, setDownloadedBookIds] = useState<Set<number>>(
    new Set(),
  );

  const [offlineDataReady, setOfflineDataReady] = useState(false);
  const [downloadingBookId, setDownloadingBookId] = useState<number | null>(
    null,
  );
  const [error, setError] = useState("");

  useEffect(() => {
    setDownloadedBookIds(new Set());
    setOfflineDataReady(false);

    if (!initialized || !owner) {
      return;
    }

    let active = true;

    return () => {
      active = false;
    };
  }, [initialized, owner]);

  return (
    <>
      {error && (
        <Alert variant="destructive" className="mt-8">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid grid-cols-2 gap-x-5 gap-y-11 pt-10 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6">
        {libraries.map((library) => (
          <LibraryCard
            key={library.id}
            library={library}
            downloaded={downloadedBookIds.has(library.bookId)}
            checkingOfflineData={!offlineDataReady}
            downloading={downloadingBookId === library.bookId}
            downloadDisabled={
              !owner || !offlineDataReady || downloadingBookId !== null
            }
          />
        ))}
      </div>
    </>
  );
};
