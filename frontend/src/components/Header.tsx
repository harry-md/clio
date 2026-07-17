"use client";

import Link from "next/link";
import { SubmitEvent, useEffect, useState } from "react";
import { ClioLogo } from "@/components/ClioLogo";
import { Api } from "@/lib/api";
import type { AuthUser } from "@/lib/types";

type SiteHeaderProps = {
  onSearch?: (value: string) => void;
};

export function SiteHeader({ onSearch }: SiteHeaderProps) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [search, setSearch] = useState("");

  useEffect(() => {
    let active = true;

    Api.get<AuthUser>("/current-user")
      .then(({ data }) => {
        if (active) setUser(data);
      })
      .catch(() => {
        if (active) setUser(null);
      });

    return () => {
      active = false;
    };
  }, []);

  const handleSearch = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();
    onSearch?.(search.trim());
  };

  const handleLogout = async () => {
    await Api.post("/logout");
    setUser(null);
  };

  return (
    <header className="sticky top-0 z-40 border-b border-[#343432] bg-[#151515]/95 backdrop-blur-sm">
      <div className="mx-auto flex min-h-18 max-w-360 items-center gap-6 px-5 lg:px-10">
        <ClioLogo />

        <nav className="hidden items-center gap-7 lg:flex">
          <Link className="nav-link nav-link-active" href="/">
            Khám phá
          </Link>
          <Link className="nav-link" href="/library">
            Thư viện
          </Link>
          <Link className="nav-link" href="/subscriptions">
            Gói đọc
          </Link>
        </nav>

        <form
          className="ml-auto hidden w-full max-w-md md:block"
          onSubmit={handleSearch}
        >
          <label className="flex h-10 items-center border border-[#41413e] bg-[#1d1d1c] focus-within:border-[#6d9fc9]">
            <svg
              aria-hidden="true"
              viewBox="0 0 24 24"
              className="ml-3 size-4 fill-none stroke-[#8f8e89] stroke-2"
            >
              <circle cx="11" cy="11" r="7" />
              <path d="m20 20-4-4" />
            </svg>

            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Tìm sách hoặc tác giả"
              className="h-full min-w-0 flex-1 bg-transparent px-3 text-sm text-white outline-none placeholder:text-[#777671]"
            />
          </label>
        </form>

        {user ? (
          <div className="flex items-center gap-3">
            <div
              className="size-9 border border-[#484844] bg-[#292927] bg-cover bg-center"
              style={
                user.avatar
                  ? { backgroundImage: `url("${user.avatar}")` }
                  : undefined
              }
            >
              {!user.avatar && (
                <span className="grid h-full place-items-center text-sm font-semibold text-[#d8d6cf]">
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
  );
}
