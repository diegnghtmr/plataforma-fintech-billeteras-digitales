type NotificationSeverity = 'INFO' | 'WARNING' | 'CRITICAL';

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
    <span className={`px-2 py-0.5 rounded text-xs font-semibold ${SEVERITY_CLASSES[severity]}`}>
      {severity}
    </span>
  );
}
