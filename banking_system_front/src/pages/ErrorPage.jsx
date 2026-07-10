import { useTranslation } from 'react-i18next';
import { isRouteErrorResponse, useRouteError } from 'react-router-dom';
import { Button, Card } from '../components/ui';
import styles from './StatusPage.module.css';

export function ErrorFallback({ title, message }) {
  const { t } = useTranslation('common');

  const fallbackTitle = title || t('something_went_wrong');
  const fallbackMessage = message || t('page_could_not_be_loaded');

  return (
    <div className={styles.fullPage}>
      <Card className={styles.statusCard}>
        <p className={styles.kickerDanger}>{t('error')}</p>
        <h1 className={styles.title}>{fallbackTitle}</h1>
        <p className={styles.message}>{fallbackMessage}</p>
        <Button as="a" href="/customers">
          {t('go_to_profile')}
        </Button>
      </Card>
    </div>
  );
}

export default function ErrorPage() {
  const { t } = useTranslation('common');
  const error = useRouteError();
  let title = t('something_went_wrong');
  let message = t('page_could_not_be_loaded');

  if (isRouteErrorResponse(error)) {
    title = `${error.status} ${error.statusText}`;
    message = error.data?.message ?? message;
  } else if (error instanceof Error) {
    message = error.message;
  }

  return <ErrorFallback title={title} message={message} />;
}
