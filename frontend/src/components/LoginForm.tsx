"use client";

import { isAxiosError } from "axios";
import { useRouter } from "next/navigation";
import { type SubmitEvent, useState } from "react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { AuthUser } from "@/lib/types";

interface LoginFormFields {
  username: string;
  password: string;
}

const initialForm: LoginFormFields = {
  username: "",
  password: "",
};

const MAX_FIELD_LENGTH = 255;

const validate = (form: LoginFormFields) => {
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

export const LoginForm = () => {
  const router = useRouter();
  const { refreshUser } = useAuth();

  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");

    const validationError = validate(form);
    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setLoading(true);
      await Api.post<AuthUser>("/login", {
        username: form.username.trim(),
        password: form.password,
      });

      await refreshUser();
      router.replace("/");
    } catch (error: unknown) {
      if (isAxiosError(error) && !error.response) {
        setError("Không thể kết nối tới máy chủ.");
      } else {
        setError(
          getApiErrorMessage(
            error,
            "Tên đăng nhập hoặc mật khẩu không chính xác!",
          ),
        );
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="mt-9 grid gap-y-6" onSubmit={handleLogin}>
      <Field>
        <FieldLabel htmlFor="username">Tên đăng nhập</FieldLabel>

        <Input
          id="username"
          name="username"
          required
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
  );
};
