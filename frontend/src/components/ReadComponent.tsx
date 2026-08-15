"use client";

import type { Book as EpubBook, Rendition } from "epubjs";
import { ArrowLeft, ChevronLeft, ChevronRight } from "lucide-react";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { BOOK_STORE, decryptFile, get, verifyLicense } from "@/lib/offline";

interface ReadComponentProps {
  bookId: number;
}

type ReaderPhase = "loading" | "ready" | "error";

export const ReadComponent = ({ bookId }: ReadComponentProps) => {
  const { user, initialized } = useAuth();
  const userId = user?.id;

  const readerElementRef = useRef<HTMLDivElement>(null);
  const renditionRef = useRef<Rendition | null>(null);

  const [phase, setPhase] = useState<ReaderPhase>("loading");

  const [message, setMessage] = useState("Đang mở sách...");

  useEffect(() => {
    if (!initialized) {
      return;
    }

    if (userId === undefined) {
      setPhase("error");
      setMessage("Bạn cần đăng nhập để đọc sách.");
      return;
    }

    let disposed = false;
    let epubBook: EpubBook | null = null;

    const openBook = async () => {
      try {
        setPhase("loading");
        setMessage("Đang lấy dữ liệu sách...");

        const bookData = await get(BOOK_STORE, [userId, bookId]);

        if (!bookData) {
          throw new Error("Sách chưa được tải xuống trên trình duyệt này.");
        }

        setMessage("Đang xác minh license...");

        const license = await verifyLicense(bookData.license, userId, bookId);

        setMessage("Đang giải mã sách...");

        const decryptedEpub = await decryptFile(
          userId,
          license.wrappedContentKey,
          bookData.encryptedFile,
        );

        if (disposed || !readerElementRef.current) {
          return;
        }

        setMessage("Đang khởi tạo trình đọc...");

        const { default: ePub } = await import("epubjs");

        if (disposed || !readerElementRef.current) {
          return;
        }

        epubBook = ePub(decryptedEpub);

        const rendition = epubBook.renderTo(readerElementRef.current, {
          width: "100%",
          height: "100%",
          flow: "paginated",
          spread: "none",
          allowScriptedContent: false,
        });

        renditionRef.current = rendition;

        await rendition.display();

        if (!disposed) {
          setPhase("ready");
          setMessage("");
        }
      } catch (error: unknown) {
        if (disposed) {
          return;
        }

        epubBook?.destroy();
        epubBook = null;
        renditionRef.current = null;

        setPhase("error");
        setMessage(
          error instanceof Error ? error.message : "Không thể mở sách.",
        );
      }
    };

    void openBook();

    return () => {
      disposed = true;
      epubBook?.destroy();
      renditionRef.current = null;
      readerElementRef.current?.replaceChildren();
    };
  }, [bookId, initialized, userId]);

  return (
    <section className="mx-auto flex h-[calc(100vh-5rem)] max-w-360 flex-col gap-3 px-4 py-4 lg:px-8">
      <div className="flex items-center justify-between gap-4 border border-border bg-card px-4 py-3">
        <Link
          href="/library"
          className="inline-flex items-center gap-2 text-sm font-semibold text-muted-foreground transition hover:text-foreground"
        >
          <ArrowLeft className="size-4" />
          Thư viện
        </Link>
      </div>

      <div className="relative min-h-0 flex-1 overflow-hidden border border-border bg-white">
        <div ref={readerElementRef} className="h-full w-full" />

        {phase === "loading" && (
          <div className="absolute inset-0 flex flex-col items-center justify-center gap-3 bg-background/95 text-foreground">
            <Spinner />
            <p className="text-sm text-muted-foreground">{message}</p>
          </div>
        )}

        {phase === "error" && (
          <div className="absolute inset-0 flex flex-col items-center justify-center gap-4 bg-background px-6 text-center">
            <p className="font-serif text-2xl font-semibold text-foreground">
              Không thể mở sách
            </p>

            <p className="max-w-xl text-muted-foreground">{message}</p>

            <Link
              href="/library"
              className="text-sm font-semibold text-link hover:underline"
            >
              Quay lại thư viện
            </Link>
          </div>
        )}
      </div>

      <div className="flex items-center justify-center gap-3 border border-border bg-card px-4 py-3">
        <Button
          type="button"
          variant="outline"
          disabled={phase !== "ready"}
          onClick={() => {
            void renditionRef.current?.prev();
          }}
        >
          <ChevronLeft data-icon="inline-start" />
          Trang trước
        </Button>

        <Button
          type="button"
          variant="outline"
          disabled={phase !== "ready"}
          onClick={() => {
            void renditionRef.current?.next();
          }}
        >
          Trang sau
          <ChevronRight data-icon="inline-end" />
        </Button>
      </div>
    </section>
  );
};
