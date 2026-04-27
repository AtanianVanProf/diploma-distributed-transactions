import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe, NgClass } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-panel',
  imports: [CurrencyPipe, NgClass],
  templateUrl: './product-panel.html',
  styleUrl: './product-panel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductPanel implements OnInit {

  private readonly api = inject(ApiService);

  protected readonly products = signal<Product[]>([]);
  protected readonly loading = signal(false);

  private previousStocks = new Map<number, number>();

  protected readonly changedIds = signal<Set<number>>(new Set());

  ngOnInit(): void {
    this.fetchProducts(false);
  }

  refresh(): void {
    this.fetchProducts(true);
  }

  protected stockClass(stock: number): string {
    if (stock === 0) return 'stock-out';
    if (stock <= 3) return 'stock-low';
    return 'stock-high';
  }

  private fetchProducts(detectChanges: boolean): void {
    this.loading.set(true);

    this.api.getProducts().subscribe({
      next: (products) => {
        if (detectChanges) {
          const changed = new Set<number>();
          for (const product of products) {
            const prev = this.previousStocks.get(product.id);
            if (prev !== undefined && product.stock < prev) {
              changed.add(product.id);
            }
          }
          this.changedIds.set(changed);

          if (changed.size > 0) {
            setTimeout(() => this.changedIds.set(new Set()), 1500);
          }
        }

        this.previousStocks = new Map(
          products.map(p => [p.id, p.stock])
        );

        this.products.set(products);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }
}
