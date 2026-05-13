import { Card } from '../../shared/components/Card';
import { Button } from '../../shared/components/Button';
import { LoyaltyBadge } from '../points/LoyaltyBadge';
import type { UserResponse } from '../../api/users';

interface UserCardProps {
  user: UserResponse;
  onSelect?: (id: string) => void;
}

export function UserCard({ user, onSelect }: UserCardProps) {
  return (
    <Card variant="light" className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <span className="text-ink font-semibold text-base">{user.name}</span>
        <div className="flex items-center gap-2">
          <LoyaltyBadge level={user.loyaltyLevel} />
          {onSelect && (
            <Button variant="pill-sm" onClick={() => onSelect(user.id)}>
              Seleccionar
            </Button>
          )}
        </div>
      </div>
      <dl className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
        <dt className="text-stone font-medium">ID</dt>
        <dd className="text-ink font-medium">{user.id}</dd>
        <dt className="text-stone font-medium">Email</dt>
        <dd className="text-ink">{user.email}</dd>
        <dt className="text-stone font-medium">Registrado</dt>
        <dd className="text-ink">{new Date(user.registeredAt).toLocaleDateString()}</dd>
        <dt className="text-stone font-medium">Puntos</dt>
        <dd className="text-ink font-semibold">{user.points}</dd>
        <dt className="text-stone font-medium">Billeteras</dt>
        <dd className="text-ink">{user.walletCount}</dd>
        <dt className="text-stone font-medium">Balance total</dt>
        <dd className="text-ink font-semibold">{user.totalBalance.toFixed(2)}</dd>
      </dl>
    </Card>
  );
}
