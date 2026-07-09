/* eslint-disable react-refresh/only-export-components */
import styles from './shared.module.css';

// ---- value helpers (lifted out of the old CustomersPage so both this page
// ---- and the manager page can share one copy) ---------------------------

export function trimValue(value) {
  return String(value ?? '').trim();
}

export function formatValue(value) {
  return value === null || value === undefined || value === '' ? 'Not provided' : String(value);
}

// Backend serializes a boolean field `isActive` as `active`, so accept both.
export function getActiveValue(record) {
  if (typeof record?.active === 'boolean') return record.active;
  if (typeof record?.isActive === 'boolean') return record.isActive;
  return null;
}

export function getCustomerName(customer) {
  const firstName = trimValue(customer?.firstName);
  const lastName = trimValue(customer?.lastName);
  const fullName = `${firstName} ${lastName}`.trim();
  return fullName || customer?.email || 'Customer';
}

// Works for customer / account / card responses once the DTOs expose `id`.
export function getId(record) {
  return record?.id ?? record?.customerId ?? record?.accountId ?? record?.cardId ?? null;
}

// ---- presentational bits -------------------------------------------------

export function DetailItem({ label, children }) {
  return (
    <div className={styles.detailItem}>
      <dt className={styles.detailLabel}>{label}</dt>
      <dd className={styles.detailValue}>{children}</dd>
    </div>
  );
}

export function StatusBadge({ active }) {
  const tone =
    active === true ? styles.active : active === false ? styles.inactive : styles.unknown;
  const label = active === true ? 'Active' : active === false ? 'Inactive' : 'Unknown';

  return <span className={`${styles.badge} ${tone}`}>{label}</span>;
}
