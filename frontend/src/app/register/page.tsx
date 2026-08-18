import { Header } from "@/components/Header";
import { RegisterForm } from "@/components/RegisterForm";

const RegisterPage = () => {
  return (
    <main className="min-h-screen bg-background">
      <Header />

      <div className="mx-auto max-w-2xl px-5 py-14 lg:py-20">
        <section>
          <h2 className="text-5xl font-semibold text-foreground">Đăng ký</h2>
          <RegisterForm />
        </section>
      </div>
    </main>
  );
};
export default RegisterPage;
