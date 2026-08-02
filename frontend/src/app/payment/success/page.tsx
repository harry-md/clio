"use client";

import { CircleCheckIcon } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { useCart } from "@/context/CartContext";

export default function PaymentSuccessPage() {
  const router = useRouter();
  const { refreshUser } = useAuth();
  const { clearCart } = useCart();

  useEffect(() => {
    const timer = window.setTimeout(() => {
      const finishPayment = async () => {
        const pendingBookCheckout = window.sessionStorage.getItem(
          "clio-pending-book-checkout",
        );

        if (pendingBookCheckout) {
          clearCart();
        }

        window.sessionStorage.removeItem("clio-pending-book-checkout");

        await refreshUser().catch(() => undefined);
        router.replace("/");
      };

      void finishPayment();
    }, 2000);

    return () => {
      window.clearTimeout(timer);
    };
  }, [clearCart, refreshUser, router]);

  return (
    <main className="grid min-h-screen place-items-center bg-background px-5">
      <section className="w-full max-w-xl border border-border-strong bg-card p-10 text-center">
        <CircleCheckIcon
          aria-hidden="true"
          className="mx-auto size-14 text-primary"
        />

        <h1 className="mt-7 font-serif text-4xl font-semibold text-foreground">
          Mua thành công
        </h1>

        <p className="mt-3 text-muted-foreground">
          Giao dịch đã hoàn tất. Đang chuyển bạn về trang chủ.
        </p>

        <Spinner className="mx-auto mt-8 size-6 text-primary" />
      </section>
    </main>
  );
}
