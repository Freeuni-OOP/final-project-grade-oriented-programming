import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { customerApi } from '../api/customerApi';
import { customerKeys } from '../api/customerQueryKeys';
import { applyBackendFormErrors } from '../api/formErrors';
import { Button, Card, Spinner, Table, TextField, Toast, useToast } from '../components/ui';
import styles from './CustomerPage.module.css';

const UPDATE_FIELDS = ['firstName', 'lastName', 'phoneNumber', 'address'];

const UPDATE_DEFAULTS = {
  firstName: '',
  lastName: '',
  phoneNumber: '',
  address: '',
};

function trimValue(value) {
  return String(value ?? '').trim();
}

function blankToNull(value) {
  const trimmed = trimValue(value);
  return trimmed ? trimmed : null;
}

function formatValue(value) {
  return value === null || value === undefined || value === '' ? 'Not provided' : String(value);
}

function getCustomerId(customer) {
  return (
    customer?.id ?? customer?.customerId ?? customer?.customerID ?? customer?.customer_id ?? null
  );
}

function getActiveValue(record) {
  if (typeof record?.active === 'boolean') return record.active;
  if (typeof record?.isActive === 'boolean') return record.isActive;
  return null;
}

function getCustomerName(customer) {
  const firstName = trimValue(customer?.firstName);
  const lastName = trimValue(customer?.lastName);
  const fullName = `${firstName} ${lastName}`.trim();
  return fullName || 'Customer';
}

function buildUpdateDefaults(customer) {
  return {
    firstName: customer?.firstName ?? '',
    lastName: customer?.lastName ?? '',
    phoneNumber: customer?.phoneNumber ?? '',
    address: customer?.address ?? '',
  };
}

function buildUpdatePayload(values) {
  return {
    firstName: trimValue(values.firstName),
    lastName: trimValue(values.lastName),
    phoneNumber: blankToNull(values.phoneNumber),
    address: blankToNull(values.address),
  };
}

function DetailItem({ label, children }) {
  return (
    <div className={styles.detailItem}>
      <dt className={styles.detailLabel}>{label}</dt>
      <dd className={styles.detailValue}>{children}</dd>
    </div>
  );
}

function StatusBadge({ active }) {
  const className =
    active === true ? styles.active : active === false ? styles.inactive : styles.unknown;
  const label = active === true ? 'Active' : active === false ? 'Inactive' : 'Unknown';

  return <span className={`${styles.badge} ${className}`}>{label}</span>;
}

