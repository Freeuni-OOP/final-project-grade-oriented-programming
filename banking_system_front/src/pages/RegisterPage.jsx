import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { applyBackendFormErrors } from '../api/formErrors';
import { Button, Card, TextField, Toast } from '../components/ui';
import { useTranslation } from 'react-i18next';
import styles from './AuthPage.module.css';

const FIELDS = [
  { name: 'firstName', labelKey: 'first_name', type: 'text' },
  { name: 'lastName', labelKey: 'last_name', type: 'text' },
  { name: 'email', labelKey: 'label_email', type: 'email' },
  { name: 'password', labelKey: 'label_password', type: 'password' },
  { name: 'phoneNumber', labelKey: 'phone_number', type: 'tel' },
  { name: 'address', labelKey: 'label_address', type: 'text' },
  { name: 'dateOfBirth', labelKey: 'label_dob', type: 'date' },
];

const REGISTER_FIELDS = FIELDS.map(({ name }) => name);

function blankToNull(value) {
  return value?.trim() ? value.trim() : null;
}

function buildRegistrationPayload(values) {
  return {
    ...values,
    email: values.email.trim(),
    phoneNumber: blankToNull(values.phoneNumber),
    address: blankToNull(values.address),
  };
}

export default function RegisterPage() {
  const navigate = useNavigate();
  const { t } = useTranslation('customer');
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setError,
  } = useForm();

  const RULES = {
    firstName: { required: t('first_name_required') },
    lastName: { required: t('last_name_required') },
    email: {
      required: t('email_required'),
      pattern: { value: /\S+@\S+\.\S+/, message: t('email_invalid') },
    },
    password: {
      required: t('password_required'),
      minLength: { value: 6, message: t('password_min') },
      maxLength: { value: 20, message: t('password_max') },
    },
    phoneNumber: {
      pattern: { value: /^\d+$/, message: t('phone_digits_only') },
    },
    address: {},
    dateOfBirth: { required: t('dob_required') },
  };

  const onSubmit = async (values) => {
    try {
      await authApi.register(buildRegistrationPayload(values));
      navigate('/login', { replace: true, state: { registered: true } });
    } catch (err) {
      applyBackendFormErrors(err, setError, REGISTER_FIELDS);
    }
  };

  return (
    <div className={styles.page}>
      <Card className={styles.card}>
        <h1 className={styles.heading}>{t('create_account')}</h1>

        <form onSubmit={handleSubmit(onSubmit)} noValidate className={styles.form}>
          {FIELDS.map(({ name, labelKey, type }) => (
            <TextField
              key={name}
              id={name}
              type={type}
              label={t(labelKey)}
              error={errors[name]?.message}
              required={Boolean(RULES[name]?.required)}
              {...register(name, RULES[name])}
            />
          ))}

          {errors.root && <Toast variant="danger" message={errors.root.message} />}

          <Button type="submit" fullWidth isLoading={isSubmitting}>
            {isSubmitting ? t('creating_account') : t('create_account')}
          </Button>
        </form>

        <p className={styles.footer}>
          {t('already_have_account')} <Link to="/login">{t('sign_in')}</Link>
        </p>
      </Card>
    </div>
  );
}
