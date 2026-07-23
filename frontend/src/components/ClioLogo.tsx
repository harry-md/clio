import Link from "next/link";

export function ClioLogo() {
  return (
    <Link
      href="/"
      className="flex items-center gap-3 text-white"
      aria-label="Clio home"
    >
      <span className="grid size-13 place-items-center border-2 border-[#e36f32] bg-[#e36f32] font-serif text-xl font-bold">
        Clio
      </span>
    </Link>
  );
}
