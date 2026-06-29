import { useNavigate } from 'react-router-dom';
import { Button, Card } from '../components/ui';
import styles from './StatusPage.module.css';

export default function UnauthorizedPage() {
  const navigate = useNavigate();
  return (
    <div className={styles.fullPage}>
      <Card className={styles.centerCard}>
        <h1 className={styles.code}>403</h1>
        <p className={styles.message}>You don't have permission to view this page.</p>
        <Button type="button" onClick={() => navigate(-1)}>
          Go back
        </Button>
      </Card>
    </div>
  );
}
