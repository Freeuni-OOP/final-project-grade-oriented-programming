import { isRouteErrorResponse, useRouteError } from 'react-router-dom';
import { Button, Card } from '../components/ui';
import styles from './StatusPage.module.css';

export function ErrorFallback({
  title = 'Something went wrong',
  message = 'The page could not be loaded.',
}) {
  return (
    <div className={styles.fullPage}>
      <Card className={styles.statusCard}>
        <p className={styles.kickerDanger}>Error</p>
        <h1 className={styles.title}>{title}</h1>
        <p className={styles.message}>{message}</p>
        <Button as="a" href="/dashboard">
          Go to dashboard
        </Button>
      </Card>
    </div>
  );
}

export default function ErrorPage() {
  const error = useRouteError();
  let title = 'Something went wrong';
  let message = 'The page could not be loaded.';

  if (isRouteErrorResponse(error)) {
    title = `${error.status} ${error.statusText}`;
    message = error.data?.message ?? message;
  } else if (error instanceof Error) {
    message = error.message;
  }

  return <ErrorFallback title={title} message={message} />;
}
