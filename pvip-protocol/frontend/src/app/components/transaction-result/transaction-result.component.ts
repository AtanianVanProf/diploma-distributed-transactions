import { Component, input } from '@angular/core';
import { TransactionResult } from '../../models/transaction-result.model';

@Component({
  selector: 'app-transaction-result',
  standalone: true,
  templateUrl: './transaction-result.component.html',
  styleUrl: './transaction-result.component.scss'
})
export class TransactionResultComponent {
  result = input<TransactionResult | null>(null);

  getOutcomeClass(): string {
    const r = this.result();
    if (!r) return '';
    if (r.response.status === 'FAILED') return 'outcome-rejected';
    if (r.execution?.status === 'COMMITTED') return 'outcome-committed';
    if (r.execution?.status === 'REJECTED') return 'outcome-rejected';
    return 'outcome-pending';
  }

  getOutcomeText(): string {
    const r = this.result();
    if (!r) return '';
    if (r.response.status === 'FAILED') return 'REJECTED';
    if (r.execution?.status === 'COMMITTED') return 'COMMITTED';
    if (r.execution?.status === 'REJECTED') return 'REJECTED';
    return 'PROCESSING...';
  }

  getPhaseText(): string {
    const r = this.result();
    if (!r) return '';
    if (r.response.status === 'FAILED') return 'Pre-Validation (instant rejection)';
    if (r.execution) return r.execution.phase;
    return 'Collecting Intents';
  }

  getDuration(): string {
    const r = this.result();
    if (!r) return '-';
    if (r.response.status === 'FAILED') return 'Instant';
    if (!r.execution?.completedAt || !r.execution?.startedAt) return '-';
    const ms = new Date(r.execution.completedAt).getTime() - new Date(r.execution.startedAt).getTime();
    return ms < 1000 ? `${ms}ms` : `${(ms / 1000).toFixed(1)}s`;
  }

  getReason(): string | null {
    const r = this.result();
    if (!r) return null;
    if (r.response.reason) return r.response.reason;
    if (r.execution?.decisionReason) return r.execution.decisionReason;
    return null;
  }
}
