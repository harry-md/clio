import Image from "next/image";
import Link from "next/link";

export const Logo = () => {
  return (
    <Link
      href="/"
      className="flex items-center gap-0.5 text-foreground"
      aria-label="Trang chủ"
    >
      <Image src="/book.svg" alt="Logo" width={32} height={32} />
      <span className="place-items-center text-3xl font-semibold text-foreground">
        Clio
      </span>
    </Link>
  );
};
