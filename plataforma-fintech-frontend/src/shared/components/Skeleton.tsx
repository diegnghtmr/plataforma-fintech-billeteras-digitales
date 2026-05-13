import type { HTMLAttributes } from 'react';

interface SkeletonProps extends HTMLAttributes<HTMLDivElement> {
  className?: string;
}

export function Skeleton({ className = '', ...props }: SkeletonProps) {
  return (
    <div
      className={`animate-pulse bg-surface-soft rounded-md ${className}`}
      aria-hidden="true"
      {...props}
    />
  );
}

export function SkeletonCard() {
  return (
    <div className="bg-surface-card border border-hairline-light rounded-[20px] p-8 flex flex-col gap-4">
      <Skeleton className="h-6 w-1/3" />
      <Skeleton className="h-4 w-2/3" />
      <Skeleton className="h-4 w-1/2" />
    </div>
  );
}

export function SkeletonRow() {
  return (
    <tr aria-hidden="true">
      {Array.from({ length: 6 }).map((_, i) => (
        <td key={i} className="py-3 px-3">
          <Skeleton className="h-4 w-full" />
        </td>
      ))}
    </tr>
  );
}

export function SkeletonPage() {
  return (
    <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px] flex flex-col gap-8">
      <div className="flex flex-col gap-3">
        <Skeleton className="h-12 w-64" />
        <Skeleton className="h-5 w-96" />
      </div>
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
      </div>
    </div>
  );
}
