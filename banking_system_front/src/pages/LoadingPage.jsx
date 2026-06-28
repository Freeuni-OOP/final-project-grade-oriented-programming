export default function LoadingPage({ message = 'Loading page...' }) {
  return (
    <div style={styles.page} role="status" aria-live="polite">
      <div style={styles.box}>
        <div style={styles.spinner} />
        <p style={styles.message}>{message}</p>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: 240,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    display: 'flex',
    alignItems: 'center',
    gap: 12,
    color: '#566176',
    fontSize: 15,
    fontWeight: 600,
  },
  spinner: {
    width: 18,
    height: 18,
    border: '3px solid #dfe3eb',
    borderTopColor: '#0f766e',
    borderRadius: '50%',
  },
  message: {
    margin: 0,
  },
};
