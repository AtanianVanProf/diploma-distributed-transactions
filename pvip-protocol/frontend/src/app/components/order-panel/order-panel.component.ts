import { Component, input } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Order } from '../../models/order.model';

@Component({
  selector: 'app-order-panel',
  standalone: true,
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './order-panel.component.html',
  styleUrl: './order-panel.component.scss'
})
export class OrderPanelComponent {
  orders = input<Order[]>([]);

  getStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'status-completed';
      case 'FAILED': return 'status-failed';
      case 'PENDING': return 'status-pending';
      default: return 'status-pending';
    }
  }

  truncateId(id: string): string {
    return id.substring(0, 8) + '...';
  }
}
