import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { customerApi } from '../api/customerApi';
import { customerKeys } from '../api/customerQueryKeys';
import { Button, Card, Modal, Table, TextField, useToast } from '../components/ui';
import styles from './CustomerPage.module.css';

function trimValue(value) {
  return String(value ?? '').trim();
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
  return fullName || customer?.email || 'Customer';
}

function StatusBadge({ active }) {
  const className =
    active === true ? styles.active : active === false ? styles.inactive : styles.unknown;
  const label = active === true ? 'Active' : active === false ? 'Inactive' : 'Unknown';

  return <span className={`${styles.badge} ${className}`}>{label}</span>;
}

export default function ManagerCustomersPage({ title = 'Admin' }) {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const [accountId, setAccountId] = useState(null);
  const [pendingDelete, setPendingDelete] = useState(null);

  const {
    register: registerAccountLookup,
    handleSubmit: handleAccountLookupSubmit,
    formState: { errors: accountLookupErrors },
  } = useForm({ defaultValues: { accountId: '' } });

  const {
    register: registerDelete,
    handleSubmit: handleDeleteSubmit,
    reset: resetDeleteForm,
    formState: { errors: deleteErrors },
  } = useForm({ defaultValues: { customerId: '' } });

  const accountCustomersQuery = useQuery({
    queryKey: accountId
      ? customerKeys.byAccount(accountId)
      : [...customerKeys.all, 'account', 'idle'],
    queryFn: () => customerApi.getByAccount(accountId),
    enabled: Boolean(accountId),
    retry: false,
  });

  const deleteMutation = useMutation({
    mutationFn: (customerId) => customerApi.delete(customerId),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: customerKeys.all });
      resetDeleteForm();
      setPendingDelete(null);
      showToast({ title: 'Customer deleted.', variant: 'success' });
    },
  });

  const submitAccountLookup = ({ accountId: value }) => {
    setAccountId(trimValue(value));
  };

  const requestManualDelete = ({ customerId }) => {
    const id = trimValue(customerId);
    setPendingDelete({ id, label: `customer ${id}` });
  };

  const requestRowDelete = (customer) => {
    const id = getCustomerId(customer);
    if (!id) {
      return;
    }

    setPendingDelete({ id, label: getCustomerName(customer) });
  };

  const confirmDelete = () => {
    if (!pendingDelete?.id) {
      return;
    }

    deleteMutation.mutate(pendingDelete.id);
  };

  const customerColumns = useMemo(
    () => [
      {
        key: 'id',
        header: 'ID',
        render: (customer) => formatValue(getCustomerId(customer)),
      },
      {
        key: 'name',
        header: 'Name',
        render: (customer) => getCustomerName(customer),
      },
      {
        key: 'email',
        header: 'Email',
        render: (customer) => formatValue(customer.email),
      },
      {
        key: 'phoneNumber',
        header: 'Phone',
        render: (customer) => formatValue(customer.phoneNumber),
      },
      {
        key: 'status',
        header: 'Status',
        render: (customer) => <StatusBadge active={getActiveValue(customer)} />,
      },
      {
        key: 'actions',
        header: 'Actions',
        align: 'right',
        render: (customer) => {
          const customerId = getCustomerId(customer);

          return (
            <div className={styles.tableActions}>
              <Button
                type="button"
                variant="danger"
                size="sm"
                disabled={!customerId || deleteMutation.isPending}
                onClick={() => requestRowDelete(customer)}
              >
                Delete
              </Button>
            </div>
          );
        },
      },
    ],
    [deleteMutation.isPending]
  );

  const customers = accountCustomersQuery.data ?? [];

  return (
    <div className={styles.page}>
      <header className={styles.pageHeader}>
        <p className={styles.kicker}>Manager</p>
        <h1 className={styles.title}>{title}</h1>
      </header>

      <Card title="Account customers">
        <div className={styles.stack}>
          <form
            className={styles.inlineForm}
            onSubmit={handleAccountLookupSubmit(submitAccountLookup)}
          >
            <TextField
              id="manager-account-id"
              label="Account ID"
              type="number"
              min="1"
              error={accountLookupErrors.accountId?.message}
              required
              {...registerAccountLookup('accountId', {
                required: 'Account ID is required.',
                validate: (value) =>
                  /^\d+$/.test(trimValue(value)) || 'Account ID must be a positive number.',
              })}
            />
            <Button type="submit" isLoading={accountCustomersQuery.isFetching}>
              Load customers
            </Button>
          </form>

          <Table
            columns={customerColumns}
            data={customers}
            getRowKey={(customer, index) => getCustomerId(customer) ?? customer.email ?? index}
            emptyMessage={accountId ? 'No customers found for this account.' : 'No account loaded.'}
            loadingMessage="Loading customers..."
            isLoading={accountCustomersQuery.isLoading}
          />
        </div>
      </Card>

      <Card title="Delete customer">
        <form className={styles.inlineForm} onSubmit={handleDeleteSubmit(requestManualDelete)}>
          <TextField
            id="manager-delete-customer-id"
            label="Customer ID"
            type="number"
            min="1"
            error={deleteErrors.customerId?.message}
            required
            {...registerDelete('customerId', {
              required: 'Customer ID is required.',
              validate: (value) =>
                /^\d+$/.test(trimValue(value)) || 'Customer ID must be a positive number.',
            })}
          />
          <Button type="submit" variant="danger" disabled={deleteMutation.isPending}>
            Delete
          </Button>
        </form>
      </Card>

      <Modal
        open={Boolean(pendingDelete)}
        title="Delete customer"
        onClose={() => setPendingDelete(null)}
        footer={
          <>
            <Button
              type="button"
              variant="secondary"
              disabled={deleteMutation.isPending}
              onClick={() => setPendingDelete(null)}
            >
              Cancel
            </Button>
            <Button
              type="button"
              variant="danger"
              isLoading={deleteMutation.isPending}
              onClick={confirmDelete}
            >
              Delete customer
            </Button>
          </>
        }
      >
        <p className={styles.modalText}>
          Delete {pendingDelete?.label ?? 'this customer'}? This action cannot be undone.
        </p>
      </Modal>
    </div>
  );
}
