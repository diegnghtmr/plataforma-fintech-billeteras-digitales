import { render, screen } from '@testing-library/react';
import App from './App';

// App now renders RouterProvider + QueryClientProvider.
// The home route (/) renders AppLayout with the brand heading "Fintech Wallet"
// in the sidebar nav, so an accessible heading is always present.
test('renders accessible heading on home route', async () => {
  render(<App />);
  // There may be multiple "Fintech Wallet" headings (sidebar brand + page h1).
  const headings = screen.getAllByRole('heading');
  expect(headings.length).toBeGreaterThan(0);
  // At least one heading contains the brand text
  const brandHeadings = screen.getAllByText(/fintech wallet/i);
  expect(brandHeadings.length).toBeGreaterThan(0);
});
