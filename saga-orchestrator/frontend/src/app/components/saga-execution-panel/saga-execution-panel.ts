import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { OrchestratorApiService } from '../../services/orchestrator-api.service';
import { SagaExecutionResponse } from '../../models/saga.model';

@Component({
  selector: 'app-saga-execution-panel',
  imports: [DatePipe],
  templateUrl: './saga-execution-panel.html',
  styleUrl: './saga-execution-panel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SagaExecutionPanel implements OnInit {

  private readonly api = inject(OrchestratorApiService);

  protected readonly executions = signal<SagaExecutionResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly expandedIds = signal<Set<number>>(new Set());

  ngOnInit(): void {
    this.fetchExecutions();
  }

  refresh(): void {
    this.fetchExecutions();
  }

  protected toggleExpand(id: number): void {
    const current = new Set(this.expandedIds());
    if (current.has(id)) {
      current.delete(id);
    } else {
      current.add(id);
    }
    this.expandedIds.set(current);
  }

  protected sagaStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'saga-completed';
      case 'COMPENSATED': return 'saga-compensated';
      case 'FAILED': return 'saga-failed';
      case 'COMPENSATING': return 'saga-compensating';
      case 'STARTED': return 'saga-started';
      default: return '';
    }
  }

  protected stepStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'step-completed';
      case 'COMPENSATED': return 'step-compensated';
      case 'COMPENSATION_PENDING': return 'step-compensation-pending';
      case 'COMPENSATION_FAILED': return 'step-compensation-failed';
      case 'FAILED': return 'step-failed';
      case 'PENDING': return 'step-pending';
      default: return '';
    }
  }

  protected formatStepName(name: string): string {
    return name.replace(/_/g, ' ');
  }

  private fetchExecutions(): void {
    this.loading.set(true);

    this.api.getSagaExecutions().subscribe({
      next: (executions) => {
        this.executions.set(executions);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }
}
