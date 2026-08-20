import { DownloadIcon } from "lucide-react";
import Image from "next/image";
import type { LibraryItem, UserLibraryType } from "@/lib/types";
import { cn } from "@/lib/utils";
import { Button } from "./ui/button";

const libraryType: Record<UserLibraryType, string> = {
  PURCHASED: "ĐÃ MUA",
  SUBSCRIBED: "GÓI ĐỌC",
  UPLOADED: "ĐÃ TẢI LÊN",
};

export type DownloadStatus = "DOWNLOADED" | "DOWNLOADING" | "READY";

interface LibraryCardProps {
  library: LibraryItem;
  downloadStatus: DownloadStatus;
  onDownloadAction: (bookId: number) => void;
}

const LibraryCard = ({
  library,
  downloadStatus,
  onDownloadAction,
}: LibraryCardProps) => {
  const downloaded = downloadStatus === "DOWNLOADED";
  const downloading = downloadStatus === "DOWNLOADING";

  const authorNames =
    library.authors?.map((author) => author.authorFullname).join(", ") ||
    "Chưa cập nhật tác giả";

  return (
    <article className="group min-w-0">
      <a
        href={`/read?bookId=${library.bookId}`}
        className="group block min-w-0 cursor-pointer"
      >
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
              <span className="bg-overlay px-3 py-1.5 text-xs">
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
        </div>
      </a>

      {!downloaded && (
        <div className="mt-3 border-t border-border pt-3">
          <Button
            type="button"
            size="sm"
            variant="outline"
            disabled={downloading}
            onClick={(event) => {
              event.preventDefault();
              event.stopPropagation();
              onDownloadAction(library.bookId);
            }}
            className="w-full"
          >
            <DownloadIcon data-icon="inline-start" />
            Tải xuống
          </Button>
        </div>
      )}
    </article>
  );
};

export default LibraryCard;
