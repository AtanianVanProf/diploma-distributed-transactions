import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { OrderApiService } from '../../services/order-api.service';
import { OrderResponse } from '../../models/order.model';

@Component({
  selector: 'app-order-panel',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './order-panel.html',
  styleUrl: './order-panel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderPanel implements OnInit {

  private readonly api = inject(OrderApiService);

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
