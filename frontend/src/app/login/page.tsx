"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { type SubmitEvent, useState } from "react";
import { ClioLogo } from "@/components/ClioLogo";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { AuthUser } from "@/lib/types";

export default function LoginPage() {
  const router = useRouter();

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
      await Api.post<AuthUser>("/login", form);
      router.push("/");
      router.refresh();
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

  return (
    <main className="grid min-h-screen bg-[#151515] lg:grid-cols-[minmax(0,1.1fr)_minmax(420px,.9fr)]">
      <section className="relative hidden overflow-hidden border-r border-[#343432] bg-[#1a1a19] lg:flex">
        <div className="absolute inset-0 bg-[linear-gradient(135deg,rgba(113,73,43,.24),transparent_48%),linear-gradient(315deg,rgba(39,77,105,.2),transparent_45%)]" />
        <div className="relative flex w-full items-center justify-center p-12 xl:p-16">
          <div className="absolute left-12 top-12 xl:left-16 xl:top-16">
            <ClioLogo />
          </div>

          <h1 className="max-w-xl text-center font-serif text-5xl font-bold leading-tight text-[#f2efe8] xl:text-6xl">
            Tiếp tục đọc sách trên Clio
          </h1>
        </div>
      </section>

      <section className="flex min-h-screen items-center justify-center px-5 py-10 sm:px-10">
        <div className="w-full max-w-107.5">
          <div className="mb-14 lg:hidden">
            <ClioLogo />
          </div>

          <h2 className="text-center font-serif text-4xl font-semibold text-[#f3f0e9]">
            Đăng nhập
          </h2>

          <form className="mt-10 space-y-6" onSubmit={handleSubmit}>
            <div>
              <label htmlFor="username" className="form-label">
                Tên đăng nhập
              </label>
              <input
                id="username"
                name="username"
                autoComplete="username"
                required
                className="form-input"
                placeholder="Nhập tên đăng nhập"
                value={form.username}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    username: event.target.value,
                  }))
                }
              />
            </div>

            <div>
              <label htmlFor="password" className="form-label">
                Mật khẩu
              </label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                required
                className="form-input"
                placeholder="Nhập mật khẩu"
                value={form.password}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    password: event.target.value,
                  }))
                }
              />
            </div>

            {error && (
              <div
                role="alert"
                className="border border-[#7e4439] bg-[#2a1c19] px-4 py-3 text-sm text-[#e2a095]"
              >
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={submitting}
              className="primary-button w-full"
            >
              {submitting ? "Đang đăng nhập..." : "Đăng nhập"}
            </button>
          </form>

          <div className="mt-8 border-t border-[#343432] pt-7 text-center text-sm text-[#8f8e89]">
            Chưa có tài khoản?{" "}
            <Link
              href="/register"
              className="font-semibold text-[#7eb2db] hover:text-white"
            >
              Tạo tài khoản
            </Link>
          </div>
        </div>
      </section>
    </main>
  );
}
