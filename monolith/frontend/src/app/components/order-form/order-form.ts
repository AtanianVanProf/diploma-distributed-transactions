import { ChangeDetectionStrategy, Component, inject, OnInit, output, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { Customer } from '../../models/customer.model';
import { Product } from '../../models/product.model';
import {
  CreateOrderRequest,
  ErrorResponse,
  OrderResponse
} from '../../models/order.model';

interface OrderItemRow {
  productId: number | null;
  quantity: number;
}

interface Scenario {
  label: string;
  customerId: number;
  items: OrderItemRow[];
}

@Component({
  selector: 'app-order-form',
  templateUrl: './order-form.html',
  styleUrl: './order-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderForm implements OnInit {

  private readonly api = inject(ApiService);

  /** Output events for parent component */
  beforeSubmit = output<CreateOrderRequest>();
  orderPlaced = output<OrderResponse>();
  orderFailed = output<ErrorResponse>();

  /** Data from API */
  protected readonly customers = signal<Customer[]>([]);
  protected readonly products = signal<Product[]>([]);
  protected readonly loading = signal(false);

  /** Form state */
  protected readonly selectedCustomerId = signal<number | null>(null);
  protected readonly items = signal<OrderItemRow[]>([{ productId: null, quantity: 1 }]);
  protected readonly submitting = signal(false);

  /** Pre-configured scenarios for quick demo fills */
  protected readonly scenarios: Scenario[] = [
    {
      label: 'Happy Path',
      customerId: 1,
      items: [{ productId: 1, quantity: 1 }]
    },
    {
      label: 'Insufficient Stock',
      customerId: 1,
      items: [{ productId: 3, quantity: 2 }]
    },
    {
      label: 'Insufficient Balance',
      customerId: 2,
      items: [{ productId: 1, quantity: 1 }]
    },
    {
      label: 'Multi-Item Failure',
      customerId: 1,
      items: [
        { productId: 1, quantity: 1 },
        { productId: 2, quantity: 5 }
      ]
    }
  ];

  ngOnInit(): void {
    this.loading.set(true);

    forkJoin({
      customers: this.api.getCustomers(),
      products: this.api.getProducts()
    }).subscribe({
      next: ({ customers, products }) => {
        this.customers.set(customers);
        this.products.set(products);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  /** Apply a scenario — fills form fields without submitting */
  protected applyScenario(scenario: Scenario): void {
    this.selectedCustomerId.set(scenario.customerId);
    // Deep-copy items so mutations don't affect the scenario definition
    this.items.set(scenario.items.map(item => ({ ...item })));
  }

  protected onCustomerChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedCustomerId.set(value ? Number(value) : null);
  }

  protected onProductChange(index: number, event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.updateItem(index, { productId: value ? Number(value) : null });
  }

  protected onQuantityChange(index: number, event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.updateItem(index, { quantity: Math.max(1, Number(value) || 1) });
  }

  protected addItem(): void {
    this.items.update(current => [...current, { productId: null, quantity: 1 }]);
  }

  protected removeItem(index: number): void {
    this.items.update(current => current.filter((_, i) => i !== index));
  }

  protected placeOrder(): void {
    const customerId = this.selectedCustomerId();
    const currentItems = this.items();

    if (customerId === null) return;
    if (currentItems.some(item => item.productId === null || item.quantity < 1)) return;

    const request: CreateOrderRequest = {
      customerId,
      items: currentItems.map(item => ({
        productId: item.productId!,
        quantity: item.quantity
      }))
    };

    this.submitting.set(true);
    this.beforeSubmit.emit(request);

    this.api.placeOrder(request).subscribe({
      next: (response) => {
        this.submitting.set(false);
        this.orderPlaced.emit(response);
        this.resetForm();
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        const errorResponse: ErrorResponse = err.error?.error
          ? err.error
          : { error: 'UNKNOWN', message: err.message || 'An unexpected error occurred' };
        this.orderFailed.emit(errorResponse);
      }
    });
  }

  /** Whether the submit button should be disabled */
  protected isFormInvalid(): boolean {
    if (this.selectedCustomerId() === null) return true;
    const currentItems = this.items();
    if (currentItems.length === 0) return true;
    return currentItems.some(item => item.productId === null || item.quantity < 1);
  }

  private updateItem(index: number, patch: Partial<OrderItemRow>): void {
    this.items.update(current =>
      current.map((item, i) => i === index ? { ...item, ...patch } : item)
    );
  }

  private resetForm(): void {
    this.selectedCustomerId.set(null);
    this.items.set([{ productId: null, quantity: 1 }]);
  }
}
