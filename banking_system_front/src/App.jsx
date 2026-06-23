import { useState } from 'react';

const DEFAULT_BASE_URL = 'http://localhost:8080';

export default function RegisterCorsTest() {
  const [baseUrl, setBaseUrl] = useState(DEFAULT_BASE_URL);
  const [form, setForm] = useState({
    firstName: 'John',
    lastName: 'Doe',
    phoneNumber: '555123434',
    address: '123 Main St',
    dateOfBirth: '1990-01-15',
    email: 'johnDoe@bank.com',
    password: 'abdaubda1234',
  });
  const [status, setStatus] = useState(null);
  const [response, setResponse] = useState(null);
  const [corsInfo, setCorsInfo] = useState(null);
  const [errorKind, setErrorKind] = useState(null); // 'network' | 'http'

  const handleChange = (field) => (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));

  const sendRequest = async () => {
    setStatus('loading');
    setResponse(null);
    setCorsInfo(null);
    setErrorKind(null);

    const url = `${baseUrl.replace(/\/$/, '')}/api/auth/register`;

    try {
      const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });

      const corsHeaders = {
        'Access-Control-Allow-Origin': res.headers.get('Access-Control-Allow-Origin'),
        'Access-Control-Allow-Methods': res.headers.get('Access-Control-Allow-Methods'),
        'Access-Control-Allow-Headers': res.headers.get('Access-Control-Allow-Headers'),
      };
      setCorsInfo(corsHeaders);

      let body;
      const ct = res.headers.get('content-type') || '';
      if (ct.includes('application/json')) {
        body = await res.json();
      } else {
        body = await res.text();
      }

      setResponse({ ok: res.ok, status: res.status, statusText: res.statusText, body });
      setStatus(res.ok ? 'success' : 'error');
      setErrorKind(res.ok ? null : 'http');
    } catch (err) {
      setStatus('error');
      setErrorKind('network');
      setResponse({
        error: err.message,
        hint: 'Request never reached the server. This is almost certainly a CORS preflight block or the server is not running. Check the browser DevTools → Console and Network tabs for details.',
      });
    }
  };

  const previewForm = { ...form, password: '••••••••' };

  const fields = [
    { key: 'firstName', label: 'First Name' },
    { key: 'lastName', label: 'Last Name' },
    { key: 'phoneNumber', label: 'Phone Number' },
    { key: 'address', label: 'Address' },
    { key: 'dateOfBirth', label: 'Date of Birth' },
    { key: 'email', label: 'Email' },
    { key: 'password', label: 'Password', type: 'password' },
  ];

  const statusColor = {
    success: { bg: '#16a34a22', text: '#16a34a', border: '#16a34a55' },
    http: { bg: '#f59e0b22', text: '#f59e0b', border: '#f59e0b55' },
    network: { bg: '#dc262622', text: '#dc2626', border: '#dc262655' },
  };

  const activeColor =
    status === 'success'
      ? statusColor.success
      : errorKind === 'http'
        ? statusColor.http
        : statusColor.network;

  const statusLabel =
    status === 'success'
      ? `✓ HTTP ${response?.status} ${response?.statusText}`
      : errorKind === 'http'
        ? `⚠ HTTP ${response?.status} ${response?.statusText}`
        : `✗ ${response?.error ?? 'Unknown error'}`;

  return (
    <div style={styles.page}>
      <header style={styles.header}>
        <div style={styles.badge}>CORS TEST</div>
        <h1 style={styles.title}>POST /api/auth/register</h1>
        <p style={styles.subtitle}>Fire a real request from the browser and inspect CORS headers</p>
      </header>

      <div style={styles.card}>
        <label style={styles.label}>Base URL</label>
        <input
          style={styles.input}
          value={baseUrl}
          onChange={(e) => setBaseUrl(e.target.value)}
          placeholder="http://localhost:8080"
        />
      </div>

      <div style={styles.card}>
        <p style={styles.sectionTitle}>Request Body</p>
        <div style={styles.grid}>
          {fields.map(({ key, label, type }) => (
            <div key={key} style={styles.fieldGroup}>
              <label style={styles.label}>{label}</label>
              <input
                style={styles.input}
                type={type || 'text'}
                value={form[key]}
                onChange={handleChange(key)}
              />
            </div>
          ))}
        </div>

        <div style={styles.preview}>
          <span style={styles.previewLabel}>JSON Preview (password hidden)</span>
          <pre style={styles.pre}>{JSON.stringify(previewForm, null, 2)}</pre>
        </div>

        <button
          style={{ ...styles.button, opacity: status === 'loading' ? 0.6 : 1 }}
          onClick={sendRequest}
          disabled={status === 'loading'}
        >
          {status === 'loading' ? (
            <span style={styles.loadingRow}>
              <span style={styles.spinner} />
              Sending…
            </span>
          ) : (
            'Send Request'
          )}
        </button>
      </div>

      {status && status !== 'loading' && (
        <div style={styles.card}>
          <p style={styles.sectionTitle}>Response</p>

          <div
            style={{
              ...styles.statusBadge,
              background: activeColor.bg,
              color: activeColor.text,
              borderColor: activeColor.border,
            }}
          >
            {statusLabel}
          </div>

          {response?.hint && <p style={styles.hint}>{response.hint}</p>}

          {response?.body !== undefined && (
            <div style={styles.preview}>
              <span style={styles.previewLabel}>Body</span>
              <pre style={styles.pre}>
                {typeof response.body === 'string'
                  ? response.body
                  : JSON.stringify(response.body, null, 2)}
              </pre>
            </div>
          )}

          {corsInfo && (
            <div style={styles.preview}>
              <span style={styles.previewLabel}>CORS Headers</span>
              <p style={styles.corsNote}>
                ℹ Browsers block JS from reading these headers when a preflight fails — all nulls
                below may indicate a CORS block, not absent headers. Cross-check in DevTools →
                Network.
              </p>
              {Object.entries(corsInfo).map(([k, v]) => (
                <div key={k} style={styles.corsRow}>
                  <span style={styles.corsKey}>{k}</span>
                  <span style={v ? styles.corsVal : styles.corsValMissing}>
                    {v ?? '— not present'}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

const spin = `
@keyframes spin { to { transform: rotate(360deg); } }
`;
if (typeof document !== 'undefined') {
  const s = document.createElement('style');
  s.textContent = spin;
  document.head.appendChild(s);
}

const styles = {
  page: {
    minHeight: '100vh',
    background: '#0f1117',
    color: '#e2e8f0',
    fontFamily: "'JetBrains Mono', 'Fira Code', 'Courier New', monospace",
    padding: '32px 16px',
    boxSizing: 'border-box',
    maxWidth: 760,
    margin: '0 auto',
  },
  header: { marginBottom: 28 },
  badge: {
    display: 'inline-block',
    background: '#3b82f622',
    color: '#60a5fa',
    border: '1px solid #3b82f644',
    borderRadius: 4,
    fontSize: 10,
    fontWeight: 700,
    letterSpacing: 2,
    padding: '3px 10px',
    marginBottom: 12,
  },
  title: {
    margin: '0 0 6px',
    fontSize: 22,
    fontWeight: 700,
    color: '#f8fafc',
    letterSpacing: -0.5,
  },
  subtitle: { margin: 0, fontSize: 13, color: '#64748b' },
  card: {
    background: '#1e2130',
    border: '1px solid #2d3348',
    borderRadius: 10,
    padding: 20,
    marginBottom: 16,
  },
  sectionTitle: {
    margin: '0 0 14px',
    fontSize: 11,
    fontWeight: 700,
    letterSpacing: 1.5,
    textTransform: 'uppercase',
    color: '#94a3b8',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '10px 16px',
    marginBottom: 16,
  },
  fieldGroup: { display: 'flex', flexDirection: 'column', gap: 4 },
  label: {
    fontSize: 11,
    color: '#64748b',
    fontWeight: 600,
    letterSpacing: 0.5,
    marginBottom: 2,
  },
  input: {
    background: '#0f1117',
    border: '1px solid #2d3348',
    borderRadius: 6,
    color: '#e2e8f0',
    fontSize: 13,
    padding: '7px 10px',
    fontFamily: 'inherit',
    outline: 'none',
    width: '100%',
    boxSizing: 'border-box',
  },
  preview: {
    background: '#0f1117',
    border: '1px solid #2d3348',
    borderRadius: 6,
    padding: '12px 14px',
    marginBottom: 14,
    position: 'relative',
  },
  previewLabel: {
    display: 'block',
    fontSize: 10,
    color: '#475569',
    letterSpacing: 1,
    textTransform: 'uppercase',
    marginBottom: 8,
    fontWeight: 700,
  },
  pre: {
    margin: 0,
    fontSize: 12,
    color: '#7dd3fc',
    overflowX: 'auto',
    lineHeight: 1.6,
  },
  button: {
    background: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: 7,
    padding: '10px 24px',
    fontSize: 14,
    fontWeight: 700,
    fontFamily: 'inherit',
    cursor: 'pointer',
    letterSpacing: 0.3,
    width: '100%',
  },
  loadingRow: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  spinner: {
    display: 'inline-block',
    width: 14,
    height: 14,
    border: '2px solid #ffffff55',
    borderTop: '2px solid #ffffff',
    borderRadius: '50%',
    animation: 'spin 0.7s linear infinite',
  },
  statusBadge: {
    display: 'inline-block',
    border: '1px solid',
    borderRadius: 6,
    padding: '6px 14px',
    fontSize: 13,
    fontWeight: 700,
    marginBottom: 14,
  },
  hint: {
    fontSize: 12,
    color: '#f59e0b',
    background: '#f59e0b11',
    border: '1px solid #f59e0b33',
    borderRadius: 6,
    padding: '8px 12px',
    marginBottom: 14,
  },
  corsNote: {
    fontSize: 11,
    color: '#94a3b8',
    background: '#3b82f611',
    border: '1px solid #3b82f633',
    borderRadius: 5,
    padding: '6px 10px',
    marginBottom: 10,
    lineHeight: 1.5,
  },
  corsRow: {
    display: 'flex',
    gap: 12,
    padding: '4px 0',
    borderBottom: '1px solid #1e2130',
    flexWrap: 'wrap',
  },
  corsKey: { color: '#94a3b8', fontSize: 12, minWidth: 280 },
  corsVal: { color: '#4ade80', fontSize: 12, fontWeight: 600 },
  corsValMissing: { color: '#f87171', fontSize: 12, fontStyle: 'italic' },
};
