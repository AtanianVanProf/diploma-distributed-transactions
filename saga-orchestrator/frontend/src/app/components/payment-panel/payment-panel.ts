import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { PaymentApiService } from '../../services/payment-api.service';
import { Customer } from '../../models/customer.model';

@Component({
  selector: 'app-payment-panel',
  imports: [CurrencyPipe],
  templateUrl: './payment-panel.html',
  styleUrl: './payment-panel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PaymentPanel implements OnInit {

  private readonly api = inject(PaymentApiService);

  protected readonly customers = signal<Customer[]>([]);
  protected readonly loading = signal(false);

  private previousBalances = new Map<number, number>();
  protected readonly changedIds = signal<Set<number>>(new Set());

  ngOnInit(): void {
    this.fetchCustomers(false);
  }

  refresh(): void {
    this.fetchCustomers(true);
  }

  private fetchCustomers(detectChanges: boolean): void {
    this.loading.set(true);

    this.api.getCustomers().subscribe({
      next: (customers) => {
        if (detectChanges) {
          const changed = new Set<number>();
          for (const customer of customers) {
            const prev = this.previousBalances.get(customer.id);
            if (prev !== undefined && customer.balance !== prev) {
              changed.add(customer.id);
            }
          }
          this.changedIds.set(changed);

          if (changed.size > 0) {
            setTimeout(() => this.changedIds.set(new Set()), 1500);
          }
        }

        this.previousBalances = new Map(
          customers.map(c => [c.id, c.balance])
        );

        this.customers.set(customers);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }
}
