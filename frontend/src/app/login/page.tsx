"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { type SubmitEvent, useEffect, useRef, useState } from "react";
import { ClioLogo } from "@/components/ClioLogo";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { AuthUser } from "@/lib/types";

export default function LoginPage() {
  const router = useRouter();
  const { setUser } = useAuth();
  const firstInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    firstInputRef.current?.focus();
  }, []);

  const [form, setForm] = useState({
    username: "",
    password: "",
  });

  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setSubmitting(true);

    try {
      const { data } = await Api.post<AuthUser>("/login", form);
      setUser(data);
      router.replace("/");
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          "Tên đăng nhập hoặc mật khẩu không chính xác.",
        ),
      );
    } finally {
      setSubmitting(false);
    }
  };

  const errorDescriptionId = error ? "login-error-description" : undefined;

  return (
    <main className="grid min-h-screen bg-background lg:grid-cols-[minmax(0,1.1fr)_minmax(420px,.9fr)]">
      <section className="relative hidden overflow-hidden border-r border-border bg-card lg:flex">
        <div
          className="absolute inset-0"
          style={{
            background:
              "linear-gradient(135deg, var(--auth-warm-overlay), transparent 48%), linear-gradient(315deg, var(--auth-cool-overlay), transparent 45%)",
          }}
        />

        <div className="relative flex w-full items-center justify-center p-12 xl:p-16">
          <div className="absolute left-12 top-12 xl:left-16 xl:top-16">
            <ClioLogo />
          </div>

          <h1 className="max-w-xl text-center font-serif text-5xl font-bold leading-tight text-foreground xl:text-6xl">
            Tiếp tục đọc sách trên Clio
          </h1>
        </div>
      </section>

      <section className="flex min-h-screen items-center justify-center px-5 py-10 sm:px-10">
        <div className="w-full max-w-107.5">
          <div className="mb-14 lg:hidden">
            <ClioLogo />
          </div>

          <h2 className="text-center font-serif text-5xl font-semibold text-foreground">
            Đăng nhập
          </h2>

          <form className="mt-10 space-y-6" onSubmit={handleSubmit}>
            <Field>
              <FieldLabel htmlFor="username">Tên đăng nhập</FieldLabel>
              <Input
                ref={firstInputRef}
                id="username"
                name="username"
                autoComplete="username"
                required
                placeholder="Nhập tên đăng nhập"
                aria-describedby={errorDescriptionId}
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
                autoComplete="current-password"
                required
                placeholder="Nhập mật khẩu"
                aria-describedby={errorDescriptionId}
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
                <AlertDescription id="login-error-description">
                  {error}
                </AlertDescription>
              </Alert>
            )}

            <Button
              type="submit"
              size="lg"
              disabled={submitting}
              className="w-full"
            >
              {submitting && <Spinner data-icon="inline-start" />}
              {submitting ? "Đang đăng nhập..." : "Đăng nhập"}
            </Button>
          </form>

          <div className="mt-8 border-t border-border pt-7 text-center text-sm text-muted-foreground">
            Chưa có tài khoản?{" "}
            <Link
              href="/register"
              className="font-semibold text-link transition hover:text-foreground"
            >
              Tạo tài khoản
            </Link>
          </div>
        </div>
      </section>
    </main>
  );
}
