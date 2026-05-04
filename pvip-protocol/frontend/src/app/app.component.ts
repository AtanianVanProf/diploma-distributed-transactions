import { Component, inject, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin, interval, switchMap, takeWhile, tap } from 'rxjs';
import { InventoryApiService } from './services/inventory-api.service';
import { PaymentApiService } from './services/payment-api.service';
import { OrderApiService } from './services/order-api.service';
import { Product } from './models/product.model';
import { Customer } from './models/customer.model';
import { Order } from './models/order.model';
import { ProtocolExecution } from './models/protocol.model';
import { PlaceOrderRequest, PlaceOrderResponse } from './models/place-order.model';
import { SnapshotState } from './models/snapshot-state.model';
import { TransactionResult } from './models/transaction-result.model';
import { InventoryPanelComponent } from './components/inventory-panel/inventory-panel.component';
import { PaymentPanelComponent } from './components/payment-panel/payment-panel.component';
import { OrderPanelComponent } from './components/order-panel/order-panel.component';
import { ProtocolPanelComponent } from './components/protocol-panel/protocol-panel.component';
import { OrderFormComponent } from './components/order-form/order-form.component';
import { TransactionResultComponent } from './components/transaction-result/transaction-result.component';
import { StateDiffComponent } from './components/state-diff/state-diff.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    InventoryPanelComponent,
    PaymentPanelComponent,
    OrderPanelComponent,
    ProtocolPanelComponent,
    OrderFormComponent,
    TransactionResultComponent,
    StateDiffComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  private inventoryApi = inject(InventoryApiService);
  private paymentApi = inject(PaymentApiService);
  private orderApi = inject(OrderApiService);

  products: Product[] = [];
  customers: Customer[] = [];
  orders: Order[] = [];
  executions: ProtocolExecution[] = [];

  isProcessing = false;
  transactionResult: TransactionResult | null = null;
  beforeState: SnapshotState | null = null;

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.inventoryApi.getProducts().subscribe(p => this.products = p);
    this.paymentApi.getCustomers().subscribe(c => this.customers = c);
    this.orderApi.getOrders().subscribe(o => this.orders = o);
    this.orderApi.getProtocolExecutions().subscribe(e => this.executions = e);
  }

  onOrderSubmit(request: PlaceOrderRequest): void {
    this.isProcessing = true;
    this.transactionResult = null;

    this.beforeState = {
      products: this.products.map(p => ({ ...p })),
      customers: this.customers.map(c => ({ ...c }))
    };

    this.orderApi.placeOrder(request).subscribe({
      next: (response: PlaceOrderResponse) => {
        if (response.status === 'FAILED') {
          this.handleImmediateResult(response);
        } else {
          this.startPolling(response);
        }
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 409 && err.error) {
          this.handleImmediateResult(err.error as PlaceOrderResponse);
        } else {
          this.isProcessing = false;
        }
      }
    });
  }

  private handleImmediateResult(response: PlaceOrderResponse): void {
    this.transactionResult = {
      response,
      execution: null,
      beforeState: this.beforeState!,
      afterState: this.beforeState
    };
    this.isProcessing = false;
    this.loadData();
  }

  private startPolling(response: PlaceOrderResponse): void {
    this.transactionResult = {
      response,
      execution: null,
      beforeState: this.beforeState!,
      afterState: null
    };

    interval(500).pipe(
      switchMap(() => this.orderApi.getProtocolExecution(response.transactionId)),
      tap(execution => {
        this.transactionResult = {
          ...this.transactionResult!,
          execution
        };
      }),
      takeWhile(execution => execution.status !== 'COMMITTED' && execution.status !== 'REJECTED', true)
    ).subscribe({
      next: (execution) => {
        if (execution.status === 'COMMITTED' || execution.status === 'REJECTED') {
          this.finalizeResult(execution);
        }
      }
    });
  }

  private finalizeResult(execution: ProtocolExecution): void {
    forkJoin({
      products: this.inventoryApi.getProducts(),
      customers: this.paymentApi.getCustomers()
    }).subscribe(afterState => {
      this.transactionResult = {
        ...this.transactionResult!,
        execution,
        afterState
      };
      this.isProcessing = false;
      this.loadData();
    });
  }

  onReset(): void {
    this.orderApi.resetAll().subscribe(() => {
      this.transactionResult = null;
      this.beforeState = null;
      this.loadData();
    });
  }
}
