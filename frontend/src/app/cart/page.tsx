import { CartClient } from "@/components/CartClient";
import { Header } from "@/components/Header";

export default function CartPage() {
  return (
    <main className="min-h-screen bg-background">
      <Header />
      <CartClient />
    </main>
  );
}
