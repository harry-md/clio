import Link from "next/link";

export const ClioLogo = () => {
  return (
    <Link
      href="/"
      className="flex items-center gap-3 text-foreground"
      aria-label="Trang chủ"
    >
      <span className="grid size-13 place-items-center border-2 border-primary bg-primary font-serif text-4xl font-bold text-foreground">
        C
      </span>
    </Link>
  );
};
