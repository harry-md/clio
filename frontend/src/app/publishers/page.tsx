import { Header } from "@/components/Header";
import { PublisherDashboard } from "@/components/PublisherDashboard";

const PublisherPage = () => {
  return (
    <main className="min-h-screen bg-background">
      <Header />

      <section className="mx-auto max-w-360 px-5 py-12 lg:px-10 lg:py-16">
        <PublisherDashboard />
      </section>
    </main>
  );
};

export default PublisherPage;
