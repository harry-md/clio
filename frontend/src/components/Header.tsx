"use client";

import { SearchIcon, ShoppingCartIcon } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { Logo } from "@/components/Logo";
import { Button, buttonVariants } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";
import { useCart } from "@/context/CartContext";
import { Api } from "@/lib/api";
import { cn } from "@/lib/utils";

const navigationItems = [
  {
    href: "/",
    label: "Trang chủ",
  },
  {
    href: "/subscriptions",
    label: "Gói đọc",
  },
  {
    href: "/library",
    label: "Thư viện",
  },
] as const;

export const Header = () => {
  const router = useRouter();
  const pathname = usePathname();
  const { user, initialized, setUser } = useAuth();
  const { books, clearCart } = useCart();
  const [navigating, setNavigating] = useState(false);

  const handleOpenSearch = () => {
    if (pathname === "/search") {
      return;
    }

    setNavigating(true);
    router.push("/search");
  };

  const handleLogout = async () => {
    try {
      await Api.post("/logout");
    } finally {
      clearCart();
      setUser(null);
      router.replace("/");
    }
  };

  return (
    <>
      {navigating && <LoadingOverlay />}

      <header className="sticky top-0 z-40 border-b border-border bg-background/95 backdrop-blur-sm">
        <div className="mx-auto flex min-h-18 max-w-360 items-center gap-6 px-5 lg:px-10">
          <Logo />

          <nav className="hidden items-center gap-7 lg:flex">
            {navigationItems.map((item) => {
              const isActive =
                item.href === "/"
                  ? pathname === "/"
                  : pathname === item.href ||
                    pathname.startsWith(`${item.href}/`);

              return (
                <Link
                  key={item.href}
                  href={item.href}
                  aria-current={isActive ? "page" : undefined}
                  className={cn(
                    "flex min-h-18 items-center border-b-2 border-transparent text-sm font-semibold text-muted-foreground transition-colors hover:text-foreground",
                    isActive && "border-ring text-foreground",
                  )}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>

          <Button
            type="button"
            variant="outline"
            onClick={handleOpenSearch}
            className="ml-auto hidden w-full max-w-md cursor-text justify-start border-border-strong bg-card px-3 text-left font-normal text-subtle-foreground hover:bg-card hover:text-foreground md:flex"
          >
            <SearchIcon className="size-4 text-muted-foreground" />

            <span className="cursor-text">Tìm kiếm sách...</span>
          </Button>
          <Link
            href="/cart"
            aria-label={`Giỏ hàng có ${books.length} cuốn sách`}
            className={cn(
              buttonVariants({
                variant: "ghost",
                size: "icon",
              }),
              "relative",
            )}
          >
            <ShoppingCartIcon aria-hidden="true" />
            {books.length > 0 && (
              <span className="absolute -right-1 -top-1 grid min-h-5 min-w-5 place-items-center bg-primary px-1 text-xs font-bold text-primary-foreground">
                {books.length}
              </span>
            )}
          </Link>

          {!initialized ? (
            <div aria-hidden="true" className="h-10 w-36" />
          ) : user ? (
            <div className="flex items-center gap-3">
              <div
                className={cn(
                  "relative size-9 shrink-0 overflow-hidden border bg-secondary",
                  user.isSubscribed ? "border-primary" : "border-border-strong",
                )}
              >
                {user.avatar ? (
                  <Image
                    src={user.avatar}
                    alt={`Avatar ${user.firstName} ${user.lastName}`}
                    fill
                    sizes="36px"
                    className="object-cover"
                  />
                ) : (
                  <span className="grid h-full place-items-center text-sm font-semibold text-secondary-foreground">
                    {user.firstName}
                  </span>
                )}
              </div>

              <div className="hidden xl:block">
                <p
                  className={cn(
                    "font-semibold",
                    user.isSubscribed ? "text-primary" : "text-foreground",
                  )}
                >
                  {user.firstName} {user.lastName}
                </p>

                <Button
                  type="button"
                  variant="link"
                  onClick={handleLogout}
                  className="h-auto justify-start px-0 text-sm"
                >
                  Đăng xuất
                </Button>
              </div>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                href="/login"
                className={cn(
                  buttonVariants({ variant: "outline" }),
                  "hidden sm:inline-flex",
                )}
              >
                Đăng nhập
              </Link>

              <Link
                href="/register"
                className={buttonVariants({ variant: "default" })}
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
