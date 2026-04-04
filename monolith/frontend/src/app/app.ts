import { ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ApiService } from './services/api.service';
import { Customer } from './models/customer.model';
import { Product } from './models/product.model';
import { CreateOrderRequest, ErrorResponse, OrderResponse } from './models/order.model';
import { CustomerPanel } from './components/customer-panel/customer-panel';
import { ProductPanel } from './components/product-panel/product-panel';
import { OrderForm } from './components/order-form/order-form';
import { TransactionResult, SnapshotState } from './components/transaction-result/transaction-result';
import { OrderHistory } from './components/order-history/order-history';

@Component({
  selector: 'app-root',
  imports: [CustomerPanel, ProductPanel, OrderForm, TransactionResult, OrderHistory],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class App implements OnInit {

  private readonly api = inject(ApiService);

  /** Child component references */
  customerPanel = viewChild.required(CustomerPanel);
  productPanel = viewChild.required(ProductPanel);
  transactionResult = viewChild.required(TransactionResult);
  orderHistory = viewChild.required(OrderHistory);

  /** Cached data used for "before" snapshots */
  private cachedCustomers = signal<Customer[]>([]);
  private cachedProducts = signal<Product[]>([]);

  /** Transaction result state — all driven by signals for OnPush */
  protected resultType = signal<'success' | 'error' | null>(null);
  protected orderResponse = signal<OrderResponse | undefined>(undefined);
  protected errorResponse = signal<ErrorResponse | undefined>(undefined);
  protected beforeState = signal<SnapshotState | null>(null);
  protected afterState = signal<SnapshotState | null>(null);

  /** Tracks the customerId from the most recent form submission */
  private pendingCustomerId = signal<number | null>(null);

  /** Whether the DB reset is in progress */
  protected resetting = signal(false);

  ngOnInit(): void {
    this.refreshCache();
  }

  /** Fetches all customers and products into the local cache */
  private refreshCache(): void {
    forkJoin({
      customers: this.api.getCustomers(),
      products: this.api.getProducts()
    }).subscribe(({ customers, products }) => {
      this.cachedCustomers.set(customers);
      this.cachedProducts.set(products);
    });
  }

  /** Captures the customerId before the API call fires */
  onBeforeSubmit(request: CreateOrderRequest): void {
    this.pendingCustomerId.set(request.customerId);
  }

  /** Handles a successful order placement */
  onOrderPlaced(response: OrderResponse): void {
    const customerId = response.customerId;
    const beforeCustomer = this.cachedCustomers().find(c => c.id === customerId);
    const beforeProducts = [...this.cachedProducts()];

    if (beforeCustomer) {
      this.beforeState.set({ customer: { ...beforeCustomer }, products: beforeProducts });
    }

    // Fetch fresh data for the "after" snapshot, then refresh all panels
    forkJoin({
      customer: this.api.getCustomerById(customerId),
      products: this.api.getProducts()
    }).subscribe(({ customer, products }) => {
      this.afterState.set({ customer, products });
      this.resultType.set('success');
      this.orderResponse.set(response);
      this.errorResponse.set(undefined);

      this.customerPanel().refresh();
      this.productPanel().refresh();
      this.orderHistory().refresh();
      this.refreshCache();
      this.pendingCustomerId.set(null);
    });
  }

  /** Handles a failed order placement */
  onOrderFailed(error: ErrorResponse): void {
    const customerId = this.pendingCustomerId();
    const beforeCustomer = customerId
      ? this.cachedCustomers().find(c => c.id === customerId)
      : undefined;
    const beforeProducts = [...this.cachedProducts()];

    if (beforeCustomer) {
      this.beforeState.set({ customer: { ...beforeCustomer }, products: beforeProducts });
    }

    // Fetch fresh data — on failure nothing should have changed, but we verify
    const customerFetch = customerId
      ? this.api.getCustomerById(customerId)
      : undefined;

    if (customerFetch) {
      forkJoin({
        customer: customerFetch,
        products: this.api.getProducts()
      }).subscribe(({ customer, products }) => {
        this.afterState.set({ customer, products });
        this.resultType.set('error');
        this.errorResponse.set(error);
        this.orderResponse.set(undefined);

        this.customerPanel().refresh();
        this.productPanel().refresh();
        this.refreshCache();
        this.pendingCustomerId.set(null);
      });
    } else {
      // No customer context — show error without before/after comparison
      this.resultType.set('error');
      this.errorResponse.set(error);
      this.orderResponse.set(undefined);
      this.beforeState.set(null);
      this.afterState.set(null);
    }
  }

  /** Resets the database and refreshes all panels */
  onResetDatabase(): void {
    this.resetting.set(true);

    this.api.resetDatabase().subscribe({
      next: () => {
        this.resultType.set(null);
        this.orderResponse.set(undefined);
        this.errorResponse.set(undefined);
        this.beforeState.set(null);
        this.afterState.set(null);
        this.transactionResult().clear();

        this.customerPanel().refresh();
        this.productPanel().refresh();
        this.orderHistory().refresh();
        this.refreshCache();
        this.resetting.set(false);
      },
      error: () => {
        this.resetting.set(false);
      }
    });
  }
}
