import { ChangeDetectionStrategy, Component, effect, input, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Customer } from '../../models/customer.model';
import { Product } from '../../models/product.model';
import { ErrorResponse, OrderResponse } from '../../models/order.model';

export interface SnapshotState {
  customer: Customer;
  products: Product[];
}

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

  protected getComparisonProductIds(): number[] {
    if (this.result() === 'success') {
      const response = this.orderResponse();
      return response ? response.items.map(item => item.productId) : [];
    }

    const before = this.beforeState();
    return before ? before.products.map(p => p.id) : [];
  }

  protected getProductBefore(productId: number): Product | undefined {
    return this.beforeState()?.products.find(p => p.id === productId);
  }

  protected getProductAfter(productId: number): Product | undefined {
    return this.afterState()?.products.find(p => p.id === productId);
  }

  protected hasBalanceChanged(): boolean {
    const before = this.beforeState()?.customer.balance;
    const after = this.afterState()?.customer.balance;
    return before !== undefined && after !== undefined && before !== after;
  }

  protected hasStockChanged(productId: number): boolean {
    const before = this.getProductBefore(productId)?.stock;
    const after = this.getProductAfter(productId)?.stock;
    return before !== undefined && after !== undefined && before !== after;
  }

  protected balanceDiff(): number {
    const before = this.beforeState()?.customer.balance ?? 0;
    const after = this.afterState()?.customer.balance ?? 0;
    return after - before;
  }

  protected stockDiff(productId: number): number {
    const before = this.getProductBefore(productId)?.stock ?? 0;
    const after = this.getProductAfter(productId)?.stock ?? 0;
    return after - before;
  }
}
