import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import {
  Home,
  Users,
  Wallet,
  Send,
  ArrowLeftRight,
  Trophy,
  CalendarClock,
  Bell,
  BarChart3,
  ShieldAlert,
  Menu,
  X,
} from 'lucide-react';
import { ToastViewport } from '../components/Toast';

const NAV_ITEMS = [
  { to: '/', label: 'Inicio', Icon: Home },
  { to: '/users', label: 'Usuarios', Icon: Users },
  { to: '/wallets', label: 'Billeteras', Icon: Wallet },
  { to: '/operations', label: 'Operaciones', Icon: Send },
  { to: '/transactions', label: 'Transacciones', Icon: ArrowLeftRight },
  { to: '/points', label: 'Puntos', Icon: Trophy },
  { to: '/scheduled', label: 'Programadas', Icon: CalendarClock },
  { to: '/notifications', label: 'Alertas', Icon: Bell },
  { to: '/analytics', label: 'Analítica', Icon: BarChart3 },
  { to: '/fraud', label: 'Fraude', Icon: ShieldAlert },
] as const;

function NavItem({ to, label, Icon }: { to: string; label: string; Icon: typeof Home }) {
  return (
    <NavLink
      to={to}
      end={to === '/'}
      className={({ isActive }) =>
        `inline-flex items-center gap-2 text-button-sm tracking-wide transition-opacity px-1 py-0.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-dark rounded-sm ${
          isActive ? 'text-on-dark' : 'text-on-dark-mute hover:text-on-dark'
        }`
      }
    >
      <Icon size={18} strokeWidth={1.5} />
      <span>{label}</span>
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
        <span className="font-display font-medium text-on-dark text-xl tracking-tight mr-8 shrink-0">
          fintech
        </span>

        {/* Desktop nav links */}
        <nav className="hidden lg:flex items-center gap-6 flex-1 overflow-x-auto [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden">
          {NAV_ITEMS.map((item) => (
            <NavItem key={item.to} to={item.to} label={item.label} Icon={item.Icon} />
          ))}
        </nav>

        {/* CTA — right side */}
        <div className="hidden lg:flex ml-auto shrink-0">
          <NavLink
            to="/users"
            className="inline-flex items-center justify-center rounded-full bg-canvas-light text-canvas-dark text-button-sm px-5 py-2 h-10 hover:bg-faint transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-dark"
          >
            Ir al dashboard
          </NavLink>
        </div>

        {/* Hamburger — mobile */}
        <button
          className="lg:hidden ml-auto text-on-dark p-2 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand rounded-sm"
          aria-label={mobileOpen ? 'Cerrar menú' : 'Abrir menú'}
          onClick={() => setMobileOpen((v) => !v)}
        >
          {mobileOpen ? <X size={24} /> : <Menu size={24} />}
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
                  `inline-flex items-center gap-3 text-base font-semibold tracking-wide py-3 border-b border-hairline-dark transition-opacity focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand rounded-sm ${
                    isActive ? 'text-on-dark' : 'text-on-dark-mute'
                  }`
                }
              >
                <item.Icon size={20} strokeWidth={1.5} />
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

      {/* Toast viewport — renders floating notifications */}
      <ToastViewport />
    </div>
  );
}
