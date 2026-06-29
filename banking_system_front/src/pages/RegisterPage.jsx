import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { Button, Card, TextField, Toast } from '../components/ui';
import styles from './AuthPage.module.css';

const FIELDS = [
  { name: 'firstName', label: 'First name', type: 'text' },
  { name: 'lastName', label: 'Last name', type: 'text' },
  { name: 'email', label: 'Email', type: 'email' },
  { name: 'password', label: 'Password', type: 'password' },
  { name: 'phoneNumber', label: 'Phone number', type: 'tel' },
  { name: 'address', label: 'Address', type: 'text' },
  { name: 'dateOfBirth', label: 'Date of birth', type: 'date' },
];

const RULES = {
  firstName: { required: 'First name is required.' },
  lastName: { required: 'Last name is required.' },
  email: {
    required: 'Email is required.',
    pattern: { value: /\S+@\S+.\S+/, message: 'Enter a valid email.' },
  },
  password: {
    required: 'Password is required.',
    minLength: { value: 6, message: 'At least 6 characters.' },
    maxLength: { value: 20, message: 'At most 20 characters.' },
  },
  phoneNumber: {
    pattern: { value: /^\d+$/, message: 'Phone number must contain only digits.' },
  },
  address: {},
  dateOfBirth: { required: 'Date of birth is required.' },
};

export default function RegisterPage() {
  const navigate = useNavigate();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setError,
  } = useForm();

  const onSubmit = async (values) => {
    try {
      await authApi.register(values);
      navigate('/login', { replace: true, state: { registered: true } });
    } catch (err) {
      const msg =
        err.response?.data?.message ??
        err.response?.data ??
        'Registration failed. Please try again.';
      setError('root', { message: String(msg) });
    }
  };

  return (
    <div className={styles.page}>
      <Card className={styles.card}>
        <h1 className={styles.heading}>Create account</h1>

        <form onSubmit={handleSubmit(onSubmit)} noValidate className={styles.form}>
          {FIELDS.map(({ name, label, type }) => (
            <TextField
              key={name}
              id={name}
              type={type}
              label={label}
              error={errors[name]?.message}
              required={Boolean(RULES[name]?.required)}
              {...register(name, RULES[name])}
            />
          ))}

          {errors.root && <Toast variant="danger" message={errors.root.message} />}

          <Button type="submit" fullWidth isLoading={isSubmitting}>
            {isSubmitting ? 'Creating account…' : 'Create account'}
          </Button>
        </form>

        <p className={styles.footer}>
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </Card>
    </div>
  );
}
