import type { InputHTMLAttributes, Ref } from 'react';
import type { LucideIcon } from 'lucide-react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  leftIcon?: LucideIcon;
  ref?: Ref<HTMLInputElement>;
}

export function Input({ className = '', leftIcon: LeftIcon, ref, ...props }: InputProps) {
  if (LeftIcon) {
    return (
      <div className="relative">
        <span className="absolute left-4 top-1/2 -translate-y-1/2 text-stone pointer-events-none">
          <LeftIcon size={16} strokeWidth={1.5} />
        </span>
        <input
          ref={ref}
          className={`w-full bg-canvas-light text-ink border border-hairline-light rounded-[12px] pl-10 pr-4 h-14 text-body-md focus:outline-none focus:border-brand focus:ring-1 focus:ring-brand focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-light ${className}`}
          {...props}
        />
      </div>
    );
  }

  return (
    <input
      ref={ref}
      className={`w-full bg-canvas-light text-ink border border-hairline-light rounded-[12px] px-4 h-14 text-body-md focus:outline-none focus:border-brand focus:ring-1 focus:ring-brand focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-light ${className}`}
      {...props}
    />
  );
}
