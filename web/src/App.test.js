import { render, screen } from '@testing-library/react';
import App from './App';

test('renders apartment living heading', () => {
  render(<App />);
  const headingElement = screen.getByText(/Apartment Living/i);
  expect(headingElement).toBeInTheDocument();
});
