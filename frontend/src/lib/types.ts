export interface AuthUser {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  avatar: string | null;
  role: "READER" | "PUBLISHER" | "ADMIN";
  isSubscribed: boolean;
}

export interface BookAuthor {
  authorId: number;
  authorFullname: string;
  role: "AUTHOR" | "COAUTHOR" | "ARTIST" | "TRANSLATOR";
}

export interface Book {
  id: number;
  title: string;
  price: number;
  thumbnail: string | null;
  type: string;
  rating: number | null;
  ratingCount: number;
  authors: BookAuthor[];
}

export interface BookCategory {
  id: number;
  name: string;
}

export interface BookInfo {
  description: string | null;
  language: string;
  fileSize: number;
  wordCount: number;
  isbn: string | null;
}

export interface BookDetail extends Book {
  categories: BookCategory[];
  bookInfo: BookInfo;
  createdAt: string;
  updatedAt: string;
}

export interface PageMetadata {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface PageResponse<T> {
  content: T[];
  page: PageMetadata;
}

export type UserLibraryType = "PURCHASED" | "SUBSCRIBED" | "UPLOADED";

export interface LibraryItem {
  id: number;
  type: UserLibraryType;
  cfiPosition: string | null;
  bookId: number;
  title: string;
  thumbnail: string | null;
  authors: BookAuthor[];
}
