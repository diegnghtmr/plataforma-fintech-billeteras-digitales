import { Card } from '../../shared/components/Card';
import { Button } from '../../shared/components/Button';
import type { UserResponse } from '../../api/users';

interface UserCardProps {
  user: UserResponse;
  onSelect?: (id: string) => void;
}

export function UserCard({ user, onSelect }: UserCardProps) {
  return (
    <Card className="flex flex-col gap-2">
      <div className="flex items-center justify-between">
        <span className="text-surface-fg font-semibold">{user.name}</span>
        {onSelect && (
          <Button variant="ghost" onClick={() => onSelect(user.id)}>
            Seleccionar
          </Button>
        )}
      </div>
      <dl className="grid grid-cols-2 gap-1 text-sm text-surface-fg/80">
        <dt className="font-medium">ID</dt>
        <dd>{user.id}</dd>
        <dt className="font-medium">Email</dt>
        <dd>{user.email}</dd>
        <dt className="font-medium">Registrado</dt>
        <dd>{new Date(user.registeredAt).toLocaleDateString()}</dd>
        <dt className="font-medium">Puntos</dt>
        <dd>{user.points}</dd>
        <dt className="font-medium">Nivel</dt>
        <dd>{user.loyaltyLevel}</dd>
        <dt className="font-medium">Billeteras</dt>
        <dd>{user.walletCount}</dd>
        <dt className="font-medium">Balance total</dt>
        <dd>{user.totalBalance.toFixed(2)}</dd>
      </dl>
    </Card>
  );
}
