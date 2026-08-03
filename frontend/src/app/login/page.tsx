"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { type SubmitEvent, useState } from "react";
import { Header } from "@/components/Header";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { Api, getApiErrorMessage } from "@/lib/api";

interface LoginForm {
  username: string;
  password: string;
}

const initialForm: LoginForm = {
  username: "",
  password: "",
};

const MAX_FIELD_LENGTH = 255;

const validateForm = (form: LoginForm) => {
  const username = form.username.trim();

  if (!username || !form.password) {
    return "Vui lòng nhập tên đăng nhập và mật khẩu.";
  }

  if (username.length > MAX_FIELD_LENGTH) {
    return "Tên đăng nhập vượt quá độ dài cho phép.";
  }

  if (form.password.length > MAX_FIELD_LENGTH) {
    return "Mật khẩu vượt quá độ dài cho phép.";
  }
  return null;
};

export default function LoginPage() {
  const router = useRouter();
  const { refreshUser } = useAuth();

  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");

    const validationError = validateForm(form);
    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setLoading(true);
      await Api.post("/login", {
        username: form.username.trim(),
        password: form.password,
      });

      await refreshUser();
      router.replace("/");
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          "Tên đăng nhập hoặc mật khẩu không chính xác!",
        ),
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-background">
      <Header />

      <div className="mx-auto max-w-2xl px-5 py-14 lg:py-20">
        <section>
          <h2 className="font-serif text-5xl font-semibold text-foreground">
            Đăng nhập
          </h2>
          <form className="mt-9 grid gap-y-6" onSubmit={handleSubmit}>
            <Field>
              <FieldLabel htmlFor="username">Tên đăng nhập</FieldLabel>

              <Input
                id="username"
                name="username"
                required
                autoComplete="username"
                placeholder="Nhập tên đăng nhập"
                value={form.username}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    username: event.target.value,
                  }))
                }
              />
            </Field>

            <Field>
              <FieldLabel htmlFor="password">Mật khẩu</FieldLabel>

              <Input
                id="password"
                name="password"
                type="password"
                required
                autoComplete="current-password"
                placeholder="Nhập mật khẩu"
                value={form.password}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    password: event.target.value,
                  }))
                }
              />
            </Field>

            {error && (
              <Alert variant="destructive">
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            <Button type="submit" size="lg" disabled={loading}>
              {loading && <Spinner data-icon="inline-start" />}
              {loading ? "Đang đăng nhập..." : "Đăng nhập"}
            </Button>
          </form>

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
}
