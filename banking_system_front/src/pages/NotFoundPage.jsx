import { Link } from 'react-router-dom';
import { Button, Card } from '../components/ui';
import styles from './StatusPage.module.css';

export default function NotFoundPage() {
  return (
    <Card className={styles.statusCard}>
      <p className={styles.code}>404</p>
      <h1 className={styles.title}>Page not found</h1>
      <p className={styles.message}>The route you opened does not exist in this app.</p>
      <Button as={Link} to="/dashboard">
        Go to dashboard
      </Button>
    </Card>
  );
}
