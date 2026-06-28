import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/authApi';

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
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.heading}>Create account</h1>

        <form onSubmit={handleSubmit(onSubmit)} noValidate style={styles.form}>
          {FIELDS.map(({ name, label, type }) => (
            <div key={name} style={styles.field}>
              <label htmlFor={name} style={styles.label}>
                {label}
              </label>
              <input id={name} type={type} style={styles.input} {...register(name, RULES[name])} />
              {errors[name] && <span style={styles.error}>{errors[name].message}</span>}
            </div>
          ))}

          {errors.root && <p style={styles.rootError}>{errors.root.message}</p>}

          <button type="submit" disabled={isSubmitting} style={styles.button}>
            {isSubmitting ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <p style={styles.footer}>
          Already have an account? <Link to="/login">Sign in</Link>
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
    padding: '24px 0',
  },
  card: {
    background: '#fff',
    borderRadius: 8,
    padding: '40px 36px',
    width: '100%',
    maxWidth: 420,
    boxShadow: '0 2px 12px rgba(0,0,0,0.08)',
  },
  heading: { margin: '0 0 24px', fontSize: 24, fontWeight: 600 },
  form: { display: 'flex', flexDirection: 'column', gap: 14 },
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
