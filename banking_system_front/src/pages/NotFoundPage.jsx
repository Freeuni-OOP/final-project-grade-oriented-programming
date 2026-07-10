import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Button, Card } from '../components/ui';
import styles from './StatusPage.module.css';

export default function NotFoundPage() {
  const { t } = useTranslation('common');

  return (
    <Card className={styles.statusCard}>
      <p className={styles.code}>404</p>
      <h1 className={styles.title}>{t('page_not_found')}</h1>
      <p className={styles.message}>{t('route_does_not_exist')}</p>
      <Button as={Link} to="/customers">
        {t('go_to_profile')}
      </Button>
    </Card>
  );
}
