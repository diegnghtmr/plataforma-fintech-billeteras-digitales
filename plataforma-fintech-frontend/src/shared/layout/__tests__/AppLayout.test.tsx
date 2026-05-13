import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { AppLayout } from '../AppLayout';

function Wrapper() {
  return (
    <MemoryRouter initialEntries={['/']}>
      <Routes>
        <Route element={<AppLayout />}>
          <Route index element={<div>Home</div>} />
        </Route>
      </Routes>
    </MemoryRouter>
  );
}

describe('AppLayout', () => {
  it('renders nav links', () => {
    render(<Wrapper />);
    expect(screen.getByRole('link', { name: /usuarios/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /billeteras/i })).toBeInTheDocument();
  });

  it('renders Puntos nav link', () => {
    render(<Wrapper />);
    expect(screen.getByRole('link', { name: /puntos/i })).toBeInTheDocument();
  });

  it('renders outlet content', () => {
    render(<Wrapper />);
    expect(screen.getByText('Home')).toBeInTheDocument();
  });

  it('renders all nav links', () => {
    render(<Wrapper />);
    expect(screen.getByRole('link', { name: /inicio/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /usuarios/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /billeteras/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /operaciones/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /transacciones/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /puntos/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /programadas/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /alertas/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /analítica/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /fraude/i })).toBeInTheDocument();
  });
});
