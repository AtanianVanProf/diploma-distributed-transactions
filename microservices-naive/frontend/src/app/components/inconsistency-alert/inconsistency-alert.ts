import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface InconsistencyRecord {
  orderId?: number;
  items: { field: string; before: string; after: string; service: string }[];
}

@Component({
  selector: 'app-inconsistency-alert',
  templateUrl: './inconsistency-alert.html',
  styleUrl: './inconsistency-alert.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InconsistencyAlert {

  records = input<InconsistencyRecord[]>([]);

  protected visible = computed(() => this.records().length > 0);

  /** Total number of stock units leaked across all records */
  protected totalLeakedUnits = computed(() => {
    let total = 0;
    for (const record of this.records()) {
      for (const item of record.items) {
        if (item.field.includes('Stock')) {
          const before = parseInt(item.before, 10);
          const after = parseInt(item.after, 10);
          if (!isNaN(before) && !isNaN(after)) {
            total += Math.abs(before - after);
          }
        }
      }
    }
    return total;
  });

  /** Distinct affected product/field entries with their cumulative changes */
  protected affectedItems = computed(() => {
    const map = new Map<string, { field: string; totalDiff: number; service: string }>();

    for (const record of this.records()) {
      for (const item of record.items) {
        const existing = map.get(item.field);
        const before = parseFloat(item.before.replace('$', ''));
        const after = parseFloat(item.after.replace('$', ''));
        const diff = !isNaN(before) && !isNaN(after) ? Math.abs(before - after) : 0;

        if (existing) {
          existing.totalDiff += diff;
        } else {
          map.set(item.field, { field: item.field, totalDiff: diff, service: item.service });
        }
      }
    }

    return Array.from(map.values());
  });
}
