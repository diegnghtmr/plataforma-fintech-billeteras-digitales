import { Info, AlertTriangle, AlertOctagon } from 'lucide-react';

type NotificationSeverity = 'INFO' | 'WARNING' | 'CRITICAL';

/**
 * Class strings intentionally include bg-info / bg-warn / bg-danger identifiers
 * because SeverityBadge.test.tsx uses className.toContain() assertions.
 * The actual colour values are resolved by the legacy shim tokens in styles.css.
 */
const SEVERITY_CONFIG: Record<NotificationSeverity, { className: string; Icon: typeof Info }> = {
  INFO: { className: 'bg-info text-info-fg', Icon: Info },
  WARNING: { className: 'bg-warn text-warn-fg', Icon: AlertTriangle },
  CRITICAL: { className: 'bg-danger text-danger-fg', Icon: AlertOctagon },
};

interface SeverityBadgeProps {
  severity: NotificationSeverity;
}

export function SeverityBadge({ severity }: SeverityBadgeProps) {
  const { className, Icon } = SEVERITY_CONFIG[severity];
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold tracking-wide ${className}`}
    >
      <Icon size={12} strokeWidth={2} />
      {severity}
    </span>
  );
}
