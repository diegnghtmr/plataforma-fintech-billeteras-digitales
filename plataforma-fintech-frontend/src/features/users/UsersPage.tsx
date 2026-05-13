import { useState } from 'react';
import { useCreateUserMutation, useUserQuery, useUpdateUserMutation, useDeleteUserMutation } from './hooks';
import { useSelectionStore } from '../../stores/use-selection-store';
import { UserForm } from './UserForm';
import { UserCard } from './UserCard';
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
      },
    });
  }

  function handleUpdate() {
    if (!createdUserId) return;
    const body: { name?: string; email?: string } = {};
    if (editName) body.name = editName;
    if (editEmail) body.email = editEmail;
    updateMutation.mutate({ userId: createdUserId, body }, {
      onSuccess: () => setShowEditForm(false),
    });
  }

  function handleDelete() {
    if (!createdUserId) return;
    deleteMutation.mutate(createdUserId, {
      onSuccess: () => {
        setCreatedUserId(undefined);
        setShowDeleteConfirm(false);
      },
    });
  }

  return (
    <div className="flex flex-col gap-6 max-w-lg">
      <h2 className="text-canvas-fg text-xl font-bold">Usuarios</h2>

      <UserForm onSubmit={handleSubmit} isPending={mutation.isPending} />

      {mutation.isError && mutation.error && (
        <p className="text-danger text-sm">
          {(mutation.error as { message?: string })?.message ?? 'Error desconocido'}
        </p>
      )}

      {user && (
        <>
          <UserCard user={user} onSelect={(id) => setSelectedUserId(id)} />

          <div className="flex gap-2">
            <button
              onClick={() => setShowEditForm((v) => !v)}
              className="px-3 py-1 rounded text-sm bg-surface text-surface-fg border border-surface-fg/20 hover:opacity-90"
            >
              {showEditForm ? 'Cancelar edición' : 'Editar'}
            </button>
            <button
              onClick={() => setShowDeleteConfirm((v) => !v)}
              className="px-3 py-1 rounded text-sm bg-danger text-danger-fg hover:opacity-90"
            >
              Eliminar
            </button>
          </div>

          {showEditForm && (
            <div className="bg-surface rounded-lg p-4 flex flex-col gap-3">
              <h3 className="text-surface-fg font-semibold text-sm">Editar usuario</h3>
              <input
                type="text"
                placeholder="Nuevo nombre"
                value={editName}
                onChange={(e) => setEditName(e.target.value)}
                className="bg-canvas text-canvas-fg border border-surface-fg/20 rounded px-3 py-2 text-sm"
              />
              <input
                type="email"
                placeholder="Nuevo email"
                value={editEmail}
                onChange={(e) => setEditEmail(e.target.value)}
                className="bg-canvas text-canvas-fg border border-surface-fg/20 rounded px-3 py-2 text-sm"
              />
              <button
                onClick={handleUpdate}
                disabled={updateMutation.isPending}
                className="bg-accent text-accent-fg rounded px-4 py-2 text-sm font-medium hover:opacity-90 disabled:opacity-50"
              >
                {updateMutation.isPending ? 'Guardando...' : 'Guardar cambios'}
              </button>
            </div>
          )}

          {showDeleteConfirm && (
            <div className="bg-surface rounded-lg p-4 flex flex-col gap-3 border border-danger">
              <p className="text-surface-fg text-sm">
                ¿Eliminar usuario <strong>{user.id}</strong> y todos sus datos?
              </p>
              <div className="flex gap-2">
                <button
                  onClick={handleDelete}
                  disabled={deleteMutation.isPending}
                  className="px-3 py-1 rounded text-sm bg-danger text-danger-fg hover:opacity-90 disabled:opacity-50"
                >
                  {deleteMutation.isPending ? 'Eliminando...' : 'Confirmar eliminación'}
                </button>
                <button
                  onClick={() => setShowDeleteConfirm(false)}
                  className="px-3 py-1 rounded text-sm bg-surface text-surface-fg border border-surface-fg/20"
                >
                  Cancelar
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
