"use client";

import { useSearchParams } from "next/navigation";
import { Reader } from "@/components/Reader";

export const ReaderRoute = () => {
  const searchParams = useSearchParams();
  const bookId = Number(searchParams.get("bookId"));
  return <Reader bookId={bookId} />;
};