export default function CustomersPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const [lookup, setLookup] = useState(null);

  const {
    register: registerIdLookup,
    handleSubmit: handleIdLookupSubmit,
    formState: { errors: idLookupErrors },
  } = useForm({ defaultValues: { customerId: '' } });

  const {
    register: registerEmailLookup,
    handleSubmit: handleEmailLookupSubmit,
    formState: { errors: emailLookupErrors },
  } = useForm({ defaultValues: { email: '' } });

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm({ defaultValues: UPDATE_DEFAULTS });

  const lookupQueryKey = useMemo(() => {
    if (!lookup) {
      return [...customerKeys.all, 'lookup', 'idle'];
    }

    return lookup.type === 'id'
      ? customerKeys.byId(lookup.value)
      : customerKeys.byEmail(lookup.value);
  }, [lookup]);

  const customerQuery = useQuery({
    queryKey: lookupQueryKey,
    queryFn: () =>
      lookup.type === 'id'
        ? customerApi.getById(lookup.value)
        : customerApi.getByEmail(lookup.value),
    enabled: Boolean(lookup),
    retry: false,
  });

  const customer = customerQuery.data;
  const responseCustomerId = getCustomerId(customer);
  const customerId = responseCustomerId ?? (lookup?.type === 'id' ? lookup.value : null);
  const activeValue = getActiveValue(customer);
  const canWriteCustomer = Boolean(customerId);

  useEffect(() => {
    if (customer) {
      reset(buildUpdateDefaults(customer));
    }
  }, [customer, reset]);

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }) => customerApi.update(id, payload),
    retry: false,
    onSuccess: (updatedCustomer, variables) => {
      queryClient.setQueryData(customerKeys.byId(variables.id), updatedCustomer);
      if (updatedCustomer?.email) {
        queryClient.setQueryData(customerKeys.byEmail(updatedCustomer.email), updatedCustomer);
      }
      queryClient.invalidateQueries({ queryKey: customerKeys.all });
      showToast({ title: 'Customer profile updated.', variant: 'success' });
    },
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, action }) =>
      action === 'activate' ? customerApi.activate(id) : customerApi.deactivate(id),
    retry: false,
    onSuccess: (_result, variables) => {
      queryClient.invalidateQueries({ queryKey: customerKeys.all });
      showToast({
        title: variables.action === 'activate' ? 'Customer activated.' : 'Customer deactivated.',
        variant: 'success',
      });
    },
  });

  const submitIdLookup = ({ customerId: value }) => {
    setLookup({ type: 'id', value: trimValue(value) });
  };

  const submitEmailLookup = ({ email }) => {
    setLookup({ type: 'email', value: trimValue(email) });
  };

  const submitUpdate = async (values) => {
    if (!customerId) {
      setError('root', { message: 'Customer ID is required before updating.' });
      return;
    }

    try {
      await updateMutation.mutateAsync({
        id: customerId,
        payload: buildUpdatePayload(values),
      });
    } catch (error) {
      applyBackendFormErrors(error, setError, UPDATE_FIELDS);
    }
  };

  const changeStatus = (action) => {
    if (!customerId) {
      return;
    }

    statusMutation.mutate({ id: customerId, action });
  };

  const accountColumns = useMemo(
    () => [
      { key: 'name', header: 'Name', render: (account) => formatValue(account.name) },
      { key: 'category', header: 'Category', render: (account) => formatValue(account.category) },
      {
        key: 'dateOpened',
        header: 'Opened',
        render: (account) => formatValue(account.dateOpened),
      },
      {
        key: 'status',
        header: 'Status',
        render: (account) => <StatusBadge active={getActiveValue(account)} />,
      },
    ],
    []
  );

  const statusAction = statusMutation.isPending ? statusMutation.variables?.action : null;
  const isUpdatePending = updateMutation.isPending || isSubmitting;

  return (
    <div className={styles.page}>
      <header className={styles.pageHeader}>
        <p className={styles.kicker}>Customer area</p>
        <h1 className={styles.title}>Customers</h1>
      </header>

      <Card title="Customer lookup">
        <div className={styles.lookupGrid}>
          <form className={styles.inlineForm} onSubmit={handleIdLookupSubmit(submitIdLookup)}>
            <TextField
              id="customer-id-lookup"
              label="Customer ID"
              type="number"
              min="1"
              error={idLookupErrors.customerId?.message}
              required
              {...registerIdLookup('customerId', {
                required: 'Customer ID is required.',
                validate: (value) =>
                  /^\d+$/.test(trimValue(value)) || 'Customer ID must be a positive number.',
              })}
            />
            <Button type="submit" isLoading={customerQuery.isFetching && lookup?.type === 'id'}>
              Search ID
            </Button>
          </form>

          <form className={styles.inlineForm} onSubmit={handleEmailLookupSubmit(submitEmailLookup)}>
            <TextField
              id="customer-email-lookup"
              label="Email"
              type="email"
              error={emailLookupErrors.email?.message}
              required
              {...registerEmailLookup('email', {
                required: 'Email is required.',
                pattern: { value: /\S+@\S+\.\S+/, message: 'Enter a valid email.' },
              })}
            />
            <Button type="submit" isLoading={customerQuery.isFetching && lookup?.type === 'email'}>
              Search Email
            </Button>
          </form>
        </div>
      </Card>

      <Card
        title="Customer profile"
        actions={
          customer && (
            <div className={styles.actionsRow}>
              <Button
                type="button"
                variant="secondary"
                disabled={!canWriteCustomer || activeValue === true}
                isLoading={statusAction === 'activate'}
                onClick={() => changeStatus('activate')}
              >
                Activate
              </Button>
              <Button
                type="button"
                variant="danger"
                disabled={!canWriteCustomer || activeValue === false}
                isLoading={statusAction === 'deactivate'}
                onClick={() => changeStatus('deactivate')}
              >
                Deactivate
              </Button>
            </div>
          )
        }
      >
        {!lookup && <p className={styles.mutedText}>No customer selected.</p>}

        {lookup && customerQuery.isLoading && (
          <div className={styles.centerState}>
            <Spinner label="Loading customer..." />
          </div>
        )}

        {lookup && customerQuery.isError && (
          <Toast variant="danger" message="Customer profile could not be loaded." />
        )}

        {customer && (
          <div className={styles.stack}>
            <dl className={styles.profileGrid}>
              <DetailItem label="Name">{getCustomerName(customer)}</DetailItem>
              <DetailItem label="Status">
                <StatusBadge active={activeValue} />
              </DetailItem>
              <DetailItem label="Customer ID">{formatValue(customerId)}</DetailItem>
              <DetailItem label="Email">{formatValue(customer.email)}</DetailItem>
              <DetailItem label="Phone">{formatValue(customer.phoneNumber)}</DetailItem>
              <DetailItem label="Date of birth">{formatValue(customer.dateOfBirth)}</DetailItem>
              <DetailItem label="Address">{formatValue(customer.address)}</DetailItem>
            </dl>

            <Table
              columns={accountColumns}
              data={customer.accounts ?? []}
              getRowKey={(account, index) =>
                `${account.name ?? 'account'}-${account.dateOpened ?? index}`
              }
              emptyMessage="No linked accounts."
              caption="Linked accounts"
            />
          </div>
        )}
      </Card>

      <Card title="Update profile">
        <form className={styles.stack} onSubmit={handleSubmit(submitUpdate)} noValidate>
          <div className={styles.formGrid}>
            <TextField
              id="customer-first-name"
              label="First name"
              error={errors.firstName?.message}
              disabled={!customer}
              required
              {...register('firstName', { required: 'First name is required.' })}
            />
            <TextField
              id="customer-last-name"
              label="Last name"
              error={errors.lastName?.message}
              disabled={!customer}
              required
              {...register('lastName', { required: 'Last name is required.' })}
            />
            <TextField
              id="customer-phone-number"
              label="Phone number"
              type="tel"
              error={errors.phoneNumber?.message}
              disabled={!customer}
              {...register('phoneNumber', {
                pattern: { value: /^\d+$/, message: 'Phone number must contain only digits.' },
              })}
            />
            <TextField
              id="customer-address"
              label="Address"
              error={errors.address?.message}
              disabled={!customer}
              {...register('address')}
            />
          </div>

          {errors.root && <Toast variant="danger" message={errors.root.message} />}

          <div className={styles.actionsRow}>
            <Button type="submit" isLoading={isUpdatePending} disabled={!customer || !customerId}>
              Save changes
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
