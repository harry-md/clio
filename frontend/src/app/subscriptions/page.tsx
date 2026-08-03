"use client";

import Image from "next/image";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { EmptyState } from "@/components/EmptyState";
import { Header } from "@/components/Header";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { Api, getApiErrorMessage } from "@/lib/api";

interface SubscriptionPlan {
  id: number;
  name: string;
  price: number;
  duration: number;
  description: string | null;
  active: boolean;
}

interface StripeCheckoutResponse {
  orderId: number;
  checkoutUrl: string;
}

const priceFormatter = new Intl.NumberFormat("en-US", {
  maximumFractionDigits: 0,
});

export default function SubscriptionsPage() {
  const router = useRouter();
  const { user, initialized } = useAuth();

  const [plan, setPlan] = useState<SubscriptionPlan | null>(null);
  const [loading, setLoading] = useState(true);
  const [purchasing, setPurchasing] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    const fetchSubscriptionPlan = async () => {
      try {
        setLoading(true);
        setError("");

        const { data } = await Api.get<SubscriptionPlan[]>(
          "/subscription-plans",
          {
            signal: controller.signal,
          },
        );

        setPlan(data.find((item) => item.active) ?? null);
      } catch (requestError) {
        if (!controller.signal.aborted) {
          setError(
            getApiErrorMessage(
              requestError,
              "Không thể tải thông tin gói đọc.",
            ),
          );
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };

    void fetchSubscriptionPlan();

    return () => {
      controller.abort();
    };
  }, []);

  const handlePurchase = async () => {
    if (!plan || purchasing) {
      return;
    }

    if (!user) {
      router.push("/login");
      return;
    }

    try {
      setPurchasing(true);
      setError("");

      const { data } = await Api.post<StripeCheckoutResponse>(
        "/orders/subscription",
        {
          planId: plan.id,
        },
      );

      if (!data.checkoutUrl) {
        throw new Error("Checkout URL không hợp lệ");
      }

      window.location.assign(data.checkoutUrl);
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          "Không thể tạo phiên thanh toán. Vui lòng thử lại.",
        ),
      );
      setPurchasing(false);
    }
  };

  const durationLabel =
    plan?.duration === 1 ? "1 tháng" : `${plan?.duration ?? 0} tháng`;

  return (
    <main className="min-h-screen bg-background">
      {loading && <LoadingOverlay />}

      <Header />

      <section className="border-b border-border bg-card">
        <div className="mx-auto max-w-360 px-5 py-16 lg:px-10 lg:py-24">
          <h1 className="max-w-4xl font-serif text-5xl font-bold leading-tight text-foreground sm:text-6xl lg:text-7xl">
            Đọc mọi cuốn sách trong thời gian hiệu lực
          </h1>
        </div>
      </section>

      <section className="mx-auto max-w-360 px-5 py-14 lg:px-10 lg:py-20">
        {error && (
          <Alert variant="destructive" className="mb-8">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {!loading && !plan && !error && (
          <EmptyState
            title="Hiện chưa có gói đọc"
            description="Gói đọc sẽ được hiển thị tại đây khi sẵn sàng."
          />
        )}

        {plan && (
          <article className="grid border border-border-strong bg-card lg:grid-cols-2">
            <div className="flex flex-col border-b border-border p-7 sm:p-10 lg:border-b-0 lg:border-r lg:p-14">
              <h2 className="mt-2 text-4xl font-semibold leading-tight text-foreground sm:text-5xl">
                {plan.name}
              </h2>

              <div className="mt-2 flex items-start justify-between gap-4">
                <p className="flex-7 text-muted-foreground">
                  {plan.description ??
                    "Đọc mọi cuốn sách trong hệ thống trong thời gian hiệu lực."}
                </p>

                <span className="flex-3 text-right text-muted-foreground">
                  Hiệu lực: {durationLabel}
                </span>
              </div>
              <div className="mt-10 border-y border-border py-8">
                <div className="mt-3 flex flex-wrap items-end gap-3">
                  <span className="text-5xl font-semibold text-price sm:text-6xl">
                    {`${priceFormatter.format(Number(plan.price))} VND`}
                  </span>

                  <span className="pb-2 text-muted-foreground">
                    / {durationLabel}
                  </span>
                </div>
              </div>
              <Button
                type="button"
                size="lg"
                disabled={!initialized || purchasing}
                onClick={handlePurchase}
                className="mt-8 w-full sm:w-auto sm:min-w-56"
              >
                {purchasing && <Spinner data-icon="inline-start" />}
                {purchasing ? "Đang chuyển đến thanh toán..." : "Mua gói đọc"}
              </Button>
            </div>
            <div className="relative min-h-72 lg:min-h-full">
              <Image
                src="https://res.cloudinary.com/dswxedhsf/image/upload/v1785130596/library_compressed_t0ncux.jpg"
                alt="Không gian thư viện"
                fill
                sizes="(min-width: 1024px) 50vw, 100vw"
                className="object-cover"
              />
            </div>
          </article>
        )}
      </section>
    </main>
  );
}
