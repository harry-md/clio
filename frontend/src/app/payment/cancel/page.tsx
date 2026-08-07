import { CircleXIcon } from "lucide-react";
import { PaymentCancelHandler } from "@/components/PaymentCancelHandler";
import { Spinner } from "@/components/ui/spinner";

const PaymentCancelPage = () => {
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

        <PaymentCancelHandler />
      </section>
    </main>
  );
};
export default PaymentCancelPage;
