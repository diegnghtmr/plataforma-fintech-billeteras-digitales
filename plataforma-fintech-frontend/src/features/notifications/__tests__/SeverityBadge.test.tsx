import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { SeverityBadge } from '../SeverityBadge';

/**
 * T08-F10 (RED) — SeverityBadge tests.
 */
describe('SeverityBadge', () => {
  it('renders INFO with info color class', () => {
    render(<SeverityBadge severity="INFO" />);
    const badge = screen.getByText('INFO');
    expect(badge.className).toContain('bg-info');
  });

  it('renders WARNING with warn color class', () => {
    render(<SeverityBadge severity="WARNING" />);
    const badge = screen.getByText('WARNING');
    expect(badge.className).toContain('bg-warn');
  });

  it('renders CRITICAL with danger color class', () => {
    render(<SeverityBadge severity="CRITICAL" />);
    const badge = screen.getByText('CRITICAL');
    expect(badge.className).toContain('bg-danger');
  });
});
