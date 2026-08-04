import { cn } from "@/lib/utils";

interface SpinnerProps {
  className?: string;
}

const spokes = [
  { angle: 0, opacity: 1 },
  { angle: 30, opacity: 0.92 },
  { angle: 60, opacity: 0.84 },
  { angle: 90, opacity: 0.76 },
  { angle: 120, opacity: 0.68 },
  { angle: 150, opacity: 0.6 },
  { angle: 180, opacity: 0.52 },
  { angle: 210, opacity: 0.44 },
  { angle: 240, opacity: 0.36 },
  { angle: 270, opacity: 0.28 },
  { angle: 300, opacity: 0.2 },
  { angle: 330, opacity: 0.12 },
] as const;

export const Spinner = ({ className }: SpinnerProps) => {
  return (
    <output aria-label="Đang tải" className="inline-flex">
      <svg
        aria-hidden="true"
        viewBox="0 0 50 50"
        className={cn(
          "size-14 animate-spin text-foreground motion-reduce:animate-none",
          className,
        )}
      >
        {spokes.map((spoke) => (
          <line
            key={spoke.angle}
            x1="25"
            y1="5"
            x2="25"
            y2="14"
            stroke="currentColor"
            strokeWidth="5"
            strokeLinecap="round"
            opacity={spoke.opacity}
            transform={`rotate(${spoke.angle} 25 25)`}
          />
        ))}
      </svg>
    </output>
  );
};
