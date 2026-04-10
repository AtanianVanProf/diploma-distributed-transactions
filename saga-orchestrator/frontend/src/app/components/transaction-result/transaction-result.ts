import { ChangeDetectionStrategy, Component, effect, input, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Customer } from '../../models/customer.model';
import { Product } from '../../models/product.model';
import { ErrorResponse, OrderResponse } from '../../models/order.model';
import { SnapshotState } from '../../models/snapshot-state.model';
import { SagaExecutionResponse } from '../../models/saga.model';

@Component({
  selector: 'app-transaction-result',
  imports: [CurrencyPipe],
  templateUrl: './transaction-result.html',
  styleUrl: './transaction-result.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionResult {

  result = input<'success' | 'error' | null>(null);
  orderResponse = input<OrderResponse | undefined>(undefined);
  errorResponse = input<ErrorResponse | undefined>(undefined);
  beforeState = input<SnapshotState | null>(null);
  afterState = input<SnapshotState | null>(null);
  sagaExecution = input<SagaExecutionResponse | undefined>(undefined);

  protected readonly cleared = signal(false);

  constructor() {
    effect(() => {
      if (this.result() !== null) {
        this.cleared.set(false);
      }
    });
  }

  clear(): void {
    this.cleared.set(true);
  }

  protected isVisible(): boolean {
    if (this.cleared()) return false;
    return this.result() !== null;
  }

  protected isCompensated(): boolean {
    return this.sagaExecution()?.status === 'COMPENSATED';
  }

  protected isFailedNoCompensation(): boolean {
    return this.sagaExecution()?.status === 'FAILED';
  }

  protected getComparisonProductIds(): number[] {
    const order = this.orderResponse();
    if (order && order.items.length > 0) {
      return order.items.map(item => item.productId);
    }
    const before = this.beforeState();
    return before ? before.products.map(p => p.id) : [];
  }

  protected getCustomerBefore(): Customer | undefined {
    const order = this.orderResponse();
    const before = this.beforeState();
    if (!before) return undefined;
    if (order) {
      return before.customers.find(c => c.id === order.customerId);
    }
    return before.customers[0];
  }

  protected getCustomerAfter(): Customer | undefined {
    const order = this.orderResponse();
    const after = this.afterState();
    if (!after) return undefined;
    if (order) {
      return after.customers.find(c => c.id === order.customerId);
    }
    return after.customers[0];
  }

  protected getProductBefore(productId: number): Product | undefined {
    return this.beforeState()?.products.find(p => p.id === productId);
  }

  protected getProductAfter(productId: number): Product | undefined {
    return this.afterState()?.products.find(p => p.id === productId);
  }

  protected hasBalanceChanged(): boolean {
    const before = this.getCustomerBefore()?.balance;
    const after = this.getCustomerAfter()?.balance;
    return before !== undefined && after !== undefined && before !== after;
  }

  protected hasStockChanged(productId: number): boolean {
    const before = this.getProductBefore(productId)?.stock;
    const after = this.getProductAfter(productId)?.stock;
    return before !== undefined && after !== undefined && before !== after;
  }

  protected balanceDiff(): number {
    const before = this.getCustomerBefore()?.balance ?? 0;
    const after = this.getCustomerAfter()?.balance ?? 0;
    return after - before;
  }

  protected stockDiff(productId: number): number {
    const before = this.getProductBefore(productId)?.stock ?? 0;
    const after = this.getProductAfter(productId)?.stock ?? 0;
    return after - before;
  }
}
