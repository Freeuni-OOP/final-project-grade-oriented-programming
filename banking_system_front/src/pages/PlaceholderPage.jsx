import { Card } from '../components/ui';
import styles from './StatusPage.module.css';

export default function PlaceholderPage({ title, description }) {
  return (
    <Card className={styles.placeholderCard}>
      <p className={styles.kicker}>Placeholder page</p>
      <h1 className={styles.title}>{title}</h1>
      <p className={styles.message}>{description}</p>
    </Card>
  );
}
