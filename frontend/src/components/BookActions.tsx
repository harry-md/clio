"use client";

import { LibraryIcon, ShoppingCartIcon } from "lucide-react";

import { Button } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";
import { useCart } from "@/context/CartContext";
import type { BookDetail } from "@/lib/types";

type BookActionsProps = {
  book: BookDetail;
};

export const BookActions = ({ book }: BookActionsProps) => {
  const { user } = useAuth();
  const { addBook, hasBook } = useCart();

  const bookInCart = hasBook(book.id);

  return (
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
        <Button type="button" size="lg" variant="outline" className="min-w-48">
          <LibraryIcon data-icon="inline-start" />
          Thêm vào thư viện
        </Button>
      )}
    </div>
  );
};
