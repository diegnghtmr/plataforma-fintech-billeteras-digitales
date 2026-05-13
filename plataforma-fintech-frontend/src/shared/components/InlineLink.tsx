import { Link } from 'react-router-dom';
import type { LinkProps } from 'react-router-dom';

type InlineLinkProps =
  | (LinkProps & { tone?: 'default' })
  | (LinkProps & { tone: 'dark' });

/**
 * Design-system-aligned inline navigational link.
 * tone="default" — for use on light canvases.
 * tone="dark"    — for use on dark canvas backgrounds.
 */
export function InlineLink({ className = '', tone = 'default', ...props }: InlineLinkProps & { tone?: 'default' | 'dark' }) {
  const baseStyles =
    'font-medium underline decoration-1 underline-offset-4 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-light rounded-sm';

  const toneStyles =
    tone === 'dark'
      ? 'text-on-dark decoration-hairline-dark hover:decoration-on-dark'
      : 'text-ink decoration-hairline-strong hover:decoration-ink';

  return (
    <Link
      {...props}
      className={`${baseStyles} ${toneStyles} ${className}`}
    />
  );
}
