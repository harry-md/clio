"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

export const PaymentCancelHandler = () => {
  const router = useRouter();

  useEffect(() => {
    window.sessionStorage.removeItem("pending-book-checkout");

    const timer = window.setTimeout(() => {
      router.replace("/");
    }, 2500);

    return () => {
      window.clearTimeout(timer);
    };
  }, [router]);

  return null;
};
