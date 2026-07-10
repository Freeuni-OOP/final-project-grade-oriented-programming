import { useTranslation } from 'react-i18next';
import { Spinner } from '../components/ui';
import styles from './StatusPage.module.css';

export default function LoadingPage({ message }) {
  const { t } = useTranslation('common');
  const loadingMessage = message || t('loading');

  return (
    <div className={styles.loadingPage}>
      <Spinner label={loadingMessage} />
    </div>
  );
}
