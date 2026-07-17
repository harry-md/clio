import Link from "next/link";

export function ClioLogo() {
  return (
    <Link
      href="/"
      className="flex items-center gap-3 text-white"
      aria-label="Clio home"
    >
      <span className="grid size-9 place-items-center border border-[#e36f32] font-serif text-xl font-semibold text-[#e36f32]">
        C
      </span>

      <span className="font-serif text-2xl font-semibold tracking-[0.08em]">
        CLIO
      </span>
    </Link>
  );
}
