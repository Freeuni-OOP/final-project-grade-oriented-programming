import { useRef } from 'react';
import styles from './ScrollStrip.module.css';

/**
 * A horizontally scrollable row of selectable chips.
 *
 * items       - array of records to render
 * getKey      - (item) => stable key; also used to match selectedKey
 * renderItem  - (item, isSelected) => chip contents
 * selectedKey - currently selected key (compared as string)
 * onSelect    - (item) => void
 */
export default function ScrollStrip({
  items = [],
  getKey,
  renderItem,
  selectedKey = null,
  onSelect,
  emptyMessage = 'Nothing to show yet.',
  ariaLabel,
}) {
  const trackRef = useRef(null);

  const scrollByPage = (direction) => {
    const track = trackRef.current;
    if (!track) return;
    track.scrollBy({ left: direction * track.clientWidth * 0.8, behavior: 'smooth' });
  };

  if (!items.length) {
    return <p className={styles.empty}>{emptyMessage}</p>;
  }

  return (
    <div className={styles.wrap}>
      <button
        type="button"
        className={styles.arrow}
        aria-label="Scroll left"
        onClick={() => scrollByPage(-1)}
      >
        &#8249;
      </button>

      <ul className={styles.track} ref={trackRef} aria-label={ariaLabel}>
        {items.map((item, index) => {
          const key = getKey(item);
          const isSelected = selectedKey != null && String(key) === String(selectedKey);

          return (
            <li key={key ?? index} className={styles.item}>
              <button
                type="button"
                className={`${styles.chip} ${isSelected ? styles.chipSelected : ''}`}
                aria-pressed={isSelected}
                onClick={() => onSelect?.(item)}
              >
                {renderItem(item, isSelected)}
              </button>
            </li>
          );
        })}
      </ul>

      <button
        type="button"
        className={styles.arrow}
        aria-label="Scroll right"
        onClick={() => scrollByPage(1)}
      >
        &#8250;
      </button>
    </div>
  );
}
