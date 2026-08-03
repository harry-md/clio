"use client";

import { CircleXIcon } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { Spinner } from "@/components/ui/spinner";

export default function PaymentCancelPage() {
  const router = useRouter();

  useEffect(() => {
    window.sessionStorage.removeItem("clio-pending-book-checkout");

    const timer = window.setTimeout(() => {
      router.replace("/");
    }, 2500);

    return () => {
      window.clearTimeout(timer);
    };
  }, [router]);

  return (
    <main className="grid min-h-screen place-items-center bg-background px-5">
      <section className="w-full max-w-xl border border-destructive bg-destructive-surface p-10 text-center">
        <CircleXIcon
          aria-hidden="true"
          className="mx-auto size-14 text-destructive-foreground"
        />

        <h1 className="mt-7 font-serif text-4xl font-semibold text-foreground">
          Mua thất bại
        </h1>

        <p className="mt-3 text-muted-foreground">
          Giao dịch chưa hoàn tất. Đang chuyển bạn về trang chủ.
        </p>

        <Spinner className="mx-auto mt-8 size-6 text-destructive-foreground" />
      </section>
    </main>
  );
}
