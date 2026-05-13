import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';

const NAV_ITEMS = [
  { to: '/', label: 'Inicio' },
  { to: '/users', label: 'Usuarios' },
  { to: '/wallets', label: 'Billeteras' },
  { to: '/operations', label: 'Operaciones' },
  { to: '/transactions', label: 'Transacciones' },
  { to: '/points', label: 'Puntos' },
  { to: '/scheduled', label: 'Programadas' },
  { to: '/notifications', label: 'Alertas' },
  { to: '/analytics', label: 'Analítica' },
  { to: '/fraud', label: 'Fraude' },
] as const;

function NavItem({ to, label }: { to: string; label: string }) {
  return (
    <NavLink
      to={to}
      end={to === '/'}
      className={({ isActive }) =>
        `text-sm font-semibold tracking-wide transition-opacity px-1 py-0.5 ${
          isActive ? 'text-on-dark' : 'text-on-dark-mute hover:text-on-dark'
        }`
      }
    >
      {label}
    </NavLink>
  );
}

export function AppLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <div className="min-h-screen bg-canvas-light text-ink">
      {/* Top nav — canvas-dark, h-16 */}
      <header className="fixed top-0 inset-x-0 z-50 bg-canvas-dark h-16 flex items-center px-6 lg:px-12">
        {/* Wordmark */}
        <span
          className="font-display font-medium text-on-dark text-xl tracking-tight mr-8 shrink-0"
          style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
        >
          fintech
        </span>

        {/* Desktop nav links — centred */}
        <nav className="hidden lg:flex items-center gap-6 flex-1">
          {NAV_ITEMS.map((item) => (
            <NavItem key={item.to} to={item.to} label={item.label} />
          ))}
        </nav>

        {/* CTA — right side */}
        <div className="hidden lg:flex ml-auto">
          <NavLink
            to="/users"
            className="inline-flex items-center justify-center rounded-full bg-canvas-light text-canvas-dark text-sm font-semibold tracking-wide px-5 py-2 h-10 hover:bg-faint transition-colors"
          >
            Ir al dashboard
          </NavLink>
        </div>

        {/* Hamburger — mobile */}
        <button
          className="lg:hidden ml-auto text-on-dark p-2"
          aria-label="Abrir menú"
          onClick={() => setMobileOpen((v) => !v)}
        >
          <svg width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            {mobileOpen ? (
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            ) : (
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
            )}
          </svg>
        </button>
      </header>

      {/* Mobile drawer */}
      {mobileOpen && (
        <div
          className="lg:hidden fixed inset-0 z-40 bg-canvas-dark pt-16"
          onClick={() => setMobileOpen(false)}
        >
          <nav
            className="flex flex-col gap-1 px-6 py-6"
            onClick={(e) => e.stopPropagation()}
          >
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                onClick={() => setMobileOpen(false)}
                className={({ isActive }) =>
                  `text-base font-semibold tracking-wide py-3 border-b border-hairline-dark transition-opacity ${
                    isActive ? 'text-on-dark' : 'text-on-dark-mute'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      )}

      {/* Main content — offset for fixed header */}
      <main className="pt-16 bg-canvas-light">
        <Outlet />
      </main>
    </div>
  );
}
