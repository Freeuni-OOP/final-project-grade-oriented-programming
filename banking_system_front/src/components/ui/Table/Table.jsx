import Spinner from '../Spinner/Spinner';
import { cx } from '../utils';
import styles from './Table.module.css';

export default function Table({
  columns = [],
  data = [],
  getRowKey,
  emptyMessage = 'No records found.',
  loadingMessage = 'Loading records...',
  isLoading = false,
  caption,
  className = '',
}) {
  const columnCount = Math.max(columns.length, 1);

  return (
    <div className={cx(styles.tableWrap, className)}>
      <table className={styles.table}>
        {caption && <caption className={styles.caption}>{caption}</caption>}
        <thead>
          <tr>
            {columns.map((column) => (
              <th
                key={String(column.key)}
                className={cx(styles.th, styles[column.align ?? 'left'], column.headerClassName)}
                scope="col"
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {isLoading && (
            <tr>
              <td className={styles.stateCell} colSpan={columnCount}>
                <Spinner label={loadingMessage} />
              </td>
            </tr>
          )}
          {!isLoading &&
            data.map((row, rowIndex) => (
              <tr key={getRowKey ? getRowKey(row, rowIndex) : (row.id ?? row.key ?? rowIndex)}>
                {columns.map((column) => (
                  <td
                    key={String(column.key)}
                    className={cx(styles.td, styles[column.align ?? 'left'], column.cellClassName)}
                  >
                    {column.render ? column.render(row, rowIndex) : row[column.key]}
                  </td>
                ))}
              </tr>
            ))}
          {!isLoading && data.length === 0 && (
            <tr>
              <td className={styles.stateCell} colSpan={columnCount}>
                {emptyMessage}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
