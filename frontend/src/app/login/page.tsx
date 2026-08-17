import Link from "next/link";
import { Header } from "@/components/Header";
import { LoginForm } from "@/components/LoginForm";

const LoginPage = () => {
  return (
    <main className="min-h-screen bg-background">
      <Header />

      <div className="mx-auto max-w-2xl px-5 py-14 lg:py-20">
        <section>
          <h2 className="text-5xl font-semibold text-foreground">Đăng nhập</h2>
          <LoginForm />
          <div className="mt-8 border-t border-border pt-7 text-sm text-muted-foreground">
            Chưa có tài khoản?{" "}
            <Link
              href="/register"
              className="font-semibold text-link transition hover:text-foreground"
            >
              Tạo tài khoản
            </Link>
          </div>
        </section>
      </div>
    </main>
  );
};
export default LoginPage;
