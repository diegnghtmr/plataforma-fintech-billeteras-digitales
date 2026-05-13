import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { LoyaltyBadge } from '../LoyaltyBadge';

describe('LoyaltyBadge', () => {
  it('renders BRONZE badge with correct text', () => {
    render(<LoyaltyBadge level="BRONZE" />);
    expect(screen.getByText('BRONZE')).toBeInTheDocument();
  });

  it('renders SILVER badge with correct text', () => {
    render(<LoyaltyBadge level="SILVER" />);
    expect(screen.getByText('SILVER')).toBeInTheDocument();
  });

  it('renders GOLD badge with correct text', () => {
    render(<LoyaltyBadge level="GOLD" />);
    expect(screen.getByText('GOLD')).toBeInTheDocument();
  });

  it('renders PLATINUM badge with correct text', () => {
    render(<LoyaltyBadge level="PLATINUM" />);
    expect(screen.getByText('PLATINUM')).toBeInTheDocument();
  });

  it('has accessible aria-label', () => {
    render(<LoyaltyBadge level="GOLD" />);
    const badge = screen.getByRole('status');
    expect(badge).toHaveAttribute('aria-label', 'Loyalty level: GOLD');
  });
});
