import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, signal, viewChild } from '@angular/core';
import { forkJoin, interval, of, Subscription } from 'rxjs';
import { catchError, filter, map, switchMap, take, timeout } from 'rxjs/operators';
import { InventoryApiService } from './services/inventory-api.service';
import { PaymentApiService } from './services/payment-api.service';
import { OrderApiService } from './services/order-api.service';
import { Customer } from './models/customer.model';
import { Product } from './models/product.model';
import { PlaceOrderRequest, PlaceOrderResponse, ErrorResponse } from './models/order.model';
import { SagaExecutionResponse } from './models/saga.model';
import { SnapshotState } from './models/snapshot-state.model';
import { InventoryPanel } from './components/inventory-panel/inventory-panel';
import { PaymentPanel } from './components/payment-panel/payment-panel';
import { OrderPanel } from './components/order-panel/order-panel';
import { SagaExecutionPanel } from './components/saga-execution-panel/saga-execution-panel';
import { OrderForm } from './components/order-form/order-form';
import { TransactionResult } from './components/transaction-result/transaction-result';
import { OrderHistory } from './components/order-history/order-history';

@Component({
  selector: 'app-root',
  imports: [InventoryPanel, PaymentPanel, OrderPanel, SagaExecutionPanel, OrderForm, TransactionResult, OrderHistory],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent implements OnInit, OnDestroy {

  private readonly inventoryApi = inject(InventoryApiService);
  private readonly paymentApi = inject(PaymentApiService);
  private readonly orderApi = inject(OrderApiService);

  inventoryPanel = viewChild.required(InventoryPanel);
  paymentPanel = viewChild.required(PaymentPanel);
  orderPanel = viewChild.required(OrderPanel);
  sagaPanel = viewChild.required(SagaExecutionPanel);
  transactionResult = viewChild.required(TransactionResult);
  orderHistory = viewChild.required(OrderHistory);

  private pollingSubscription?: Subscription;
  private cachedCustomers = signal<Customer[]>([]);
  private cachedProducts = signal<Product[]>([]);

  protected resultType = signal<'success' | 'error' | 'timeout' | null>(null);
  protected beforeState = signal<SnapshotState | null>(null);
  protected afterState = signal<SnapshotState | null>(null);
  protected sagaExecution = signal<SagaExecutionResponse | undefined>(undefined);
  protected currentOrderId = signal<number | undefined>(undefined);
  protected failureReason = signal<string | undefined>(undefined);
  protected polling = signal(false);
  protected resetting = signal(false);

  ngOnInit(): void {
    this.refreshCache();
  }

  ngOnDestroy(): void {
    this.pollingSubscription?.unsubscribe();
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

  onBeforeSubmit(_request: PlaceOrderRequest): void {
    this.beforeState.set({
      customers: [...this.cachedCustomers()],
      products: [...this.cachedProducts()]
    });
  }

  onOrderSubmitted(response: PlaceOrderResponse): void {
    this.pollingSubscription?.unsubscribe();
    this.currentOrderId.set(response.orderId);
    this.polling.set(true);

    // Refresh order panel to show PENDING order
    this.orderPanel().refresh();
    this.orderHistory().refresh();

    // Poll for saga terminal status, then fetch fresh state
    this.pollingSubscription = interval(500).pipe(
      switchMap(() => this.orderApi.getSagaExecution(response.sagaId).pipe(
        catchError(() => of(null))
      )),
      filter((saga): saga is SagaExecutionResponse =>
        saga !== null && ['COMPLETED', 'COMPENSATED', 'FAILED'].includes(saga.status)
      ),
      take(1),
      timeout(10000),
      switchMap(saga => forkJoin({
        customers: this.paymentApi.getCustomers(),
        products: this.inventoryApi.getProducts()
      }).pipe(map(state => ({ saga, ...state }))))
    ).subscribe({
      next: ({ saga, customers, products }) => {
        this.polling.set(false);
        this.sagaExecution.set(saga);
        this.failureReason.set(saga.failureReason ?? undefined);
        this.afterState.set({ customers, products });

        if (saga.status === 'COMPLETED') {
          this.resultType.set('success');
        } else {
          this.resultType.set('error');
        }

        this.refreshAllPanels();
        this.refreshCache();
      },
      error: () => {
        // Timeout
        this.polling.set(false);
        this.resultType.set('timeout');
        this.refreshAllPanels();
        this.refreshCache();
      }
    });
  }

  onOrderError(error: ErrorResponse): void {
    this.pollingSubscription?.unsubscribe();
    this.polling.set(false);
    this.resultType.set('error');
    this.failureReason.set(error.message);
    this.sagaExecution.set(undefined);
    this.beforeState.set(null);
    this.refreshAllPanels();
  }

  onResetDatabase(): void {
    this.pollingSubscription?.unsubscribe();
    this.resetting.set(true);

    this.orderApi.resetAllDatabases().subscribe({
      next: () => {
        this.resultType.set(null);
        this.beforeState.set(null);
        this.afterState.set(null);
        this.sagaExecution.set(undefined);
        this.currentOrderId.set(undefined);
        this.failureReason.set(undefined);
        this.transactionResult().clear();

        this.refreshAllPanels();
        this.refreshCache();
        this.resetting.set(false);
      },
      error: () => {
        this.resetting.set(false);
      }
    });
  }

  private refreshAllPanels(): void {
    this.inventoryPanel().refresh();
    this.paymentPanel().refresh();
    this.orderPanel().refresh();
    this.sagaPanel().refresh();
    this.orderHistory().refresh();
  }
}
