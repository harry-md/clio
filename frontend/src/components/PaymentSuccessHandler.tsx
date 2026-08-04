"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

import { useAuth } from "@/context/AuthContext";
import { useCart } from "@/context/CartContext";

export const PaymentSuccessHandler = () => {
  const router = useRouter();

  const { refreshUser } = useAuth();
  const { clearCart } = useCart();

  useEffect(() => {
    const timer = window.setTimeout(() => {
      const finishPayment = async () => {
        const pendingBookCheckout = window.sessionStorage.getItem(
          "pending-book-checkout",
        );

        if (pendingBookCheckout) {
          clearCart();
        }

        window.sessionStorage.removeItem("pending-book-checkout");
        await refreshUser().catch();
        router.replace("/library");
      };

      void finishPayment();
    }, 2000);

    return () => {
      window.clearTimeout(timer);
    };
  }, [clearCart, refreshUser, router]);

  return null;
};
