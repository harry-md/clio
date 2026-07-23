"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  type ChangeEvent,
  type SubmitEvent,
  useEffect,
  useRef,
  useState,
} from "react";
import { Header } from "@/components/Header";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
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
  const firstInputRef = useRef<HTMLInputElement>(null);

  const [form, setForm] = useState(initialForm);
  const [avatarPreview, setAvatarPreview] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    firstInputRef.current?.focus();
  }, []);

  useEffect(() => {
    return () => {
      if (avatarPreview) {
        URL.revokeObjectURL(avatarPreview);
      }
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
    <main className="min-h-screen bg-background">
      <Header />

      <div className="mx-auto max-w-2xl px-5 py-14 lg:py-20">
        <section>
          <h2 className="font-serif text-5xl font-semibold text-foreground">
            Tạo tài khoản
          </h2>

          <form
            className="mt-9 grid gap-x-5 gap-y-6 sm:grid-cols-2"
            onSubmit={handleSubmit}
          >
            <Field>
              <FieldLabel htmlFor="lastName">Họ</FieldLabel>

              <Input
                ref={firstInputRef}
                id="lastName"
                name="lastName"
                required
                autoComplete="family-name"
                placeholder="Nhập họ"
                value={form.lastName}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    lastName: event.target.value,
                  }))
                }
              />
            </Field>

            <Field>
              <FieldLabel htmlFor="firstName">Tên</FieldLabel>

              <Input
                id="firstName"
                name="firstName"
                required
                autoComplete="given-name"
                placeholder="Nhập tên"
                value={form.firstName}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    firstName: event.target.value,
                  }))
                }
              />
            </Field>

            <Field className="sm:col-span-2">
              <FieldLabel htmlFor="email">Email</FieldLabel>

              <Input
                id="email"
                name="email"
                type="email"
                required
                autoComplete="email"
                placeholder="Nhập email"
                value={form.email}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    email: event.target.value,
                  }))
                }
              />
            </Field>

            <Field className="sm:col-span-2">
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
                autoComplete="new-password"
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

            <Field>
              <FieldLabel htmlFor="confirmPassword">
                Xác nhận mật khẩu
              </FieldLabel>

              <Input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                required
                autoComplete="new-password"
                placeholder="Nhập lại mật khẩu"
                value={form.confirmPassword}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    confirmPassword: event.target.value,
                  }))
                }
              />
            </Field>

            <Field className="sm:col-span-2">
              <FieldLabel htmlFor="avatar">Ảnh đại diện</FieldLabel>

              <label
                htmlFor="avatar"
                className="flex min-h-24 cursor-pointer items-center gap-5 border border-dashed border-border-strong bg-card p-4 transition hover:border-ring"
              >
                <div
                  className="grid size-16 shrink-0 place-items-center border border-input bg-secondary bg-cover bg-center text-2xl text-muted-foreground"
                  style={
                    avatarPreview
                      ? { backgroundImage: `url("${avatarPreview}")` }
                      : undefined
                  }
                >
                  {!avatarPreview && "＋"}
                </div>

                <div>
                  <p className="text-sm font-semibold text-secondary-foreground">
                    Chọn ảnh
                  </p>
                  <p className="mt-1 text-xs text-muted-foreground">
                    PNG, JPEG hoặc WebP, tối đa 10 MB
                  </p>
                </div>
              </label>

              <input
                id="avatar"
                name="avatar"
                type="file"
                accept="image/png,image/jpeg,image/webp"
                className="sr-only"
                onChange={handleAvatar}
              />
            </Field>

            {error && (
              <Alert variant="destructive" className="sm:col-span-2">
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            <div className="flex flex-col gap-4 border-t border-border pt-7 sm:col-span-2 sm:flex-row sm:items-center sm:justify-between">
              <p className="hidden text-sm text-muted-foreground sm:block">
                Đã có tài khoản?{" "}
                <Link
                  href="/login"
                  className="font-semibold text-link transition hover:text-foreground"
                >
                  Đăng nhập
                </Link>
              </p>

              <Button
                type="submit"
                size="lg"
                disabled={loading}
                className="min-w-40 sm:ml-auto"
              >
                {loading && <Spinner data-icon="inline-start" />}
                {loading ? "Đang tạo..." : "Tạo tài khoản"}
              </Button>
            </div>
          </form>
        </section>
      </div>
    </main>
  );
}
