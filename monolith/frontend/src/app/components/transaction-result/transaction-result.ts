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

  /** null = hidden, 'success' = green panel, 'error' = red panel */
  result = input<'success' | 'error' | null>(null);

  orderResponse = input<OrderResponse | undefined>(undefined);
  errorResponse = input<ErrorResponse | undefined>(undefined);
  beforeState = input<SnapshotState | null>(null);
  afterState = input<SnapshotState | null>(null);

  /** Tracks whether the panel is visible — allows parent to clear via method call */
  protected readonly cleared = signal(false);

  constructor() {
    // Reset cleared flag when a new result arrives
    effect(() => {
      if (this.result() !== null) {
        this.cleared.set(false);
      }
    });
  }

  /** Parent calls this on DB reset to hide the result panel */
  clear(): void {
    this.cleared.set(true);
  }

  /** Reset cleared flag when a new result comes in (tracked via template) */
  protected isVisible(): boolean {
    if (this.cleared()) return false;
    return this.result() !== null;
  }

  /**
   * Returns the list of products to compare in the before/after table.
   * On success: only products that were part of the order.
   * On failure: all products from the beforeState snapshot.
   */
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
