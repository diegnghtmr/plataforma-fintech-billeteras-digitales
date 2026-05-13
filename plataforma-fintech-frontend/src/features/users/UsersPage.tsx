import { useState } from 'react';
import { useCreateUserMutation, useUserQuery, useUpdateUserMutation, useDeleteUserMutation } from './hooks';
import { useSelectionStore } from '../../stores/use-selection-store';
import { UserForm } from './UserForm';
import { UserCard } from './UserCard';
import { Button } from '../../shared/components/Button';
import { Card } from '../../shared/components/Card';
import { Modal } from '../../shared/components/Modal';
import { pushToast } from '../../shared/components/Toast';
import type { CreateUserFormData } from './schemas';

export function UsersPage() {
  const [createdUserId, setCreatedUserId] = useState<string | undefined>(undefined);
  const [editName, setEditName] = useState('');
  const [editEmail, setEditEmail] = useState('');
  const [showEditForm, setShowEditForm] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const setSelectedUserId = useSelectionStore((s) => s.setSelectedUserId);

  const mutation = useCreateUserMutation();
  const updateMutation = useUpdateUserMutation();
  const deleteMutation = useDeleteUserMutation();
  const { data: user } = useUserQuery(createdUserId);

  function handleSubmit(data: CreateUserFormData) {
    mutation.mutate(data, {
      onSuccess: (result) => {
        setCreatedUserId(result.id);
        pushToast({ variant: 'success', message: 'Usuario creado correctamente.' });
      },
      onError: () => {
        pushToast({ variant: 'error', message: 'No se pudo crear el usuario.' });
      },
    });
  }

  function handleUpdate() {
    if (!createdUserId) return;
    const body: { name?: string; email?: string } = {};
    if (editName) body.name = editName;
    if (editEmail) body.email = editEmail;
    updateMutation.mutate({ userId: createdUserId, body }, {
      onSuccess: () => {
        setShowEditForm(false);
        pushToast({ variant: 'success', message: 'Usuario actualizado.' });
      },
      onError: () => {
        pushToast({ variant: 'error', message: 'No se pudo actualizar el usuario.' });
      },
    });
  }

  function handleDelete() {
    if (!createdUserId) return;
    deleteMutation.mutate(createdUserId, {
      onSuccess: () => {
        setCreatedUserId(undefined);
        setShowDeleteConfirm(false);
        pushToast({ variant: 'success', message: 'Usuario eliminado.' });
      },
      onError: () => {
        pushToast({ variant: 'error', message: 'No se pudo eliminar el usuario.' });
      },
    });
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

      <div className="flex flex-col gap-8 max-w-2xl">
        {/* Create user form */}
        <Card variant="light">
          <h2 className="text-heading-sm text-ink mb-6">
            Crear usuario
          </h2>
          <UserForm onSubmit={handleSubmit} isPending={mutation.isPending} />
        </Card>

        {mutation.isError && mutation.error && (
          <p className="text-accent-danger text-sm">
            {(mutation.error as { message?: string })?.message ?? 'Error desconocido'}
          </p>
        )}

        {user && (
          <>
            <UserCard user={user} onSelect={(id) => setSelectedUserId(id)} />

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

            <Modal
              open={showDeleteConfirm}
              onClose={() => setShowDeleteConfirm(false)}
              title="Eliminar usuario"
              description={`¿Eliminar usuario ${user.id} y todos sus datos? Esta acción no se puede deshacer.`}
              confirmLabel="Confirmar eliminación"
              tone="danger"
              onConfirm={handleDelete}
              isPending={deleteMutation.isPending}
            />
          </>
        )}
      </div>
    </div>
  );
}
