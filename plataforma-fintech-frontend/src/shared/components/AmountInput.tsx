import { type InputHTMLAttributes, type KeyboardEvent, type ClipboardEvent, type Ref } from 'react';
import { DollarSign } from 'lucide-react';

const BLOCKED_KEYS = new Set(['e', 'E', '+', '-', ',']);
const NUMERIC_PATTERN = /^\d*\.?\d*$/;

type AmountInputProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'type' | 'inputMode'> & {
  ref?: Ref<HTMLInputElement>;
};

export function AmountInput({
  className = '',
  onKeyDown,
  onPaste,
  ref,
  ...rest
}: AmountInputProps) {
  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (BLOCKED_KEYS.has(e.key)) {
      e.preventDefault();
    }
    onKeyDown?.(e);
  }

  function handlePaste(e: ClipboardEvent<HTMLInputElement>) {
    const text = e.clipboardData.getData('text');
    if (!NUMERIC_PATTERN.test(text)) {
      e.preventDefault();
    }
    onPaste?.(e);
  }

  function handleWheel() {
    if (ref && 'current' in ref && ref.current) {
      ref.current.blur();
    }
  }

  return (
    <div className="relative">
      <span className="absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none text-stone">
        <DollarSign size={16} strokeWidth={1.5} />
      </span>
      <input
        ref={ref}
        type="number"
        inputMode="decimal"
        step="0.01"
        min="0"
        onKeyDown={handleKeyDown}
        onPaste={handlePaste}
        onWheel={handleWheel}
        className={`w-full bg-canvas-light text-ink border border-hairline-light rounded-[12px] pl-10 pr-4 h-14 text-body-md focus:outline-none focus:border-brand focus:ring-1 focus:ring-brand focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-light ${className}`}
        {...rest}
      />
    </div>
  );
}
