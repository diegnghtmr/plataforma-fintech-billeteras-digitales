import type { ReactNode } from 'react';
import type { LucideIcon } from 'lucide-react';
import { Inbox } from 'lucide-react';

interface EmptyStateProps {
  icon?: LucideIcon;
  title: string;
  description?: string;
  action?: ReactNode;
  /** "light" (default) for white/canvas-light surfaces; "dark" for canvas-dark. */
  tone?: 'light' | 'dark';
}

export function EmptyState({
  icon: Icon = Inbox,
  title,
  description,
  action,
  tone = 'light',
}: EmptyStateProps) {
  const isDark = tone === 'dark';
  return (
    <div className="flex flex-col items-center justify-center py-16 px-6 text-center gap-4">
      <div
        className={`flex items-center justify-center w-16 h-16 rounded-full ${
          isDark ? 'bg-surface-elevated' : 'bg-surface-soft'
        }`}
      >
        <Icon
          size={28}
          className={isDark ? 'text-on-dark' : 'text-stone'}
          strokeWidth={1.5}
        />
      </div>
      <div className="flex flex-col gap-1.5 w-full max-w-[20rem]">
        <p className={`text-heading-sm ${isDark ? 'text-on-dark' : 'text-ink'}`}>
          {title}
        </p>
        {description && (
          <p className={`text-body-sm ${isDark ? 'text-on-dark-mute' : 'text-stone'}`}>
            {description}
          </p>
        )}
      </div>
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
}
