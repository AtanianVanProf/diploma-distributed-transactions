import { ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild } from '@angular/core';
import { forkJoin } from 'rxjs';
import { InventoryApiService } from './services/inventory-api.service';
import { PaymentApiService } from './services/payment-api.service';
import { OrderApiService } from './services/order-api.service';
import { Customer } from './models/customer.model';
import { Product } from './models/product.model';
import { CreateOrderRequest, ErrorResponse, OrderResponse } from './models/order.model';
import { SnapshotState } from './models/snapshot-state.model';
import { InventoryPanel } from './components/inventory-panel/inventory-panel';
import { PaymentPanel } from './components/payment-panel/payment-panel';
import { OrderPanel } from './components/order-panel/order-panel';
import { OrderForm } from './components/order-form/order-form';
import { TransactionResult } from './components/transaction-result/transaction-result';
import { InconsistencyAlert, InconsistencyRecord } from './components/inconsistency-alert/inconsistency-alert';

@Component({
  selector: 'app-root',
  imports: [InventoryPanel, PaymentPanel, OrderPanel, OrderForm, TransactionResult, InconsistencyAlert],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class App implements OnInit {

  private readonly inventoryApi = inject(InventoryApiService);
  private readonly paymentApi = inject(PaymentApiService);
  private readonly orderApi = inject(OrderApiService);

  inventoryPanel = viewChild.required(InventoryPanel);
  paymentPanel = viewChild.required(PaymentPanel);
  orderPanel = viewChild.required(OrderPanel);
  transactionResult = viewChild.required(TransactionResult);

  private cachedCustomers = signal<Customer[]>([]);
  private cachedProducts = signal<Product[]>([]);

  protected resultType = signal<'success' | 'error' | null>(null);
  protected orderResponse = signal<OrderResponse | undefined>(undefined);
  protected errorResponse = signal<ErrorResponse | undefined>(undefined);
  protected beforeState = signal<SnapshotState | null>(null);
  protected afterState = signal<SnapshotState | null>(null);

  protected inconsistencyRecords = signal<InconsistencyRecord[]>([]);

  private pendingCustomerId = signal<number | null>(null);

  protected resetting = signal(false);

  ngOnInit(): void {
    this.refreshCache();
  }

  private refreshCache(): void {
    forkJoin({
      customers: this.paymentApi.getCustomers(),
      products: this.inventoryApi.getProducts()
    }).subscribe(({ customers, products }) => {
      this.cachedCustomers.set(customers);
      this.cachedProducts.set(products);
    });
  }

  onBeforeSubmit(request: CreateOrderRequest): void {
    this.pendingCustomerId.set(request.customerId);
  }

  onOrderPlaced(response: OrderResponse): void {
    if (response.status === 'FAILED') {
      const errorResponse: ErrorResponse = {
        error: 'ORDER_FAILED',
        message: response.failureReason ?? 'Order failed'
      };
      this.orderResponse.set(response);
      this.handleOrderFailure(errorResponse);
      return;
    }

    const beforeSnapshot: SnapshotState = {
      customers: [...this.cachedCustomers()],
      products: [...this.cachedProducts()]
    };
    this.beforeState.set(beforeSnapshot);

    forkJoin({
      customers: this.paymentApi.getCustomers(),
      products: this.inventoryApi.getProducts()
    }).subscribe(({ customers, products }) => {
      this.afterState.set({ customers, products });
      this.resultType.set('success');
      this.orderResponse.set(response);
      this.errorResponse.set(undefined);

      this.inventoryPanel().refresh();
      this.paymentPanel().refresh();
      this.orderPanel().refresh();
      this.refreshCache();
      this.pendingCustomerId.set(null);
    });
  }

  onOrderFailed(error: ErrorResponse): void {
    this.orderResponse.set(undefined);
    this.handleOrderFailure(error);
  }

  private handleOrderFailure(error: ErrorResponse): void {
    const beforeSnapshot: SnapshotState = {
      customers: [...this.cachedCustomers()],
      products: [...this.cachedProducts()]
    };
    this.beforeState.set(beforeSnapshot);

    forkJoin({
      customers: this.paymentApi.getCustomers(),
      products: this.inventoryApi.getProducts()
    }).subscribe(({ customers, products }) => {
      this.afterState.set({ customers, products });
      this.resultType.set('error');
      this.errorResponse.set(error);

      this.detectInconsistency(beforeSnapshot, customers, products);

      this.inventoryPanel().refresh();
      this.paymentPanel().refresh();
      this.orderPanel().refresh();
      this.refreshCache();
      this.pendingCustomerId.set(null);
    });
  }

  private detectInconsistency(beforeSnapshot: SnapshotState, freshCustomers: Customer[], freshProducts: Product[]): void {
    const changedItems: { field: string; before: string; after: string; service: string }[] = [];

    for (const beforeProduct of beforeSnapshot.products) {
      const afterProduct = freshProducts.find(p => p.id === beforeProduct.id);
      if (afterProduct && afterProduct.stock !== beforeProduct.stock) {
        changedItems.push({
          field: `${beforeProduct.name} — Stock`,
          before: String(beforeProduct.stock),
          after: String(afterProduct.stock),
          service: 'Inventory Service'
        });
      }
    }

    const customerId = this.pendingCustomerId();
    if (customerId) {
      const beforeCustomer = beforeSnapshot.customers.find(c => c.id === customerId);
      const afterCustomer = freshCustomers.find(c => c.id === customerId);
      if (beforeCustomer && afterCustomer && beforeCustomer.balance !== afterCustomer.balance) {
        changedItems.push({
          field: `${beforeCustomer.name} — Balance`,
          before: `$${beforeCustomer.balance.toFixed(2)}`,
          after: `$${afterCustomer.balance.toFixed(2)}`,
          service: 'Payment Service'
        });
      }
    }

    if (changedItems.length > 0) {
      this.inconsistencyRecords.update(current => [...current, { items: changedItems }]);
    }
  }

  onResetDatabase(): void {
    this.resetting.set(true);

    this.orderApi.resetDatabase().subscribe({
      next: () => {
        this.resultType.set(null);
        this.orderResponse.set(undefined);
        this.errorResponse.set(undefined);
        this.beforeState.set(null);
        this.afterState.set(null);
        this.inconsistencyRecords.set([]);
        this.transactionResult().clear();

        this.inventoryPanel().refresh();
        this.paymentPanel().refresh();
        this.orderPanel().refresh();
        this.refreshCache();
        this.resetting.set(false);
      },
      error: () => {
        this.resetting.set(false);
      }
    });
  }
}
