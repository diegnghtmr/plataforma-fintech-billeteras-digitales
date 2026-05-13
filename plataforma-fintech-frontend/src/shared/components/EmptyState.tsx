import type { ReactNode } from 'react';
import type { LucideIcon } from 'lucide-react';
import { Inbox } from 'lucide-react';

interface EmptyStateProps {
  icon?: LucideIcon;
  title: string;
  description?: string;
  action?: ReactNode;
}

export function EmptyState({
  icon: Icon = Inbox,
  title,
  description,
  action,
}: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-6 text-center gap-4">
      <div className="flex items-center justify-center w-16 h-16 rounded-full bg-surface-soft">
        <Icon size={28} className="text-stone" strokeWidth={1.5} />
      </div>
      <div className="flex flex-col gap-1.5 max-w-xs">
        <p className="text-heading-sm text-ink">{title}</p>
        {description && (
          <p className="text-body-sm text-stone">{description}</p>
        )}
      </div>
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
}
