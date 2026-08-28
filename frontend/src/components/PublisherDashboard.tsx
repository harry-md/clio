"use client";

import { PlusIcon, ShoppingBagIcon, WalletIcon } from "lucide-react";
import Image from "next/image";
import { useEffect, useState } from "react";
import { EmptyState } from "@/components/EmptyState";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { PublisherBookUploadForm } from "@/components/PublisherUploadBookForm";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { PublisherDashboardData } from "@/lib/types";

const moneyFormatter = new Intl.NumberFormat("en-US", {
  maximumFractionDigits: 0,
});

const numberFormatter = new Intl.NumberFormat("vi-VN");

const currentDate = new Date();
const defaultYear = currentDate.getFullYear();
const defaultMonth = currentDate.getMonth() + 1;

const months = Array.from({ length: 12 }, (_, index) => index + 1);
const years = Array.from({ length: 6 }, (_, index) => defaultYear - index);

export const PublisherDashboard = () => {
  const { initialized, user } = useAuth();
  const [month, setMonth] = useState(defaultMonth);
  const [year, setYear] = useState(defaultYear);
  const [dashboard, setDashboard] = useState<PublisherDashboardData | null>(
    null,
  );
  const [showUploadForm, setShowUploadForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!initialized || user?.role !== "PUBLISHER") {
      return;
    }

    let active = true;

    const loadDashboard = async () => {
      setLoading(true);
      setError("");

      try {
        const { data } = await Api.get<PublisherDashboardData>(
          "/publishers/current-publisher/dashboard",
          {
            params: {
              year,
              month,
            },
          },
        );

        if (active) {
          setDashboard(data);
        }
      } catch (requestError: unknown) {
        if (active) {
          setError(
            getApiErrorMessage(
              requestError,
              "Không thể tải thống kê nhà xuất bản.",
            ),
          );
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void loadDashboard();

    return () => {
      active = false;
    };
  }, [initialized, user, year, month]);

  if (!initialized || loading) {
    return <LoadingOverlay />;
  }

  if (!user) {
    return (
      <Alert variant="destructive">
        <AlertDescription>
          Bạn cần đăng nhập bằng tài khoản nhà xuất bản.
        </AlertDescription>
      </Alert>
    );
  }

  if (user.role !== "PUBLISHER") {
    return (
      <Alert variant="destructive">
        <AlertDescription>
          Tài khoản không có quyền truy cập trang này.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <>
      <div className="flex flex-col gap-5 border-b border-border pb-8 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-5xl font-semibold text-foreground">
          Tổng quan nhà xuất bản
        </h1>

        <Button
          type="button"
          size="lg"
          onClick={() => setShowUploadForm((current) => !current)}
        >
          <PlusIcon data-icon="inline-start" />

          {showUploadForm ? "Ẩn đăng sách" : "Đăng tải sách"}
        </Button>
      </div>

      {showUploadForm && (
        <section className="mt-8 border border-border-strong bg-card p-5 md:p-8">
          <h2 className="text-3xl font-semibold text-foreground">
            Đăng tải sách
          </h2>

          <PublisherBookUploadForm />
        </section>
      )}

      {error && (
        <Alert className="mt-8" variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {dashboard && (
        <>
          <div className="mt-8 flex flex-col gap-4 border-b border-border pb-6 md:flex-row md:items-end md:justify-between">
            <div>
              <p className="text-sm font-semibold text-muted-foreground">
                Kỳ thống kê
              </p>

              <p className="mt-1 text-2xl font-semibold text-foreground">
                Tháng {month}/{year}
              </p>
            </div>

            <div className="flex flex-wrap gap-3">
              <label className="flex items-center gap-2 text-sm text-muted-foreground">
                <span className="text-lg">Tháng</span>

                <select
                  value={month}
                  onChange={(event) => setMonth(Number(event.target.value))}
                  className="h-10 border border-border-strong bg-card px-3 text-foreground"
                >
                  {months.map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </select>
              </label>

              <label className="flex items-center gap-2 text-sm text-muted-foreground">
                <span className="text-lg">Năm</span>

                <select
                  value={year}
                  onChange={(event) => setYear(Number(event.target.value))}
                  className="h-10 border border-border-strong bg-card px-3 text-foreground"
                >
                  {years.map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </select>
              </label>
            </div>
          </div>

          <section className="mt-8 grid gap-5 md:grid-cols-2">
            <div className="border border-border-strong bg-card p-6 md:p-8">
              <div className="flex items-center gap-2 text-muted-foreground">
                <WalletIcon className="size-5" />

                <span className="text-sm font-semibold uppercase tracking-[0.16em]">
                  Số dư hiện tại
                </span>
              </div>

              <p className="mt-5 text-5xl font-semibold text-price">
                {moneyFormatter.format(Number(dashboard.publisher.balance))}đ
              </p>
            </div>

            <div className="border border-border-strong bg-card p-6 md:p-8">
              <div className="flex items-center gap-2 text-muted-foreground">
                <WalletIcon className="size-5" />

                <span className="text-sm font-semibold uppercase tracking-[0.16em]">
                  Tài khoản ngân hàng
                </span>
              </div>

              <p className="mt-5 break-all text-2xl font-semibold text-foreground">
                {dashboard.publisher.bankAccountNumber || "Chưa cập nhật"}
              </p>
            </div>
          </section>

          <section className="mt-12">
            <div className="flex items-end justify-between gap-5 border-b border-border pb-6">
              <div>
                <h2 className="mt-3 text-3xl font-semibold text-foreground">
                  Top 5 sách bán chạy
                </h2>
              </div>
            </div>

            {dashboard.topSellingBooks.length > 0 ? (
              <ol className="divide-y divide-border border-b border-border">
                {dashboard.topSellingBooks.map((book, index) => (
                  <li
                    key={book.bookId}
                    className="grid grid-cols-[2rem_4rem_minmax(0,1fr)_auto] items-center gap-4 py-5 sm:grid-cols-[3rem_5rem_minmax(0,1fr)_auto]"
                  >
                    <span className="text-2xl font-semibold text-subtle-foreground">
                      {String(index + 1).padStart(2, "0")}
                    </span>

                    <div className="relative aspect-2/3 w-16 overflow-hidden border border-border bg-muted sm:w-20">
                      {book.thumbnail ? (
                        <Image
                          src={book.thumbnail}
                          alt={`Bìa sách ${book.title}`}
                          fill
                          sizes="80px"
                          className="object-cover"
                        />
                      ) : (
                        <div className="flex h-full items-end p-2">
                          <span className="line-clamp-3 text-xs text-muted-foreground">
                            {book.title}
                          </span>
                        </div>
                      )}
                    </div>

                    <div className="min-w-0">
                      <h3 className="truncate text-lg font-semibold text-foreground sm:text-xl">
                        {book.title}
                      </h3>
                    </div>

                    <div className="text-right">
                      <span className="flex items-center justify-end gap-2 text-3xl font-semibold text-price">
                        <ShoppingBagIcon className="size-6" />
                        {numberFormatter.format(book.salesCount)}
                      </span>
                    </div>
                  </li>
                ))}
              </ol>
            ) : (
              <EmptyState
                className="mt-6"
                title="Không có đơn sách trong tháng này"
              />
            )}
          </section>
        </>
      )}
    </>
  );
};
