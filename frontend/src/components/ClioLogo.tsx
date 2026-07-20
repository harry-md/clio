import Link from "next/link";

export function ClioLogo() {
  return (
    <Link
      href="/"
      className="flex items-center gap-3 text-white"
      aria-label="Clio home"
    >
      <span className="grid size-12 place-items-center border-2 border-[#e36f32] font-sans text-xl font-bold text-[#e36f32]">
        Clio
      </span>
    </Link>
  );
}
