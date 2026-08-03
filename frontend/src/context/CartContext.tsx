"use client";

import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import type { Book } from "@/lib/types";

const CART_STORAGE_KEY = "clio-cart";

interface CartContextValue {
  books: Book[];
  addBook: (book: Book) => void;
  removeBook: (bookId: number) => void;
  hasBook: (bookId: number) => boolean;
  clearCart: () => void;
}

interface CartProviderProps {
  children: ReactNode;
}

const CartContext = createContext<CartContextValue | undefined>(undefined);

export function CartProvider({ children }: CartProviderProps) {
  const [books, setBooks] = useState<Book[]>([]);
  const [cartReady, setCartReady] = useState(false);

  useEffect(() => {
    try {
      const storedCart = window.localStorage.getItem(CART_STORAGE_KEY);

      if (storedCart) {
        const parsedCart: unknown = JSON.parse(storedCart);

        if (Array.isArray(parsedCart)) {
          setBooks(parsedCart as Book[]);
        }
      }
    } catch {
      window.localStorage.removeItem(CART_STORAGE_KEY);
    } finally {
      setCartReady(true);
    }
  }, []);

  useEffect(() => {
    if (cartReady) {
      window.localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(books));
    }
  }, [books, cartReady]);

  const addBook = useCallback((book: Book) => {
    setBooks((current) => {
      if (current.some((item) => item.id === book.id)) {
        return current;
      }

      return [...current, book];
    });
  }, []);

  const removeBook = useCallback((bookId: number) => {
    setBooks((current) => current.filter((book) => book.id !== bookId));
  }, []);

  const hasBook = useCallback(
    (bookId: number) => books.some((book) => book.id === bookId),
    [books],
  );

  const clearCart = useCallback(() => {
    setBooks([]);
    window.localStorage.removeItem(CART_STORAGE_KEY);
  }, []);

  const value = useMemo(
    () => ({ books, addBook, removeBook, hasBook, clearCart }),
    [books, addBook, removeBook, hasBook, clearCart],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const context = useContext(CartContext);

  if (context === undefined) {
    throw new Error("useCart phải gọi trong CartProvider");
  }

  return context;
}
