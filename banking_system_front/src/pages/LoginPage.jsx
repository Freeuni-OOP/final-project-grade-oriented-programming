import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { useAuth } from '../components/AuthContext';
import { Button, Card, TextField, Toast } from '../components/ui';
import styles from './AuthPage.module.css';

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
    <div className={styles.page}>
      <Card className={styles.card}>
        <h1 className={styles.heading}>Sign in</h1>

        <form onSubmit={handleSubmit(onSubmit)} noValidate className={styles.form}>
          <TextField
            id="email"
            type="email"
            label="Email"
            autoComplete="email"
            error={errors.email?.message}
            required
            {...register('email', {
              required: 'Email is required.',
              pattern: { value: /\S+@\S+\.\S+/, message: 'Enter a valid email.' },
            })}
          />

          <TextField
            id="password"
            type="password"
            label="Password"
            autoComplete="current-password"
            error={errors.password?.message}
            required
            {...register('password', { required: 'Password is required.' })}
          />

          {errors.root && <Toast variant="danger" message={errors.root.message} />}

          <Button type="submit" fullWidth isLoading={isSubmitting}>
            {isSubmitting ? 'Signing in…' : 'Sign in'}
          </Button>
        </form>

        <p className={styles.footer}>
          No account? <Link to="/register">Create one</Link>
        </p>
      </Card>
    </div>
  );
}
