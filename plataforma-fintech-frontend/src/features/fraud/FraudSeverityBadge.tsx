import { Info, AlertTriangle, AlertOctagon, ShieldAlert } from 'lucide-react';

type FraudSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

const SEVERITY_CONFIG: Record<FraudSeverity, { className: string; Icon: typeof Info }> = {
  LOW: { className: 'text-accent-light-green bg-accent-light-green/15', Icon: Info },
  MEDIUM: { className: 'text-accent-yellow bg-accent-yellow/15', Icon: AlertTriangle },
  HIGH: { className: 'text-accent-warning bg-accent-warning/15', Icon: AlertOctagon },
  CRITICAL: { className: 'text-accent-danger bg-accent-danger/15 font-bold', Icon: ShieldAlert },
};

interface FraudSeverityBadgeProps {
  severity: FraudSeverity;
}

export function FraudSeverityBadge({ severity }: FraudSeverityBadgeProps) {
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
