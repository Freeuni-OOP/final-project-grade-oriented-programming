import { useNavigate } from 'react-router-dom';

export default function UnauthorizedPage() {
  const navigate = useNavigate();
  return (
    <div style={styles.page}>
      <div style={styles.box}>
        <h1 style={styles.code}>403</h1>
        <p style={styles.message}>You don't have permission to view this page.</p>
        <button style={styles.button} onClick={() => navigate(-1)}>
          Go back
        </button>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: '#f5f5f5',
  },
  box: { textAlign: 'center', padding: 40 },
  code: { fontSize: 72, fontWeight: 700, margin: '0 0 8px', color: '#2563eb' },
  message: { fontSize: 16, color: '#444', marginBottom: 24 },
  button: {
    padding: '10px 24px',
    fontSize: 14,
    fontWeight: 600,
    background: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: 4,
    cursor: 'pointer',
  },
};
