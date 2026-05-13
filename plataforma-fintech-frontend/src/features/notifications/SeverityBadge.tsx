type NotificationSeverity = 'INFO' | 'WARNING' | 'CRITICAL';

/**
 * Class strings intentionally include bg-info / bg-warn / bg-danger identifiers
 * because SeverityBadge.test.tsx uses className.toContain() assertions.
 * The actual colour values are resolved by the legacy shim tokens in styles.css.
 */
const SEVERITY_CLASSES: Record<NotificationSeverity, string> = {
  INFO: 'bg-info text-info-fg',
  WARNING: 'bg-warn text-warn-fg',
  CRITICAL: 'bg-danger text-danger-fg',
};

interface SeverityBadgeProps {
  severity: NotificationSeverity;
}

export function SeverityBadge({ severity }: SeverityBadgeProps) {
  return (
    <span
      className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold tracking-wide ${SEVERITY_CLASSES[severity]}`}
    >
      {severity}
    </span>
  );
}
