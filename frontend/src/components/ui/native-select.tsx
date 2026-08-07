import { ChevronDownIcon } from "lucide-react";
import type * as React from "react";
import { cn } from "@/lib/utils";

type NativeSelectProps = Omit<React.ComponentProps<"select">, "size"> & {
  size?: "sm" | "default";
};

export const NativeSelect = ({
  className,
  size = "default",
  ...props
}: NativeSelectProps) => {
  return (
    <div
      className={cn(
        "group/native-select relative w-full has-[select:disabled]:opacity-50",
        className,
      )}
      data-slot="native-select-wrapper"
      data-size={size}
    >
      <select
        data-slot="native-select"
        data-size={size}
        className="h-12 w-full min-w-0 appearance-none rounded-none border border-input bg-field px-3.5 py-2 pr-10 text-base text-field-foreground outline-none transition-[color,background-color,border-color,box-shadow] selection:bg-primary selection:text-primary-foreground focus-visible:border-ring focus-visible:bg-field-focus focus-visible:ring-1 focus-visible:ring-ring/30 disabled:pointer-events-none disabled:cursor-not-allowed aria-invalid:border-destructive aria-invalid:ring-1 aria-invalid:ring-destructive/30 data-[size=sm]:h-10"
        {...props}
      />

      <ChevronDownIcon
        aria-hidden="true"
        data-slot="native-select-icon"
        className="pointer-events-none absolute top-1/2 right-3 size-4 -translate-y-1/2 text-muted-foreground select-none"
      />
    </div>
  );
};

export const NativeSelectOption = ({
  className,
  ...props
}: React.ComponentProps<"option">) => {
  return (
    <option
      data-slot="native-select-option"
      className={cn("bg-[Canvas] text-[CanvasText]", className)}
      {...props}
    />
  );
};

export const NativeSelectOptGroup = ({
  className,
  ...props
}: React.ComponentProps<"optgroup">) => {
  return (
    <optgroup
      data-slot="native-select-optgroup"
      className={cn("bg-[Canvas] text-[CanvasText]", className)}
      {...props}
    />
  );
};
