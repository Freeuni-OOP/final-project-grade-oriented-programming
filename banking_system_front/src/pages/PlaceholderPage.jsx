import { useTranslation } from 'react-i18next';
import { Card } from '../components/ui';
import styles from './StatusPage.module.css';

export default function PlaceholderPage({ title, description }) {
  const { t } = useTranslation('common');

  return (
    <Card className={styles.placeholderCard}>
      <p className={styles.kicker}>{t('placeholder_page')}</p>
      <h1 className={styles.title}>{title}</h1>
      <p className={styles.message}>{description}</p>
    </Card>
  );
}
