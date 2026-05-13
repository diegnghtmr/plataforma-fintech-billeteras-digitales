import { render, screen } from '@testing-library/react';
import { Button } from '../Button';
import { Card } from '../Card';
import { Input } from '../Input';
import { Field } from '../Field';

describe('Button', () => {
  it('renders primary button', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByRole('button', { name: /click me/i })).toBeInTheDocument();
  });

  it('renders ghost variant', () => {
    render(<Button variant="ghost">Ghost</Button>);
    expect(screen.getByRole('button', { name: /ghost/i })).toBeInTheDocument();
  });
});

describe('Card', () => {
  it('renders children', () => {
    render(<Card><p>Card content</p></Card>);
    expect(screen.getByText('Card content')).toBeInTheDocument();
  });
});

describe('Input', () => {
  it('renders an input', () => {
    render(<Input placeholder="Type here" />);
    expect(screen.getByPlaceholderText('Type here')).toBeInTheDocument();
  });
});

describe('Field', () => {
  it('renders label and children', () => {
    render(<Field label="Email"><Input placeholder="email" /></Field>);
    expect(screen.getByText('Email')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('email')).toBeInTheDocument();
  });

  it('renders error when provided', () => {
    render(<Field label="Name" error="Required"><Input /></Field>);
    expect(screen.getByText('Required')).toBeInTheDocument();
  });
});
