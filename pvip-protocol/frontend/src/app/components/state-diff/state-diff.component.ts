import { Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { SnapshotState } from '../../models/snapshot-state.model';

@Component({
  selector: 'app-state-diff',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './state-diff.component.html',
  styleUrl: './state-diff.component.scss'
})
export class StateDiffComponent {
  before = input<SnapshotState | null>(null);
  after = input<SnapshotState | null>(null);

  hasChanges(): boolean {
    const b = this.before();
    const a = this.after();
    if (!b || !a) return false;
    return b.products.some((p, i) => p.stock !== a.products[i]?.stock) ||
           b.customers.some((c, i) => c.balance !== a.customers[i]?.balance);
  }

  getStockChange(productId: number): { before: number; after: number; changed: boolean } | null {
    const b = this.before();
    const a = this.after();
    if (!b || !a) return null;
    const bp = b.products.find(p => p.id === productId);
    const ap = a.products.find(p => p.id === productId);
    if (!bp || !ap) return null;
    return { before: bp.stock, after: ap.stock, changed: bp.stock !== ap.stock };
  }

  getBalanceChange(customerId: number): { before: number; after: number; changed: boolean } | null {
    const b = this.before();
    const a = this.after();
    if (!b || !a) return null;
    const bc = b.customers.find(c => c.id === customerId);
    const ac = a.customers.find(c => c.id === customerId);
    if (!bc || !ac) return null;
    return { before: bc.balance, after: ac.balance, changed: bc.balance !== ac.balance };
  }
}
