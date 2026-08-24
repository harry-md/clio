"use client";

import { LibraryIcon, ShoppingCartIcon } from "lucide-react";
import { useState } from "react";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { useCart } from "@/context/CartContext";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { BookDetail, LibraryItem } from "@/lib/types";

type BookActionsProps = {
  book: BookDetail;
};

export const BookActions = ({ book }: BookActionsProps) => {
  const { user } = useAuth();
  const { addBook, hasBook } = useCart();

  const [addingToLibrary, setAddingToLibrary] = useState(false);
  const [addedToLibrary, setAddedToLibrary] = useState(false);
  const [libraryError, setLibraryError] = useState("");

  const bookInCart = hasBook(book.id);

  const handleAddToLibrary = async () => {
    if (addingToLibrary || addedToLibrary) {
      return;
    }

    try {
      setAddingToLibrary(true);
      setLibraryError("");

      await Api.post<LibraryItem>("/libraries", {
        bookId: book.id,
      });

      setAddedToLibrary(true);
    } catch (error: unknown) {
      setLibraryError(
        getApiErrorMessage(
          error,
          "Không thể thêm sách vào thư viện. Vui lòng thử lại.",
        ),
      );
    } finally {
      setAddingToLibrary(false);
    }
  };

  return (
    <div className="grid gap-3">
      <div className="flex flex-wrap gap-3">
        <Button
          type="button"
          size="lg"
          disabled={bookInCart}
          onClick={() => addBook(book)}
          className="min-w-52"
        >
          <ShoppingCartIcon data-icon="inline-start" />
          {bookInCart ? "Đã có trong giỏ" : "Thêm vào giỏ"}
        </Button>

        {user?.isSubscribed && (
          <Button
            type="button"
            size="lg"
            variant="outline"
            className="min-w-48"
            disabled={addingToLibrary || addedToLibrary}
            onClick={handleAddToLibrary}
          >
            {addingToLibrary ? (
              <Spinner data-icon="inline-start" />
            ) : (
              <LibraryIcon data-icon="inline-start" />
            )}

            {addedToLibrary ? "Đã có trong thư viện" : "Thêm vào thư viện"}
          </Button>
        )}
      </div>

      {libraryError && (
        <Alert variant="destructive">
          <AlertDescription>{libraryError}</AlertDescription>
        </Alert>
      )}
    </div>
  );
};
