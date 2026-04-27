import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { OrderResponse } from '../../models/order.model';

@Component({
  selector: 'app-order-history',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './order-history.html',
  styleUrl: './order-history.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderHistory implements OnInit {

  private readonly api = inject(ApiService);

  protected readonly orders = signal<OrderResponse[]>([]);
  protected readonly loading = signal(false);

  ngOnInit(): void {
    this.fetchOrders();
  }

  refresh(): void {
    this.fetchOrders();
  }

  private fetchOrders(): void {
    this.loading.set(true);

    this.api.getOrders().subscribe({
      next: (orders) => {
        this.orders.set(orders);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }
}
