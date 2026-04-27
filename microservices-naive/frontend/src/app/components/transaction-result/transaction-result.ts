import { ChangeDetectionStrategy, Component, effect, input, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Customer } from '../../models/customer.model';
import { Product } from '../../models/product.model';
import { ErrorResponse, OrderResponse } from '../../models/order.model';
import { SnapshotState } from '../../models/snapshot-state.model';

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

  protected getCustomerBefore(): Customer | undefined {
    const order = this.orderResponse();
    const before = this.beforeState();
    if (!before) return undefined;
    if (order) {
      return before.customers.find(c => c.id === order.customerId);
    }
    return before.customers.find(c => c.id === this.getOrderCustomerId()) ?? before.customers[0];
  }

  protected getCustomerAfter(): Customer | undefined {
    const order = this.orderResponse();
    const after = this.afterState();
    if (!after) return undefined;
    if (order) {
      return after.customers.find(c => c.id === order.customerId);
    }
    return after.customers.find(c => c.id === this.getOrderCustomerId()) ?? after.customers[0];
  }

  private getOrderCustomerId(): number | undefined {
    return this.orderResponse()?.customerId;
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

  protected hasInconsistency(): boolean {
    if (this.result() !== 'error') return false;

    for (const productId of this.getComparisonProductIds()) {
      if (this.hasStockChanged(productId)) return true;
    }

    if (this.hasBalanceChanged()) return true;

    return false;
  }

  protected getInconsistencies(): { field: string; before: string; after: string; service: string }[] {
    const inconsistencies: { field: string; before: string; after: string; service: string }[] = [];

    for (const productId of this.getComparisonProductIds()) {
      if (this.hasStockChanged(productId)) {
        const before = this.getProductBefore(productId);
        const after = this.getProductAfter(productId);
        inconsistencies.push({
          field: `${before?.name ?? 'Product #' + productId} — Stock`,
          before: String(before?.stock ?? '—'),
          after: String(after?.stock ?? '—'),
          service: 'Inventory Service'
        });
      }
    }

    if (this.hasBalanceChanged()) {
      const before = this.getCustomerBefore();
      const after = this.getCustomerAfter();
      inconsistencies.push({
        field: `${before?.name ?? 'Customer'} — Balance`,
        before: `$${before?.balance?.toFixed(2) ?? '—'}`,
        after: `$${after?.balance?.toFixed(2) ?? '—'}`,
        service: 'Payment Service'
      });
    }

    return inconsistencies;
  }
}
