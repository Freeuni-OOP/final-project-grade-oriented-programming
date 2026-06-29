import { Spinner } from '../components/ui';
import styles from './StatusPage.module.css';

export default function LoadingPage({ message = 'Loading page...' }) {
  return (
    <div className={styles.loadingPage}>
      <Spinner label={message} />
    </div>
  );
}
