import { CircleCheckIcon } from "lucide-react";
import { PaymentSuccessHandler } from "@/components/PaymentSuccessHandler";
import { Spinner } from "@/components/ui/spinner";

const PaymentSuccessPage = () => {
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
          Giao dịch đã hoàn tất. Đang chuyển bạn về thư viện.
        </p>

        <Spinner className="mx-auto mt-8 size-6 text-primary" />

        <PaymentSuccessHandler />
      </section>
    </main>
  );
};
export default PaymentSuccessPage;
