export type AuthUser = {
  username: string;
  firstName: string;
  lastName: string;
  avatar: string | null;
  role: "READER" | "PUBLISHER" | "ADMIN";
  isSubscribed: boolean;
};

export type BookAuthor = {
  authorId: number;
  authorFullname: string;
  role: "AUTHOR" | "COAUTHOR" | "ARTIST" | "TRANSLATOR";
};

export type Book = {
  id: number;
  title: string;
  price: number;
  thumbnail: string | null;
  type: string;
  rating: number | null;
  ratingCount: number;
  authors: BookAuthor[];
};

export type BookCategory = {
  id: number;
  name: string;
};

export type BookInfo = {
  description: string | null;
  language: string;
  fileSize: number;
  wordCount: number;
  isbn: string | null;
};

export type BookDetail = Book & {
  categories: BookCategory[];
  bookInfo: BookInfo;
  createdAt: string;
  updatedAt: string;
};

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
};
