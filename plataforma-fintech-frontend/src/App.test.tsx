import { render, screen } from '@testing-library/react';
import App from './App';

// App renders RouterProvider + QueryClientProvider.
// The home route (/) renders AppLayout with a nav bar containing the brand wordmark
// and lazy-loads the HomePage component inside a Suspense boundary.
test('renders accessible nav links on home route', async () => {
  render(<App />);
  // The nav should always be present, immediately
  const navLinks = screen.getAllByRole('link');
  expect(navLinks.length).toBeGreaterThan(0);
  // The brand wordmark "fintech" is always visible in the nav header
  expect(screen.getByText(/fintech/i)).toBeInTheDocument();
});
