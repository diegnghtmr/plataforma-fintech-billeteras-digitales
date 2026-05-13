import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SearchSelect } from '../SearchSelect';
import type { SearchSelectOption } from '../SearchSelect';

const OPTIONS: SearchSelectOption[] = [
  { value: 'USR001', label: 'Ana García', description: 'USR001 · ana@example.com' },
  { value: 'USR002', label: 'Carlos López', description: 'USR002 · carlos@example.com' },
  { value: 'USR003', label: 'María Rodríguez', description: 'USR003 · maria@example.com' },
];

describe('SearchSelect', () => {
  it('renders trigger with placeholder when no value is selected', () => {
    render(
      <SearchSelect
        options={OPTIONS}
        value=""
        onChange={() => undefined}
        placeholder="Selecciona un usuario"
      />
    );
    expect(screen.getByRole('combobox')).toBeInTheDocument();
    expect(screen.getByText('Selecciona un usuario')).toBeInTheDocument();
  });

  it('renders trigger with selected option label', () => {
    render(
      <SearchSelect
        options={OPTIONS}
        value="USR002"
        onChange={() => undefined}
        placeholder="Selecciona un usuario"
      />
    );
    expect(screen.getByText('Carlos López')).toBeInTheDocument();
  });

  it('opens dropdown on trigger click', async () => {
    const user = userEvent.setup();
    render(
      <SearchSelect
        options={OPTIONS}
        value=""
        onChange={() => undefined}
        placeholder="Selecciona un usuario"
      />
    );

    await user.click(screen.getByRole('combobox'));
    expect(screen.getByRole('listbox')).toBeInTheDocument();
    expect(screen.getAllByRole('option')).toHaveLength(OPTIONS.length);
  });

  it('filters options as user types', async () => {
    const user = userEvent.setup();
    render(
      <SearchSelect
        options={OPTIONS}
        value=""
        onChange={() => undefined}
        placeholder="Selecciona un usuario"
      />
    );

    await user.click(screen.getByRole('combobox'));
    await user.type(screen.getByRole('combobox'), 'carlos');

    const visibleOptions = screen.getAllByRole('option');
    expect(visibleOptions).toHaveLength(1);
    expect(screen.getByText('Carlos López')).toBeInTheDocument();
  });

  it('shows emptyMessage when filter matches nothing', async () => {
    const user = userEvent.setup();
    render(
      <SearchSelect
        options={OPTIONS}
        value=""
        onChange={() => undefined}
        placeholder="Selecciona un usuario"
        emptyMessage="Sin resultados"
      />
    );

    await user.click(screen.getByRole('combobox'));
    await user.type(screen.getByRole('combobox'), 'zzzzz');

    expect(screen.getByText('Sin resultados')).toBeInTheDocument();
    expect(screen.queryByRole('option')).not.toBeInTheDocument();
  });

  it('calls onChange with option value when option clicked', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(
      <SearchSelect
        options={OPTIONS}
        value=""
        onChange={onChange}
        placeholder="Selecciona un usuario"
      />
    );

    await user.click(screen.getByRole('combobox'));
    fireEvent.mouseDown(screen.getByRole('option', { name: /ana garcía/i }));

    expect(onChange).toHaveBeenCalledWith('USR001');
  });

  it('closes dropdown on Escape key', async () => {
    const user = userEvent.setup();
    render(
      <SearchSelect
        options={OPTIONS}
        value=""
        onChange={() => undefined}
        placeholder="Selecciona un usuario"
      />
    );

    await user.click(screen.getByRole('combobox'));
    expect(screen.getByRole('listbox')).toBeInTheDocument();

    // After opening, the combobox becomes an input — target it directly
    const input = screen.getByRole('combobox');
    fireEvent.keyDown(input, { key: 'Escape' });
    expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
  });

  it('shows loading state in dropdown', async () => {
    const user = userEvent.setup();
    render(
      <SearchSelect
        options={[]}
        value=""
        onChange={() => undefined}
        placeholder="Selecciona un usuario"
        isLoading={true}
      />
    );

    await user.click(screen.getByRole('combobox'));
    expect(screen.getByText('Cargando...')).toBeInTheDocument();
  });

  it('is disabled when disabled prop is true', () => {
    render(
      <SearchSelect
        options={OPTIONS}
        value=""
        onChange={() => undefined}
        placeholder="Selecciona un usuario"
        disabled={true}
      />
    );

    const trigger = screen.getByRole('combobox');
    expect(trigger).toBeDisabled();
  });

  it('navigates options with keyboard and selects on Enter', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(
      <SearchSelect
        options={OPTIONS}
        value=""
        onChange={onChange}
        placeholder="Selecciona un usuario"
      />
    );

    await user.click(screen.getByRole('combobox'));
    expect(screen.getByRole('listbox')).toBeInTheDocument();

    // After opening, use fireEvent for keyboard navigation to ensure events hit the input
    const input = screen.getByRole('combobox');
    fireEvent.keyDown(input, { key: 'ArrowDown' });
    fireEvent.keyDown(input, { key: 'Enter' });

    expect(onChange).toHaveBeenCalledWith(OPTIONS[0]!.value);
  });
});
