import { useState, useRef, useId, useEffect } from 'react';
import { ChevronDown, Loader2 } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';

export interface SearchSelectOption {
  value: string;
  label: string;
  description?: string;
}

export interface SearchSelectProps {
  options: SearchSelectOption[];
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  emptyMessage?: string;
  disabled?: boolean;
  isLoading?: boolean;
  leftIcon?: LucideIcon;
  'aria-label'?: string;
  id?: string;
}

export function SearchSelect({
  options,
  value,
  onChange,
  placeholder = 'Seleccionar...',
  emptyMessage = 'Sin resultados',
  disabled = false,
  isLoading = false,
  leftIcon: LeftIcon,
  'aria-label': ariaLabel,
  id,
}: SearchSelectProps) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [activeIndex, setActiveIndex] = useState(-1);

  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const listboxRef = useRef<HTMLUListElement>(null);

  const listboxId = useId();
  const inputId = id ?? useId();

  const selectedOption = options.find((o) => o.value === value);

  const filtered = query.trim()
    ? options.filter((o) => {
        const q = query.toLowerCase();
        return (
          o.label.toLowerCase().includes(q) ||
          o.value.toLowerCase().includes(q) ||
          (o.description?.toLowerCase().includes(q) ?? false)
        );
      })
    : options;

  function openDropdown() {
    if (disabled) return;
    setOpen(true);
    setQuery('');
    setActiveIndex(-1);
  }

  function closeDropdown(commit = false) {
    setOpen(false);
    setQuery('');
    setActiveIndex(-1);
    if (!commit) return;
    if (activeIndex >= 0 && filtered[activeIndex]) {
      onChange(filtered[activeIndex].value);
    }
  }

  function selectOption(optionValue: string) {
    onChange(optionValue);
    setOpen(false);
    setQuery('');
    setActiveIndex(-1);
  }

  function handleTriggerClick() {
    if (open) {
      closeDropdown();
    } else {
      openDropdown();
    }
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    switch (e.key) {
      case 'ArrowDown': {
        e.preventDefault();
        setActiveIndex((prev) => Math.min(prev + 1, filtered.length - 1));
        break;
      }
      case 'ArrowUp': {
        e.preventDefault();
        setActiveIndex((prev) => Math.max(prev - 1, -1));
        break;
      }
      case 'Enter': {
        e.preventDefault();
        if (activeIndex >= 0 && filtered[activeIndex]) {
          selectOption(filtered[activeIndex].value);
        }
        break;
      }
      case 'Escape': {
        e.preventDefault();
        closeDropdown();
        break;
      }
      case 'Tab': {
        closeDropdown();
        break;
      }
    }
  }

  // Auto-focus search input when dropdown opens
  useEffect(() => {
    if (open) {
      inputRef.current?.focus();
    }
  }, [open]);

  // Scroll active option into view
  useEffect(() => {
    if (!open || activeIndex < 0) return;
    const listbox = listboxRef.current;
    if (!listbox) return;
    const active = listbox.children[activeIndex] as HTMLElement | undefined;
    active?.scrollIntoView?.({ block: 'nearest' });
  }, [activeIndex, open]);

  // Click outside closes
  useEffect(() => {
    if (!open) return;
    function handleMouseDown(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        closeDropdown();
      }
    }
    document.addEventListener('mousedown', handleMouseDown);
    return () => document.removeEventListener('mousedown', handleMouseDown);
  }, [open]);

  const activeOptionId =
    open && activeIndex >= 0 && filtered[activeIndex]
      ? `${listboxId}-option-${activeIndex}`
      : undefined;

  const triggerBase =
    'w-full bg-canvas-light text-ink border border-hairline-light rounded-[12px] h-14 text-body-md focus:outline-none focus:border-brand focus:ring-1 focus:ring-brand focus-visible:ring-2 focus-visible:ring-brand flex items-center cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed';

  return (
    <div ref={containerRef} className="relative">
      {/* Trigger button — visible when closed */}
      {!open && (
        <button
          type="button"
          id={inputId}
          role="combobox"
          aria-expanded={false}
          aria-controls={listboxId}
          aria-label={ariaLabel}
          aria-haspopup="listbox"
          disabled={disabled}
          onClick={handleTriggerClick}
          className={`${triggerBase} ${LeftIcon ? 'pl-10 pr-10' : 'px-4 pr-10'}`}
        >
          {LeftIcon && (
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-stone pointer-events-none">
              <LeftIcon size={16} strokeWidth={1.5} />
            </span>
          )}
          <span className={selectedOption ? 'text-ink' : 'text-stone'}>
            {selectedOption ? selectedOption.label : placeholder}
          </span>
          <span className="absolute right-3 top-1/2 -translate-y-1/2 text-stone pointer-events-none">
            <ChevronDown size={16} strokeWidth={1.5} />
          </span>
        </button>
      )}

      {/* Search input — visible when open */}
      {open && (
        <div className="relative">
          {LeftIcon && (
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-stone pointer-events-none z-10">
              <LeftIcon size={16} strokeWidth={1.5} />
            </span>
          )}
          <input
            ref={inputRef}
            id={inputId}
            role="combobox"
            aria-expanded={true}
            aria-controls={listboxId}
            aria-label={ariaLabel}
            aria-haspopup="listbox"
            aria-autocomplete="list"
            aria-activedescendant={activeOptionId}
            autoComplete="off"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setActiveIndex(-1);
            }}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            disabled={disabled}
            className={`w-full bg-canvas-light text-ink border border-hairline-light border-brand ring-1 ring-brand rounded-[12px] h-14 text-body-md focus:outline-none pr-10 ${LeftIcon ? 'pl-10' : 'px-4'}`}
          />
          <span className="absolute right-3 top-1/2 -translate-y-1/2 text-stone pointer-events-none">
            <ChevronDown size={16} strokeWidth={1.5} className="rotate-180" />
          </span>
        </div>
      )}

      {/* Dropdown */}
      {open && (
        <ul
          ref={listboxRef}
          id={listboxId}
          role="listbox"
          aria-label={ariaLabel}
          className="absolute z-50 top-[calc(100%+4px)] left-0 right-0 bg-canvas-light border border-hairline-light rounded-lg shadow-lg max-h-60 overflow-auto py-2"
        >
          {isLoading ? (
            <li className="flex items-center gap-2 px-4 py-2.5 text-stone text-body-sm">
              <Loader2 size={14} className="animate-spin" />
              Cargando...
            </li>
          ) : filtered.length === 0 ? (
            <li className="px-4 py-2.5 text-stone text-body-sm">{emptyMessage}</li>
          ) : (
            filtered.map((option, index) => {
              const isSelected = option.value === value;
              const isActive = index === activeIndex;
              return (
                <li
                  key={option.value}
                  id={`${listboxId}-option-${index}`}
                  role="option"
                  aria-selected={isSelected}
                  onMouseDown={(e) => {
                    // prevent input blur before we handle the click
                    e.preventDefault();
                    selectOption(option.value);
                  }}
                  onMouseEnter={() => setActiveIndex(index)}
                  className={`flex items-center justify-between px-4 py-2.5 cursor-pointer gap-3 ${
                    isActive
                      ? 'bg-surface-soft'
                      : isSelected
                        ? 'bg-surface-soft'
                        : 'hover:bg-surface-soft'
                  }`}
                >
                  <div className="flex flex-col min-w-0">
                    <span className="text-ink text-body-sm font-medium truncate">
                      {option.label}
                    </span>
                    {option.description && (
                      <span className="text-stone text-caption truncate">
                        {option.description}
                      </span>
                    )}
                  </div>
                  <span className="shrink-0 font-mono text-caption text-stone bg-surface-soft px-1.5 py-0.5 rounded">
                    {option.value}
                  </span>
                </li>
              );
            })
          )}
        </ul>
      )}
    </div>
  );
}
