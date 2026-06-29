export default function PlaceholderPage({ title, description }) {
  return (
    <section style={styles.panel}>
      <p style={styles.kicker}>Placeholder page</p>
      <h1 style={styles.title}>{title}</h1>
      <p style={styles.description}>{description}</p>
    </section>
  );
}

const styles = {
  panel: {
    maxWidth: 720,
    padding: 28,
    border: '1px solid #dfe3eb',
    borderRadius: 8,
    background: '#ffffff',
  },
  kicker: {
    margin: '0 0 8px',
    color: '#0f766e',
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
  description: {
    margin: 0,
    color: '#566176',
    fontSize: 16,
    lineHeight: 1.5,
  },
};
