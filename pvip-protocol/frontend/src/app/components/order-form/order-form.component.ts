import { Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Product } from '../../models/product.model';
import { Customer } from '../../models/customer.model';
import { PlaceOrderRequest } from '../../models/place-order.model';

interface OrderItemRow {
  productId: number | null;
  quantity: number;
}

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './order-form.component.html',
  styleUrl: './order-form.component.scss'
})
export class OrderFormComponent {
  customers = input<Customer[]>([]);
  products = input<Product[]>([]);
  isProcessing = input<boolean>(false);

  orderSubmit = output<PlaceOrderRequest>();
  timeoutSubmit = output<PlaceOrderRequest>();
  resetRequested = output<void>();

  selectedCustomerId: number | null = null;
  items: OrderItemRow[] = [{ productId: null, quantity: 1 }];

  addItem(): void {
    this.items.push({ productId: null, quantity: 1 });
  }

  removeItem(index: number): void {
    this.items.splice(index, 1);
  }

  submit(): void {
    if (!this.selectedCustomerId || this.items.some(i => !i.productId || i.quantity < 1)) return;
    this.orderSubmit.emit({
      customerId: this.selectedCustomerId,
      items: this.items.map(i => ({ productId: i.productId!, quantity: i.quantity }))
    });
  }

  loadScenario(scenario: string): void {
    switch (scenario) {
      case 'happy':
        this.selectedCustomerId = 1;
        this.items = [{ productId: 1, quantity: 1 }];
        break;
      case 'stock':
        this.selectedCustomerId = 1;
        this.items = [{ productId: 3, quantity: 2 }];
        break;
      case 'balance':
        this.selectedCustomerId = 2;
        this.items = [{ productId: 1, quantity: 1 }];
        break;
      case 'timeout':
        this.selectedCustomerId = 1;
        this.items = [{ productId: 2, quantity: 1 }];
        this.submitTimeout();
        break;
    }
  }

  private submitTimeout(): void {
    if (!this.selectedCustomerId) return;
    this.timeoutSubmit.emit({
      customerId: this.selectedCustomerId,
      items: this.items.map(i => ({ productId: i.productId!, quantity: i.quantity }))
    });
  }

  reset(): void {
    this.selectedCustomerId = null;
    this.items = [{ productId: null, quantity: 1 }];
    this.resetRequested.emit();
  }
}
