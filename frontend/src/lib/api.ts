import axios, { AxiosError } from "axios";

type ApiError = {
  message?: string;
};

export const Api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api",
  withCredentials: true,
  paramsSerializer: {
    indexes: null,
  },
});

export function getApiErrorMessage(
  error: unknown,
  fallback = "Có lỗi xảy ra!",
) {
  if (error instanceof AxiosError) {
    return (error.response?.data as ApiError | undefined)?.message ?? fallback;
  }
  return fallback;
}
