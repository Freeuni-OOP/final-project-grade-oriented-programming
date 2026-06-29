import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <section style={styles.panel}>
      <p style={styles.code}>404</p>
      <h1 style={styles.title}>Page not found</h1>
      <p style={styles.message}>The route you opened does not exist in this app.</p>
      <Link to="/dashboard" style={styles.link}>
        Go to dashboard
      </Link>
    </section>
  );
}

const styles = {
  panel: {
    maxWidth: 520,
    padding: 32,
    border: '1px solid #dfe3eb',
    borderRadius: 8,
    background: '#ffffff',
  },
  code: {
    margin: '0 0 8px',
    color: '#0f766e',
    fontSize: 44,
    fontWeight: 800,
    lineHeight: 1,
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
