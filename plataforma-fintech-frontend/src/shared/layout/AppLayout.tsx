import { NavLink, Outlet } from 'react-router-dom';

export function AppLayout() {
  return (
    <div className="flex min-h-screen bg-canvas text-canvas-fg">
      <nav className="w-56 bg-surface border-r border-surface-fg/10 flex flex-col p-4 gap-2">
        <h1 className="text-xl font-bold text-accent mb-4">Fintech Wallet</h1>
        <NavLink
          to="/"
          className={({ isActive }) =>
            `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-fg' : 'text-surface-fg hover:bg-surface-fg/10'}`
          }
        >
          Inicio
        </NavLink>
        <NavLink
          to="/users"
          className={({ isActive }) =>
            `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-fg' : 'text-surface-fg hover:bg-surface-fg/10'}`
          }
        >
          Usuarios
        </NavLink>
        <NavLink
          to="/wallets"
          className={({ isActive }) =>
            `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-fg' : 'text-surface-fg hover:bg-surface-fg/10'}`
          }
        >
          Billeteras
        </NavLink>
        <NavLink
          to="/operations"
          className={({ isActive }) =>
            `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-fg' : 'text-surface-fg hover:bg-surface-fg/10'}`
          }
        >
          Operaciones
        </NavLink>
        <NavLink
          to="/transactions"
          className={({ isActive }) =>
            `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-fg' : 'text-surface-fg hover:bg-surface-fg/10'}`
          }
        >
          Transacciones
        </NavLink>
        <NavLink
          to="/points"
          className={({ isActive }) =>
            `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-fg' : 'text-surface-fg hover:bg-surface-fg/10'}`
          }
        >
          Puntos
        </NavLink>
        <NavLink
          to="/scheduled"
          className={({ isActive }) =>
            `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-fg' : 'text-surface-fg hover:bg-surface-fg/10'}`
          }
        >
          Programadas
        </NavLink>
        <NavLink
          to="/notifications"
          className={({ isActive }) =>
            `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-fg' : 'text-surface-fg hover:bg-surface-fg/10'}`
          }
        >
          Alertas
        </NavLink>
        <NavLink
          to="/analytics"
          className={({ isActive }) =>
            `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-fg' : 'text-surface-fg hover:bg-surface-fg/10'}`
          }
        >
          Analítica
        </NavLink>
        <NavLink
          to="/fraud"
          className={({ isActive }) =>
            `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-fg' : 'text-surface-fg hover:bg-surface-fg/10'}`
          }
        >
          Fraude
        </NavLink>
      </nav>
      <main className="flex-1 p-6">
        <Outlet />
      </main>
    </div>
  );
}
