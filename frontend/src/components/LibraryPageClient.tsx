"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import { EmptyState } from "@/components/EmptyState";
import LibraryBooks from "@/components/LibraryBooks";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { Pagination } from "@/components/Pagination";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useAuth } from "@/context/AuthContext";
import { Api, getApiErrorMessage } from "@/lib/api";
import { type BookData, getBooksByUser } from "@/lib/offline";
import type { LibraryItem, PageMetadata, PageResponse } from "@/lib/types";

const toLibraryItem = (book: BookData): LibraryItem => ({
  id: book.bookId,
  bookId: book.bookId,
  title: book.metadata.title,
  authors: book.metadata.authors,
  type: book.metadata.type,
  thumbnail: null,
  cfiPosition: null,
});

export const LibraryPageClient = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const { user, offlineAccount, initialized } = useAuth();
  const userId = user?.id ?? offlineAccount?.userId;

  const currentPage = Number(searchParams.get("page"));
  const [libraries, setLibraries] = useState<LibraryItem[]>([]);
  const [page, setPage] = useState<PageMetadata | null>(null);
  const [offlineSource, setOfflineSource] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!initialized) {
      return;
    }

    let active = true;

    const loadLibrary = async () => {
      setLoading(true);
      setError("");

      if (userId === undefined) {
        setLibraries([]);
        setPage(null);

        if (navigator.onLine) {
          router.replace("/login");
        } else {
          setError("Không thấy tài khoản đã đăng nhập.");
        }

        setLoading(false);
        return;
      }

      let localBooks: BookData[] = [];

      try {
        localBooks = await getBooksByUser(userId);
      } catch (error: unknown) {
        if (active) {
          setError(
            error instanceof Error ? error.message : "Lỗi khi mở thư viện.",
          );
        }
      }

      if (!active) {
        return;
      }

      const localLibraries = localBooks.map(toLibraryItem);

      setLibraries(localLibraries);
      setPage(null);
      setOfflineSource(true);

      if (!user || !navigator.onLine) {
        setLoading(false);
        return;
      }

      try {
        const { data } = await Api.get<PageResponse<LibraryItem>>(
          "/libraries",
          {
            params: {
              page: currentPage,
              size: 12,
              sort: ["createdAt,desc", "id,desc"],
            },
          },
        );

        if (!active) {
          return;
        }

        setLibraries(data.content);
        setPage(data.page);
        setOfflineSource(false);
        setError("");
      } catch (error: unknown) {
        if (!active) {
          return;
        }

        const reason = getApiErrorMessage(
          error,
          error instanceof Error ? error.message : "Không thể tải thư viện.",
        );

        if (localLibraries.length > 0) {
          setError(
            navigator.onLine
              ? `Không tải được thư viện online: ${reason}. Đang hiển thị sách đã tải.`
              : "Bạn đang offline. Đang hiển thị sách đã tải.",
          );
        } else {
          setError(reason);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void loadLibrary();

    return () => {
      active = false;
    };
  }, [currentPage, initialized, router, user, userId]);

  if (loading) {
    return <LoadingOverlay />;
  }

  return (
    <>
      <div className="border-b border-border pb-8">
        <h1 className="font-serif text-5xl font-semibold text-foreground">
          Thư viện
        </h1>

        <p className="mt-3 text-muted-foreground">
          {page?.totalElements ?? libraries.length} cuốn sách
          {offlineSource ? " đã tải trên thiết bị" : " trong thư viện"}
        </p>
      </div>

      {error && (
        <Alert variant="default" className="mt-8">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {libraries.length > 0 ? (
        <>
          <LibraryBooks libraries={libraries} />

          {!offlineSource && page && page.totalPages > 1 && (
            <Pagination
              currentPage={page.number}
              totalPages={page.totalPages}
              basePath="/library"
              toSection="library-list"
            />
          )}
        </>
      ) : (
        <EmptyState
          className="mt-8"
          title={offlineSource ? "Chưa có sách offline" : "Thư viện đang trống"}
        />
      )}
    </>
  );
};
