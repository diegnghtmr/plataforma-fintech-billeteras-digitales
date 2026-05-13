type FraudSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

const SEVERITY_CLASSES: Record<FraudSeverity, string> = {
  LOW: 'bg-info text-info-fg',
  MEDIUM: 'bg-warn text-warn-fg',
  HIGH: 'bg-danger text-danger-fg',
  CRITICAL: 'bg-danger text-danger-fg font-bold',
};

interface FraudSeverityBadgeProps {
  severity: FraudSeverity;
}

export function FraudSeverityBadge({ severity }: FraudSeverityBadgeProps) {
  return (
    <span className={`px-2 py-0.5 rounded text-xs font-semibold ${SEVERITY_CLASSES[severity]}`}>
      {severity}
    </span>
  );
}
