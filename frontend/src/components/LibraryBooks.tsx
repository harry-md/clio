"use client";

import { useEffect, useState } from "react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { useAuth } from "@/context/AuthContext";
import { Api, getApiErrorMessage } from "@/lib/api";
import { getBooksByUser, getOrCreateKey, storeBook } from "@/lib/offline";
import type { LibraryItem } from "@/lib/types";
import LibraryCard from "./LibraryCard";

interface LibraryBooksProps {
  libraries: LibraryItem[];
}

interface DownloadResponse {
  downloadUrl: string;
  urlExpiredAt: string;
  license: string;
}

const LibraryBooks = ({ libraries }: LibraryBooksProps) => {
  const { user, initialized } = useAuth();
  const userId = user?.id;

  const [downloadedBookIds, setDownloadedBookIds] = useState<Set<number>>(
    new Set(),
  );

  const [offlineDataReady, setOfflineDataReady] = useState(false);
  const [downloadingBookId, setDownloadingBookId] = useState<number | null>(
    null,
  );
  const [error, setError] = useState("");

  const handleDownload = async (bookId: number): Promise<void> => {
    if (!user) {
      return;
    }

    try {
      setDownloadingBookId(bookId);
      setError("");

      const accountKey = await getOrCreateKey(user.id);

      const { data } = await Api.post<DownloadResponse>("/libraries/download", {
        bookId: bookId,
        publicKeySpki: accountKey.publicKeySpki,
      });

      await storeBook(user.id, bookId, data.license, data.downloadUrl);

      setDownloadedBookIds((current) => {
        const bookIds = new Set(current);
        bookIds.add(bookId);
        return bookIds;
      });
    } catch (error: unknown) {
      setError(getApiErrorMessage(error));
    } finally {
      setDownloadingBookId(null);
    }
  };

  useEffect(() => {
    setDownloadedBookIds(new Set());
    setOfflineDataReady(false);
    setError("");

    if (!initialized || userId === undefined) {
      return;
    }

    let active = true;

    const loadOfflineBooks = async () => {
      try {
        const books = await getBooksByUser(userId);

        if (!active) {
          return;
        }

        setDownloadedBookIds(new Set(books.map((book) => book.bookId)));
      } catch (error: unknown) {
        if (active) {
          setError(
            error instanceof Error ? error.message : "Lỗi kiểm tra sách đã tải",
          );
        }
      } finally {
        if (active) {
          setOfflineDataReady(true);
        }
      }
    };

    void loadOfflineBooks();

    return () => {
      active = false;
    };
  }, [initialized, userId]);

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
            onDownloadAction={handleDownload}
            checkingOfflineData={!offlineDataReady}
            downloading={downloadingBookId === library.bookId}
            downloadDisabled={
              !userId || !offlineDataReady || downloadingBookId !== null
            }
          />
        ))}
      </div>
    </>
  );
};

export default LibraryBooks;
