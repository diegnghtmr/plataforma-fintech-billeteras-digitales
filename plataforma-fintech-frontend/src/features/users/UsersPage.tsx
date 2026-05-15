import { useState } from 'react';
import { Users } from 'lucide-react';
import { useCreateUserMutation, useUpdateUserMutation, useDeleteUserMutation, useUsersListQuery } from './hooks';
import { useSelectionStore } from '../../stores/use-selection-store';
import { UserForm } from './UserForm';
import { UserCard } from './UserCard';
import { Button } from '../../shared/components/Button';
import { Card } from '../../shared/components/Card';
import { Modal } from '../../shared/components/Modal';
import { EmptyState } from '../../shared/components/EmptyState';
import { SkeletonCard } from '../../shared/components/Skeleton';
import { Input } from '../../shared/components/Input';
import { pushToast } from '../../shared/components/Toast';
import type { CreateUserFormData } from './schemas';
import type { UserResponse } from '../../api/users';

export function UsersPage() {
  const [selectedUser, setSelectedUser] = useState<UserResponse | undefined>(undefined);
  const [search, setSearch] = useState('');
  const [showEditForm, setShowEditForm] = useState(false);
  const [editName, setEditName] = useState('');
  const [editEmail, setEditEmail] = useState('');
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const setSelectedUserId = useSelectionStore((s) => s.setSelectedUserId);

  const { data: users = [], isLoading } = useUsersListQuery();
  const mutation = useCreateUserMutation();
  const updateMutation = useUpdateUserMutation();
  const deleteMutation = useDeleteUserMutation();

  const filtered = users.filter((u) => {
    const q = search.toLowerCase();
    return (
      u.id.toLowerCase().includes(q) ||
      u.name.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q)
    );
  });

  function handleSubmit(data: CreateUserFormData) {
    mutation.mutate(data, {
      onSuccess: (result) => {
        setSelectedUser(result);
        pushToast({ variant: 'success', message: `Usuario ${result.id} creado correctamente.` });
      },
      onError: () => {
        pushToast({ variant: 'error', message: 'No se pudo crear el usuario.' });
      },
    });
  }

  function handleUpdate() {
    if (!selectedUser) return;
    const body: { name?: string; email?: string } = {};
    if (editName) body.name = editName;
    if (editEmail) body.email = editEmail;
    updateMutation.mutate({ userId: selectedUser.id, body }, {
      onSuccess: (updated) => {
        setSelectedUser(updated);
        setShowEditForm(false);
        setEditName('');
        setEditEmail('');
        pushToast({ variant: 'success', message: 'Usuario actualizado.' });
      },
      onError: () => {
        pushToast({ variant: 'error', message: 'No se pudo actualizar el usuario.' });
      },
    });
  }

  function handleDelete() {
    if (!selectedUser) return;
    deleteMutation.mutate(selectedUser.id, {
      onSuccess: () => {
        setSelectedUser(undefined);
        setShowDeleteConfirm(false);
        pushToast({ variant: 'success', message: 'Usuario eliminado.' });
      },
      onError: () => {
        pushToast({ variant: 'error', message: 'No se pudo eliminar el usuario.' });
      },
    });
  }

  function handleSelectUser(user: UserResponse) {
    setSelectedUser(user);
    setSelectedUserId(user.id);
    setShowEditForm(false);
    setEditName('');
    setEditEmail('');
  }

  return (
    <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
      {/* Hero band */}
      <div className="mb-12">
        <h1 className="text-display-lg text-ink mb-3">
          Usuarios
        </h1>
        <p className="text-body-md text-charcoal">
          Gestiona usuarios, sus billeteras y nivel de fidelización.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[minmax(0,400px)_minmax(0,1fr)] gap-8 items-start">
        {/* Left: Create form */}
        <aside className="lg:sticky lg:top-24">
          <Card variant="light">
            <h2 className="text-heading-sm text-ink mb-6">
              Crear usuario
            </h2>
            <UserForm onSubmit={handleSubmit} isPending={mutation.isPending} />
            {mutation.isError && mutation.error && (
              <p className="text-accent-danger text-sm mt-4">
                {(mutation.error as { message?: string })?.message ?? 'Error desconocido'}
              </p>
            )}
          </Card>

          {/* Selected user detail + actions */}
          {selectedUser && (
            <div className="mt-6 flex flex-col gap-4">
              <UserCard user={selectedUser} onSelect={(id) => setSelectedUserId(id)} />

              <div className="flex gap-3">
                <Button
                  variant="outline-light"
                  onClick={() => setShowEditForm((v) => !v)}
                  className="h-10 px-5 text-sm"
                >
                  {showEditForm ? 'Cancelar edición' : 'Editar'}
                </Button>
                <Button
                  variant="soft"
                  onClick={() => setShowDeleteConfirm((v) => !v)}
                  className="h-10 px-5 text-sm text-accent-danger hover:opacity-80"
                >
                  Eliminar
                </Button>
              </div>

              {showEditForm && (
                <Card variant="light" className="flex flex-col gap-4">
                  <h3 className="text-ink font-semibold">Editar usuario</h3>
                  <input
                    type="text"
                    placeholder="Nuevo nombre"
                    value={editName}
                    onChange={(e) => setEditName(e.target.value)}
                    className="w-full bg-canvas-light text-ink border border-hairline-light rounded-[12px] px-4 h-14 text-base focus:outline-none focus:border-brand"
                  />
                  <input
                    type="email"
                    placeholder="Nuevo email"
                    value={editEmail}
                    onChange={(e) => setEditEmail(e.target.value)}
                    className="w-full bg-canvas-light text-ink border border-hairline-light rounded-[12px] px-4 h-14 text-base focus:outline-none focus:border-brand"
                  />
                  <Button
                    variant="dark"
                    onClick={handleUpdate}
                    disabled={updateMutation.isPending}
                    className="h-12 self-start"
                  >
                    {updateMutation.isPending ? 'Guardando...' : 'Guardar cambios'}
                  </Button>
                </Card>
              )}
            </div>
          )}
        </aside>

        {/* Right: User list with search */}
        <section className="min-w-0">
          <div className="mb-4">
            <Input
              placeholder="Buscar por ID, nombre o email..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              aria-label="Buscar usuarios"
            />
          </div>

          {isLoading ? (
            <div className="flex flex-col gap-4">
              {Array.from({ length: 4 }).map((_, i) => (
                <SkeletonCard key={i} />
              ))}
            </div>
          ) : filtered.length === 0 ? (
            <EmptyState
              icon={Users}
              title={search ? 'Sin resultados' : 'No hay usuarios'}
              description={
                search
                  ? 'Prueba con otro id o nombre.'
                  : 'Crea el primer usuario con el formulario.'
              }
            />
          ) : (
            <div className="flex flex-col gap-3">
              {filtered.map((user) => (
                <button
                  key={user.id}
                  type="button"
                  onClick={() => handleSelectUser(user)}
                  className={`w-full text-left rounded-[20px] border transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 ${
                    selectedUser?.id === user.id
                      ? 'border-brand bg-surface-card'
                      : 'border-hairline-light bg-surface-card hover:border-brand/50'
                  }`}
                  aria-pressed={selectedUser?.id === user.id}
                >
                  <div className="px-6 py-4 flex items-center justify-between gap-4">
                    <div className="min-w-0">
                      <p className="text-heading-sm text-ink truncate">{user.name}</p>
                      <p className="text-body-sm text-stone truncate">{user.email}</p>
                      <p className="font-mono text-xs text-stone mt-1">{user.id}</p>
                    </div>
                    <div className="flex-shrink-0">
                      <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${
                        user.loyaltyLevel === 'PLATINUM'
                          ? 'bg-purple-100 text-purple-700'
                          : user.loyaltyLevel === 'GOLD'
                            ? 'bg-yellow-100 text-yellow-700'
                            : user.loyaltyLevel === 'SILVER'
                              ? 'bg-gray-100 text-gray-600'
                              : 'bg-amber-50 text-amber-700'
                      }`}>
                        {user.loyaltyLevel === 'PLATINUM' ? 'Platino'
                          : user.loyaltyLevel === 'GOLD' ? 'Oro'
                          : user.loyaltyLevel === 'SILVER' ? 'Plata'
                          : 'Bronce'}
                      </span>
                    </div>
                  </div>
                </button>
              ))}
            </div>
          )}
        </section>
      </div>

      {selectedUser && (
        <Modal
          open={showDeleteConfirm}
          onClose={() => setShowDeleteConfirm(false)}
          title="Eliminar usuario"
          description={`¿Eliminar usuario ${selectedUser.id} y todos sus datos? Esta acción no se puede deshacer.`}
          confirmLabel="Confirmar eliminación"
          tone="danger"
          onConfirm={handleDelete}
          isPending={deleteMutation.isPending}
        />
      )}
    </div>
  );
}
