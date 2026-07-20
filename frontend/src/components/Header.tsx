"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import { ClioLogo } from "@/components/ClioLogo";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { Api } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";

export const Header = () => {
  const router = useRouter();
  const pathname = usePathname();
  const { user, initialized, setUser } = useAuth();
  const [isNavigating, setIsNavigating] = useState(false);

  const handleOpenSearch = () => {
    if (pathname === "/search") {
      return;
    }

    setIsNavigating(true);
    router.push("/search");
  };

  const handleLogout = async () => {
    try {
      await Api.post("/logout");
    } finally {
      setUser(null);
      router.replace("/");
    }
  };

  return (
    <>
      {isNavigating && <LoadingOverlay label="Đang mở trang tìm kiếm..." />}
      <header className="sticky top-0 z-40 border-b border-[#343432] bg-[#151515]/95 backdrop-blur-sm">
        <div className="mx-auto flex min-h-18 max-w-360 items-center gap-6 px-5 lg:px-10">
          <ClioLogo />

          <nav className="hidden items-center gap-7 lg:flex">
            <Link className="text-lg nav-link nav-link-active" href="/">
              Khám phá
            </Link>
            <Link className="text-lg nav-link" href="/library">
              Thư viện
            </Link>
            <Link className="text-lg nav-link" href="/subscriptions">
              Gói đọc
            </Link>
          </nav>

          <button
            type="button"
            onClick={handleOpenSearch}
            className="cursor-text ml-auto hidden h-10 w-full max-w-md items-center border border-[#41413e] bg-[#1d1d1c] text-left transition hover:border-[#6d9fc9] md:flex"
          >
            <svg
              aria-hidden="true"
              viewBox="0 0 24 24"
              className="ml-3 size-4 fill-none stroke-[#8f8e89] stroke-2"
            >
              <circle cx="11" cy="11" r="7" />
              <path d="m20 20-4-4" />
            </svg>

            <span className="cursor-text px-3 text-sm text-[#777671]">
              Tìm kiếm sách...
            </span>
          </button>

          {!initialized ? (
            <div aria-hidden="true" className="h-10 w-36" />
          ) : user ? (
            <div className="flex items-center gap-3">
              <div
                className="size-9 border rounded-2xl border-[#484844] bg-[#292927] bg-cover bg-center"
                style={
                  user.avatar
                    ? { backgroundImage: `url("${user.avatar}")` }
                    : undefined
                }
              >
                {!user.avatar && (
                  <span
                    className="grid h-full place-items-center text-sm
                                font-semibold text-[#d8d6cf]"
                  >
                    {user.firstName.charAt(0).toUpperCase()}
                  </span>
                )}
              </div>

              <div className="hidden xl:block">
                <p className="text-sm font-medium text-[#eceae4]">
                  {user.firstName} {user.lastName}
                </p>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="text-left text-xs text-[#8ebce2] hover:text-white"
                >
                  Đăng xuất
                </button>
              </div>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                href="/login"
                className="hidden h-10 items-center border border-[#484844] px-4 text-sm font-semibold text-[#e8e6df] transition hover:border-[#777771] sm:flex"
              >
                Đăng nhập
              </Link>

              <Link
                href="/register"
                className="flex h-10 items-center bg-[#e06f32] px-4 text-sm font-bold text-[#161616] transition hover:bg-[#f08243]"
              >
                Đăng ký
              </Link>
            </div>
          )}
        </div>
      </header>
    </>
  );
};
