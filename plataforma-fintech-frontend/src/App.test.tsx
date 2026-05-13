import { render, screen } from '@testing-library/react';
import App from './App';

// App renders RouterProvider + QueryClientProvider.
// The home route (/) renders AppLayout with a nav bar containing the brand wordmark
// and lazy-loads the HomePage component inside a Suspense boundary.
test('renders accessible nav links on home route', async () => {
  render(<App />);
  // With React Router lazy route option, the layout (and its nav) renders once
  // the initial route module resolves — use findAll* to wait.
  const navLinks = await screen.findAllByRole('link');
  expect(navLinks.length).toBeGreaterThan(0);
  // Exact match — HomePage hero also contains "Fintech", we only want the nav wordmark
  expect(screen.getByText('fintech')).toBeInTheDocument();
});
