"use client";

import Image from "next/image";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { Api, getApiErrorMessage } from "@/lib/api";

export interface SubscriptionPlan {
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

interface SubscriptionPlanCardProps {
  plan: SubscriptionPlan;
}

export const SubscriptionPlanCard = ({ plan }: SubscriptionPlanCardProps) => {
  const router = useRouter();
  const { user, initialized } = useAuth();

  const [purchasing, setPurchasing] = useState(false);
  const [error, setError] = useState("");

  const handlePurchase = async () => {
    if (purchasing) return;

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
    } catch (error) {
      setError(
        getApiErrorMessage(
          error,
          "Không thể tạo phiên thanh toán. Vui lòng thử lại.",
        ),
      );
      setPurchasing(false);
    }
  };

  const durationLabel = `${plan.duration} tháng`;

  return (
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
            <span className="text-4xl font-semibold text-price">
              {`${priceFormatter.format(Number(plan.price))} VND`}
            </span>
            <span className="pb-2 text-muted-foreground">
              / {durationLabel}
            </span>
          </div>
        </div>

        {error && (
          <Alert variant="destructive" className="mt-6">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

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
  );
};
