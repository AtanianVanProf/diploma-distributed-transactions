import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { interval, switchMap, takeWhile } from 'rxjs';
import { BenchmarkService } from '../../services/benchmark.service';
import {
  BenchmarkRun,
  BenchmarkStatus
} from '../../models/benchmark.models';

@Component({
  selector: 'app-benchmark-panel',
  standalone: true,
  templateUrl: './benchmark-panel.component.html',
  styleUrl: './benchmark-panel.component.scss'
})
export class BenchmarkPanelComponent {
  private benchmarkService = inject(BenchmarkService);
  private destroyRef = inject(DestroyRef);

  status = signal<BenchmarkStatus>('IDLE');
  currentRun = signal<BenchmarkRun | null>(null);
  isRunning = signal(false);
  errorMessage = signal<string | null>(null);

  constructor() {
    this.checkInitialStatus();
  }

  startBenchmark(): void {
    this.errorMessage.set(null);
    this.isRunning.set(true);
    this.status.set('RUNNING');

    this.benchmarkService.startBenchmark()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.pollForCompletion(response.runId);
        },
        error: (err) => {
          this.status.set('FAILED');
          this.isRunning.set(false);
          this.errorMessage.set(err.error?.message || 'Failed to start benchmark');
        }
      });
  }

  resetAll(): void {
    this.errorMessage.set(null);
    this.benchmarkService.reset()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.status.set('IDLE');
          this.currentRun.set(null);
        },
        error: (err) => {
          this.errorMessage.set(err.error?.message || 'Failed to reset');
        }
      });
  }

  private checkInitialStatus(): void {
    this.benchmarkService.getStatus()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.status.set(response.status);
          if (response.status === 'RUNNING') {
            this.isRunning.set(true);
          }
        }
      });
  }

  private pollForCompletion(runId: string): void {
    interval(2000)
      .pipe(
        switchMap(() => this.benchmarkService.getStatus()),
        takeWhile((response) => response.status === 'RUNNING', true),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (response) => {
          this.status.set(response.status);
          if (response.status === 'COMPLETED' || response.status === 'FAILED') {
            this.isRunning.set(false);
            if (response.status === 'COMPLETED') {
              this.fetchResults(runId);
            }
          }
        },
        error: () => {
          this.status.set('FAILED');
          this.isRunning.set(false);
        }
      });
  }

  private fetchResults(runId: string): void {
    this.benchmarkService.getRun(runId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (run) => {
          this.currentRun.set(run);
        }
      });
  }

  getStatusClass(): string {
    switch (this.status()) {
      case 'IDLE': return 'status-idle';
      case 'RUNNING': return 'status-running';
      case 'COMPLETED': return 'status-completed';
      case 'FAILED': return 'status-failed';
    }
  }
}
