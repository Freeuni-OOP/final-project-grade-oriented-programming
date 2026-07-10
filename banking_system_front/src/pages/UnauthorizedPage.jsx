import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button, Card } from '../components/ui';
import styles from './StatusPage.module.css';

export default function UnauthorizedPage() {
  const navigate = useNavigate();
  const { t } = useTranslation('common');

  return (
    <div className={styles.fullPage}>
      <Card className={styles.centerCard}>
        <h1 className={styles.code}>403</h1>
        <p className={styles.message}>{t('unauthorized_message')}</p>
        <Button type="button" onClick={() => navigate(-1)}>
          {t('go_back')}
        </Button>
      </Card>
    </div>
  );
}
