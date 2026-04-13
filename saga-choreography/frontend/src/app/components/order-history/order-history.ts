import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { OrderApiService } from '../../services/order-api.service';
import { OrderResponse } from '../../models/order.model';
import { SagaExecutionResponse } from '../../models/saga.model';

@Component({
  selector: 'app-order-history',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './order-history.html',
  styleUrl: './order-history.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderHistory implements OnInit {

  private readonly api = inject(OrderApiService);

  protected readonly orders = signal<OrderResponse[]>([]);
  protected readonly sagas = signal<SagaExecutionResponse[]>([]);
  protected readonly loading = signal(false);

  ngOnInit(): void {
    this.fetchData();
  }

  refresh(): void {
    this.fetchData();
  }

  protected getSagaStatus(orderId: number): string {
    const saga = this.sagas().find(s => s.orderId === orderId);
    return saga ? saga.status : '—';
  }

  protected sagaStatusClass(orderId: number): string {
    const status = this.getSagaStatus(orderId);
    switch (status) {
      case 'COMPLETED': return 'saga-completed';
      case 'COMPENSATED': return 'saga-compensated';
      case 'FAILED': return 'saga-failed';
      case 'STARTED': return 'saga-started';
      case 'PENDING': return 'saga-pending';
      default: return '';
    }
  }

  private fetchData(): void {
    this.loading.set(true);

    forkJoin({
      orders: this.api.getOrders(),
      sagas: this.api.getSagaExecutions()
    }).subscribe({
      next: ({ orders, sagas }) => {
        this.orders.set(orders);
        this.sagas.set(sagas);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
