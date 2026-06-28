import { isRouteErrorResponse, useRouteError } from 'react-router-dom';

export function ErrorFallback({
  title = 'Something went wrong',
  message = 'The page could not be loaded.',
}) {
  return (
    <div style={styles.page}>
      <section style={styles.panel}>
        <p style={styles.kicker}>Error</p>
        <h1 style={styles.title}>{title}</h1>
        <p style={styles.message}>{message}</p>
        <a href="/dashboard" style={styles.link}>
          Go to dashboard
        </a>
      </section>
    </div>
  );
}

export default function ErrorPage() {
  const error = useRouteError();
  let title = 'Something went wrong';
  let message = 'The page could not be loaded.';

  if (isRouteErrorResponse(error)) {
    title = `${error.status} ${error.statusText}`;
    message = error.data?.message ?? message;
  } else if (error instanceof Error) {
    message = error.message;
  }

  return <ErrorFallback title={title} message={message} />;
}

const styles = {
  page: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: '#f7f8fb',
    padding: 24,
    boxSizing: 'border-box',
  },
  panel: {
    maxWidth: 520,
    padding: 32,
    border: '1px solid #dfe3eb',
    borderRadius: 8,
    background: '#ffffff',
    textAlign: 'left',
  },
  kicker: {
    margin: '0 0 8px',
    color: '#b42318',
    fontSize: 13,
    fontWeight: 700,
    textTransform: 'uppercase',
  },
  title: {
    margin: '0 0 10px',
    color: '#172033',
    fontSize: 28,
    lineHeight: 1.2,
  },
  message: {
    margin: '0 0 20px',
    color: '#566176',
    fontSize: 16,
    lineHeight: 1.5,
  },
  link: {
    display: 'inline-flex',
    padding: '10px 14px',
    borderRadius: 6,
    background: '#0f766e',
    color: '#ffffff',
    fontSize: 14,
    fontWeight: 700,
    textDecoration: 'none',
  },
};
