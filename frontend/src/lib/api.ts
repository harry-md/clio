import axios, { AxiosError } from "axios";

interface ApiError {
  message?: string;
}

export const Api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  withCredentials: true,
  paramsSerializer: {
    indexes: null,
  },
});

export const getApiErrorMessage = (
  error: unknown,
  fallback = "Có lỗi xảy ra!",
) => {
  if (error instanceof AxiosError) {
    return (error.response?.data as ApiError | undefined)?.message ?? fallback;
  }
  return fallback;
};
