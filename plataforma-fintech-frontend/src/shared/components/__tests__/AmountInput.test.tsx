import { render, screen, fireEvent } from '@testing-library/react';
import { AmountInput } from '../AmountInput';

describe('AmountInput', () => {
  it('renders a dollar sign prefix icon', () => {
    render(<AmountInput placeholder="0.00" />);
    // The icon is rendered inside the wrapper; the input itself is accessible
    const input = screen.getByPlaceholderText('0.00');
    expect(input).toBeInTheDocument();
    // Icon wrapper present
    expect(input.parentElement?.querySelector('span')).toBeInTheDocument();
  });

  it('renders as type="text" with inputMode="decimal"', () => {
    render(<AmountInput placeholder="0.00" />);
    const input = screen.getByPlaceholderText('0.00');
    expect(input).toHaveAttribute('type', 'text');
    expect(input).toHaveAttribute('inputMode', 'decimal');
  });

  it.each(['a', 'f', 'F', 'z', '$', '@', ' '])(
    'blocks the non-numeric "%s" key',
    (key) => {
      render(<AmountInput placeholder="0.00" />);
      const input = screen.getByPlaceholderText('0.00');
      const event = new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true });
      input.dispatchEvent(event);
      expect(event.defaultPrevented).toBe(true);
    }
  );

  it.each(['e', 'E', '+', '-', ','])(
    'blocks the "%s" key',
    (key) => {
      render(<AmountInput placeholder="0.00" />);
      const input = screen.getByPlaceholderText('0.00');
      const event = new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true });
      input.dispatchEvent(event);
      expect(event.defaultPrevented).toBe(true);
    }
  );

  it.each(['1', '0', '9', '.'])(
    'allows the "%s" key',
    (key) => {
      render(<AmountInput placeholder="0.00" />);
      const input = screen.getByPlaceholderText('0.00');
      const event = new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true });
      input.dispatchEvent(event);
      expect(event.defaultPrevented).toBe(false);
    }
  );

  it('blocks paste of non-numeric text', () => {
    render(<AmountInput placeholder="0.00" />);
    const input = screen.getByPlaceholderText('0.00');

    const clipboardData = {
      getData: () => 'abc',
    };

    const event = new Event('paste', { bubbles: true, cancelable: true });
    Object.defineProperty(event, 'clipboardData', { value: clipboardData });

    input.dispatchEvent(event);
    expect(event.defaultPrevented).toBe(true);
  });

  it('allows paste of valid numeric text', () => {
    render(<AmountInput placeholder="0.00" />);
    const input = screen.getByPlaceholderText('0.00');

    const clipboardData = {
      getData: () => '123.45',
    };

    const event = new Event('paste', { bubbles: true, cancelable: true });
    Object.defineProperty(event, 'clipboardData', { value: clipboardData });

    input.dispatchEvent(event);
    expect(event.defaultPrevented).toBe(false);
  });

  it('calls external onKeyDown after applying the guard', () => {
    const onKeyDown = vi.fn();
    render(<AmountInput placeholder="0.00" onKeyDown={onKeyDown} />);
    const input = screen.getByPlaceholderText('0.00');

    // A safe key — handler should be called
    fireEvent.keyDown(input, { key: '5' });
    expect(onKeyDown).toHaveBeenCalledTimes(1);
  });
});
