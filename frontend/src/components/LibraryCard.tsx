import { DownloadIcon } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import type { LibraryItem, UserLibraryType } from "@/lib/types";
import { cn } from "@/lib/utils";
import { Spinner } from "./Spinner";
import { Button } from "./ui/button";

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
    <Link
      href={`/read/${library.bookId}`}
      className="group block min-w-0 cursor-pointer"
    >
      <article>
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
              className={cn("object-cover", !downloaded && "opacity-55")}
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

              <p className="text-2xl leading-tight text-foreground">
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

        <div className={cn("pt-4", !downloaded && "opacity-65")}>
          <span className="text-lg transition-colors group-hover:text-link">
            {library.title}
          </span>

          <p className="mt-1 truncate text-muted-foreground transition-colors group-hover:text-link">
            {authorNames}
          </p>

          <div className="mt-3 border-t border-border pt-3">
            {!downloaded && (
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
    </Link>
  );
};

export default LibraryCard;
