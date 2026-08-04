import { EmptyState } from "@/components/EmptyState";
import { Header } from "@/components/Header";
import {
  type SubscriptionPlan,
  SubscriptionPlanCard,
} from "@/components/SubscriptionPlanCard";

const fetchSubscriptionPlans = async (): Promise<SubscriptionPlan[]> => {
  try {
    const res = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/subscription-plans`,
      {
        next: { revalidate: 3600 },
      },
    );

    if (!res.ok) return [];

    return await res.json();
  } catch (error) {
    console.error("Có lỗi khi lấy thong ", error);
    return [];
  }
};

const SubscriptionsPage = async () => {
  const activePlans = await fetchSubscriptionPlans();

  return (
    <main className="min-h-screen bg-background">
      <Header />
      <section className="mx-auto max-w-360 px-5 py-14 lg:px-10 lg:py-20">
        {activePlans.length === 0 ? (
          <EmptyState
            title="Hiện chưa có gói đọc"
            description="Gói đọc sẽ được hiển thị tại đây khi sẵn sàng."
          />
        ) : (
          <div className="flex flex-col gap-10">
            {activePlans.map((plan) => (
              <SubscriptionPlanCard key={plan.id} plan={plan} />
            ))}
          </div>
        )}
      </section>
    </main>
  );
};
export default SubscriptionsPage;
