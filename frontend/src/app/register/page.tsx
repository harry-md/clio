"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { type ChangeEvent, type SubmitEvent, useEffect, useState } from "react";
import { SiteHeader } from "@/components/Header";
import { Api, getApiErrorMessage } from "@/lib/api";

type RegisterForm = {
  username: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  email: string;
  avatar: File | null;
};

const initialForm: RegisterForm = {
  username: "",
  password: "",
  confirmPassword: "",
  firstName: "",
  lastName: "",
  email: "",
  avatar: null,
};
const MAX_FIELD_LENGTH = 255;
const MAX_AVATAR_SIZE = 10 * 1024 * 1024;

const validateForm = (form: RegisterForm) => {
  const username = form.username.trim();
  const firstName = form.firstName.trim();
  const lastName = form.lastName.trim();
  const email = form.email.trim();

  if (!lastName || !firstName || !email || !username || !form.password) {
    return "Vui lòng nhập đầy đủ các thông tin bắt buộc.";
  }

  if (
    username.length > MAX_FIELD_LENGTH ||
    firstName.length > MAX_FIELD_LENGTH ||
    lastName.length > MAX_FIELD_LENGTH ||
    email.length > MAX_FIELD_LENGTH
  ) {
    return "Thông tin nhập vào vượt quá độ dài cho phép.";
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    return "Email không đúng định dạng.";
  }

  if (form.password.length > MAX_FIELD_LENGTH) {
    return "Mật khẩu vượt quá độ dài cho phép.";
  }

  if (form.password !== form.confirmPassword) {
    return "Mật khẩu xác nhận không khớp.";
  }

  if (form.avatar && form.avatar.size > MAX_AVATAR_SIZE) {
    return "Avatar vượt quá kích thước cho phép.";
  }
  return null;
};

export default function RegisterPage() {
  const router = useRouter();

  const [form, setForm] = useState(initialForm);
  const [avatarPreview, setAvatarPreview] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    return () => {
      if (avatarPreview) URL.revokeObjectURL(avatarPreview);
    };
  }, [avatarPreview]);

  const handleAvatar = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] ?? null;

    if (avatarPreview) {
      URL.revokeObjectURL(avatarPreview);
    }

    setForm((current) => ({ ...current, avatar: file }));
    setAvatarPreview(file ? URL.createObjectURL(file) : "");
  };

  const handleSubmit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");

    const validationError = validateForm(form);
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);

    try {
      const payload = new FormData();

      payload.append("username", form.username.trim());
      payload.append("password", form.password);
      payload.append("firstName", form.firstName.trim());
      payload.append("lastName", form.lastName.trim());
      payload.append("email", form.email.trim());
      if (form.avatar) {
        payload.append("avatar", form.avatar);
      }

      await Api.post("/users", payload);

      router.push("/login");
    } catch (requestError) {
      setError(
        getApiErrorMessage(requestError, "Có lỗi trong lúc tạo tài khoản."),
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-[#151515]">
      <SiteHeader />

      <div className="mx-auto max-w-2xl px-5 py-14 lg:py-20">
        <section>
          <h2 className="font-sans text-3xl font-semibold text-[#f0eee8]">
            Tạo tài khoản
          </h2>

          <p className="mt-2 text-sm text-[#8b8a85]">
            Điền thông tin bên dưới để bắt đầu.
          </p>

          <form
            className="mt-9 grid gap-x-5 gap-y-6 sm:grid-cols-2"
            onSubmit={handleSubmit}
          >
            <div>
              <label htmlFor="lastName" className="form-label">
                Họ
              </label>
              <input
                id="lastName"
                required
                autoComplete="family-name"
                className="form-input"
                placeholder="Nhập họ"
                value={form.lastName}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    lastName: event.target.value,
                  }))
                }
              />
            </div>

            <div>
              <label htmlFor="firstName" className="form-label">
                Tên
              </label>
              <input
                id="firstName"
                required
                autoComplete="given-name"
                className="form-input"
                placeholder="Nhập tên"
                value={form.firstName}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    firstName: event.target.value,
                  }))
                }
              />
            </div>

            <div className="sm:col-span-2">
              <label htmlFor="email" className="form-label">
                Email
              </label>
              <input
                id="email"
                type="email"
                required
                autoComplete="email"
                className="form-input"
                placeholder="Nhập Email"
                value={form.email}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    email: event.target.value,
                  }))
                }
              />
            </div>

            <div className="sm:col-span-2">
              <label htmlFor="username" className="form-label">
                Tên đăng nhập
              </label>
              <input
                id="username"
                required
                autoComplete="username"
                className="form-input"
                placeholder="Nhập username"
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
                type="password"
                required
                autoComplete="new-password"
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

            <div>
              <label htmlFor="confirmPassword" className="form-label">
                Xác nhận mật khẩu
              </label>
              <input
                id="confirmPassword"
                type="password"
                required
                autoComplete="new-password"
                className="form-input"
                placeholder="Nhập lại mật khẩu"
                value={form.confirmPassword}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    confirmPassword: event.target.value,
                  }))
                }
              />
            </div>

            <div className="sm:col-span-2">
              <span className="form-label">Ảnh đại diện</span>
              <label className="flex min-h-24 cursor-pointer items-center gap-5 border border-dashed border-[#484844] bg-[#1a1a19] p-4 transition hover:border-[#74746e]">
                <div
                  className="grid size-16 shrink-0 place-items-center border border-[#444440] bg-[#252523] bg-cover bg-center text-2xl text-[#8d8c87]"
                  style={
                    avatarPreview
                      ? { backgroundImage: `url("${avatarPreview}")` }
                      : undefined
                  }
                >
                  {!avatarPreview && "＋"}
                </div>

                <div>
                  <p className="text-sm font-semibold text-[#d7d5ce]">
                    Chọn ảnh
                  </p>
                </div>

                <input
                  type="file"
                  accept="image/png,image/jpeg,image/webp"
                  className="sr-only"
                  onChange={handleAvatar}
                />
              </label>
            </div>

            {error && (
              <div
                role="alert"
                className="border border-[#7e4439] bg-[#2a1c19] px-4 py-3 text-sm text-[#e2a095] sm:col-span-2"
              >
                {error}
              </div>
            )}

            <div className="flex flex-col gap-4 border-t border-[#343432] pt-7 sm:col-span-2 sm:flex-row sm:items-center sm:justify-between">
              <p className="max-w-sm text-xs leading-5 text-[#777671]"></p>
              <p className="hidden text-sm text-[#8e8d88] sm:block">
                Đã có tài khoản?{" "}
                <Link
                  href="/login"
                  className="font-semibold text-[#7eb2db] hover:text-white"
                >
                  Đăng nhập
                </Link>
              </p>
              <button
                type="submit"
                disabled={loading}
                className="primary-button min-w-40"
              >
                {loading ? "Đang tạo..." : "Tạo tài khoản"}
              </button>
            </div>
          </form>
        </section>
      </div>
    </main>
  );
}
