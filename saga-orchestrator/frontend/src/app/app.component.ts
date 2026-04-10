import { ChangeDetectionStrategy, Component, inject, OnInit, signal, viewChild } from '@angular/core';
import { forkJoin } from 'rxjs';
import { InventoryApiService } from './services/inventory-api.service';
import { PaymentApiService } from './services/payment-api.service';
import { OrchestratorApiService } from './services/orchestrator-api.service';
import { Customer } from './models/customer.model';
import { Product } from './models/product.model';
import { CreateOrderRequest, ErrorResponse, OrderResponse } from './models/order.model';
import { SagaExecutionResponse } from './models/saga.model';
import { SnapshotState } from './models/snapshot-state.model';
import { InventoryPanel } from './components/inventory-panel/inventory-panel';
import { PaymentPanel } from './components/payment-panel/payment-panel';
import { OrderPanel } from './components/order-panel/order-panel';
import { SagaExecutionPanel } from './components/saga-execution-panel/saga-execution-panel';
import { OrderForm } from './components/order-form/order-form';
import { TransactionResult } from './components/transaction-result/transaction-result';

@Component({
  selector: 'app-root',
  imports: [InventoryPanel, PaymentPanel, OrderPanel, SagaExecutionPanel, OrderForm, TransactionResult],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent implements OnInit {

  private readonly inventoryApi = inject(InventoryApiService);
  private readonly paymentApi = inject(PaymentApiService);
  private readonly orchestratorApi = inject(OrchestratorApiService);

  inventoryPanel = viewChild.required(InventoryPanel);
  paymentPanel = viewChild.required(PaymentPanel);
  orderPanel = viewChild.required(OrderPanel);
  sagaPanel = viewChild.required(SagaExecutionPanel);
  transactionResult = viewChild.required(TransactionResult);

  private cachedCustomers = signal<Customer[]>([]);
  private cachedProducts = signal<Product[]>([]);

  protected resultType = signal<'success' | 'error' | null>(null);
  protected orderResponse = signal<OrderResponse | undefined>(undefined);
  protected errorResponse = signal<ErrorResponse | undefined>(undefined);
  protected beforeState = signal<SnapshotState | null>(null);
  protected afterState = signal<SnapshotState | null>(null);
  protected sagaExecution = signal<SagaExecutionResponse | undefined>(undefined);
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

  onBeforeSubmit(_request: CreateOrderRequest): void {
    this.beforeState.set({
      customers: [...this.cachedCustomers()],
      products: [...this.cachedProducts()]
    });
  }

  onOrderPlaced(response: OrderResponse): void {
    forkJoin({
      customers: this.paymentApi.getCustomers(),
      products: this.inventoryApi.getProducts(),
      sagas: this.orchestratorApi.getSagaExecutions()
    }).subscribe(({ customers, products, sagas }) => {
      this.afterState.set({ customers, products });
      this.sagaExecution.set(sagas.length > 0 ? sagas[sagas.length - 1] : undefined);
      this.resultType.set('success');
      this.orderResponse.set(response);
      this.errorResponse.set(undefined);

      this.refreshAllPanels();
      this.refreshCache();
    });
  }

  onOrderFailed(event: { order?: OrderResponse; error?: ErrorResponse }): void {
    forkJoin({
      customers: this.paymentApi.getCustomers(),
      products: this.inventoryApi.getProducts(),
      sagas: this.orchestratorApi.getSagaExecutions()
    }).subscribe(({ customers, products, sagas }) => {
      this.afterState.set({ customers, products });
      this.sagaExecution.set(sagas.length > 0 ? sagas[sagas.length - 1] : undefined);
      this.resultType.set('error');
      this.orderResponse.set(event.order);
      this.errorResponse.set(event.error);

      this.refreshAllPanels();
      this.refreshCache();
    });
  }

  onResetDatabase(): void {
    this.resetting.set(true);

    this.orchestratorApi.resetAllDatabases().subscribe({
      next: () => {
        this.resultType.set(null);
        this.orderResponse.set(undefined);
        this.errorResponse.set(undefined);
        this.beforeState.set(null);
        this.afterState.set(null);
        this.sagaExecution.set(undefined);
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
  }
}
