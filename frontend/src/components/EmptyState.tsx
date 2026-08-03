import { cn } from "@/lib/utils";

interface EmptyStateProps {
  title: string;
  description?: string;
  className?: string;
}

export function EmptyState({ title, description, className }: EmptyStateProps) {
  return (
    <div className={cn("border-y border-border py-20 text-center", className)}>
      <p className="text-xl text-secondary-foreground">{title}</p>

      {description && (
        <p className="mx-auto mt-2 max-w-xl text-sm text-muted-foreground">
          {description}
        </p>
      )}
    </div>
  );
}
