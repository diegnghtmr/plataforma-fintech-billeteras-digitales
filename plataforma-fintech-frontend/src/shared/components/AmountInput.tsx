import { type InputHTMLAttributes, type KeyboardEvent, type ClipboardEvent, type Ref, type FormEvent } from 'react';
import { DollarSign } from 'lucide-react';

const NUMERIC_PATTERN = /^\d*\.?\d*$/;
const ALLOWED_CONTROL_KEYS = new Set([
  'Backspace',
  'Delete',
  'Tab',
  'Escape',
  'Enter',
  'Home',
  'End',
  'ArrowLeft',
  'ArrowRight',
  'ArrowUp',
  'ArrowDown',
]);

type AmountInputProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'type' | 'inputMode'> & {
  ref?: Ref<HTMLInputElement>;
};

function isAllowedKey(e: KeyboardEvent<HTMLInputElement>, currentValue: string): boolean {
  if (ALLOWED_CONTROL_KEYS.has(e.key)) return true;
  if (e.ctrlKey || e.metaKey) return true; // Ctrl/Cmd+A/C/V/X/Z
  if (/^\d$/.test(e.key)) return true;
  if (e.key === '.' && !currentValue.includes('.')) return true;
  return false;
}

export function AmountInput({
  className = '',
  onKeyDown,
  onPaste,
  onInput,
  ref,
  ...rest
}: AmountInputProps) {
  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    const input = e.currentTarget;
    if (!isAllowedKey(e, input.value)) {
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

  // Safety net — strips invalid chars introduced by drag-drop, autofill, or other paths
  function handleInput(e: FormEvent<HTMLInputElement>) {
    const input = e.currentTarget;
    const cleaned = input.value
      .replace(/[^\d.]/g, '')
      .replace(/^(\d*\.\d*).*$/, '$1'); // keep only first dot
    if (cleaned !== input.value) {
      input.value = cleaned;
    }
    onInput?.(e);
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
        type="text"
        inputMode="decimal"
        autoComplete="off"
        onKeyDown={handleKeyDown}
        onPaste={handlePaste}
        onInput={handleInput}
        onWheel={handleWheel}
        className={`w-full bg-canvas-light text-ink border border-hairline-light rounded-[12px] pl-10 pr-4 h-14 text-body-md focus:outline-none focus:border-brand focus:ring-1 focus:ring-brand focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-light ${className}`}
        {...rest}
      />
    </div>
  );
}
