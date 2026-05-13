type FraudSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

const SEVERITY_CLASSES: Record<FraudSeverity, string> = {
  LOW: 'text-accent-light-green bg-accent-light-green/15',
  MEDIUM: 'text-accent-yellow bg-accent-yellow/15',
  HIGH: 'text-accent-warning bg-accent-warning/15',
  CRITICAL: 'text-accent-danger bg-accent-danger/15 font-bold',
};

interface FraudSeverityBadgeProps {
  severity: FraudSeverity;
}

export function FraudSeverityBadge({ severity }: FraudSeverityBadgeProps) {
  return (
    <span
      className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold tracking-wide ${SEVERITY_CLASSES[severity]}`}
    >
      {severity}
    </span>
  );
}
