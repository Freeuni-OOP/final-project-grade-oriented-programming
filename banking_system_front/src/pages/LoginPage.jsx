import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useAuth } from '../components/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setError,
  } = useForm();

  const onSubmit = async (values) => {
    try {
      const data = await authApi.login({ email: values.email, password: values.password });
      const token = data['Generated JWT token'];

      if (!token) {
        setError('root', { message: 'Login succeeded but no token was returned.' });
        return;
      }

      login(token);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      const msg = err.response?.data?.message ?? err.response?.data ?? 'Invalid email or password.';
      setError('root', { message: String(msg) });
    }
  };

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.heading}>Sign in</h1>

        <form onSubmit={handleSubmit(onSubmit)} noValidate style={styles.form}>
          <div style={styles.field}>
            <label htmlFor="email" style={styles.label}>
              Email
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              style={styles.input}
              {...register('email', {
                required: 'Email is required.',
                pattern: { value: /\S+@\S+\.\S+/, message: 'Enter a valid email.' },
              })}
            />
            {errors.email && <span style={styles.error}>{errors.email.message}</span>}
          </div>

          <div style={styles.field}>
            <label htmlFor="password" style={styles.label}>
              Password
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              style={styles.input}
              {...register('password', { required: 'Password is required.' })}
            />
            {errors.password && <span style={styles.error}>{errors.password.message}</span>}
          </div>

          {errors.root && <p style={styles.rootError}>{errors.root.message}</p>}

          <button type="submit" disabled={isSubmitting} style={styles.button}>
            {isSubmitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <p style={styles.footer}>
          No account? <Link to="/register">Create one</Link>
        </p>
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
  card: {
    background: '#fff',
    borderRadius: 8,
    padding: '40px 36px',
    width: '100%',
    maxWidth: 400,
    boxShadow: '0 2px 12px rgba(0,0,0,0.08)',
  },
  heading: { margin: '0 0 24px', fontSize: 24, fontWeight: 600 },
  form: { display: 'flex', flexDirection: 'column', gap: 16 },
  field: { display: 'flex', flexDirection: 'column', gap: 4 },
  label: { fontSize: 14, fontWeight: 500 },
  input: { padding: '8px 10px', fontSize: 14, borderRadius: 4, border: '1px solid #ccc' },
  error: { fontSize: 12, color: '#c0392b' },
  rootError: { fontSize: 13, color: '#c0392b', margin: 0 },
  button: {
    marginTop: 4,
    padding: '10px',
    fontSize: 14,
    fontWeight: 600,
    background: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: 4,
    cursor: 'pointer',
  },
  footer: { marginTop: 20, fontSize: 13, textAlign: 'center' },
};
