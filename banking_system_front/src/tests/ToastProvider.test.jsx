import { act, fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { notifyBackendError } from '../api/errorNotifications';
import { ToastProvider, useToast } from '../components/ToastProvider';

function TestButton() {
  const { showToast, showErrorToast } = useToast();

  return (
    <>
      <button onClick={() => showToast({ message: 'Saved card.', timeout: 0 })}>show normal</button>
      <button onClick={() => showErrorToast('Invalid input.', { timeout: 0 })}>show error</button>
    </>
  );
}

describe('ToastProvider', () => {
  it('shows normal toasts', () => {
    render(
      <ToastProvider>
        <TestButton />
      </ToastProvider>
    );

    fireEvent.click(screen.getByRole('button', { name: 'show normal' }));

    expect(screen.getByText('Saved card.')).toBeInTheDocument();
  });

  it('shows error toasts with the shared title', () => {
    render(
      <ToastProvider>
        <TestButton />
      </ToastProvider>
    );

    fireEvent.click(screen.getByRole('button', { name: 'show error' }));

    expect(screen.getByText('Invalid input.')).toBeInTheDocument();
  });

  it('dismisses toasts after the timeout', () => {
    vi.useFakeTimers();

    function TimedButton() {
      const { showToast } = useToast();
      return (
        <button onClick={() => showToast({ message: 'Temporary.', timeout: 1000 })}>show</button>
      );
    }

    render(
      <ToastProvider>
        <TimedButton />
      </ToastProvider>
    );

    fireEvent.click(screen.getByRole('button', { name: 'show' }));
    expect(screen.getByText('Temporary.')).toBeInTheDocument();

    act(() => {
      vi.advanceTimersByTime(1000);
    });
    expect(screen.queryByText('Temporary.')).not.toBeInTheDocument();

    vi.useRealTimers();
  });

  it('shows backend errors sent from the api layer', () => {
    render(
      <ToastProvider>
        <span>App content</span>
      </ToastProvider>
    );

    act(() => {
      notifyBackendError({ response: { status: 406 } });
    });

    expect(
      screen.getByText('This action is not allowed in the current state.')
    ).toBeInTheDocument();
  });
});
