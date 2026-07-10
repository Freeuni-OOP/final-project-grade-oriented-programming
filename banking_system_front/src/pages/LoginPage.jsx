import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { applyBackendFormErrors } from '../api/formErrors';
import { useAuth } from '../components/AuthContext';
import { Button, Card, TextField, Toast } from '../components/ui';
import { useTranslation } from 'react-i18next';
import styles from './AuthPage.module.css';

const LOGIN_FIELDS = ['email', 'password'];

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation('customer');

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
        setError('root', { message: t('login_no_token') });
        return;
      }

      login(token);
      navigate('/customers', { replace: true });
    } catch (err) {
      applyBackendFormErrors(err, setError, LOGIN_FIELDS);
    }
  };

  return (
    <div className={styles.page}>
      <Card className={styles.card}>
        <h1 className={styles.heading}>{t('sign_in')}</h1>

        <form onSubmit={handleSubmit(onSubmit)} noValidate className={styles.form}>
          <TextField
            id="email"
            type="email"
            label={t('label_email')}
            autoComplete="email"
            error={errors.email?.message}
            required
            {...register('email', {
              required: t('email_required'),
              pattern: { value: /\S+@\S+\.\S+/, message: t('email_invalid') },
            })}
          />

          <TextField
            id="password"
            type="password"
            label={t('label_password')}
            autoComplete="current-password"
            error={errors.password?.message}
            required
            {...register('password', { required: t('password_required') })}
          />

          {errors.root && <Toast variant="danger" message={errors.root.message} />}

          <Button type="submit" fullWidth isLoading={isSubmitting}>
            {isSubmitting ? t('signing_in') : t('sign_in')}
          </Button>
        </form>

        <p className={styles.footer}>
          {t('no_account')} <Link to="/register">{t('create_one')}</Link>
        </p>
      </Card>
    </div>
  );
}
