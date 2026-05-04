import { Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Customer } from '../../models/customer.model';

@Component({
  selector: 'app-payment-panel',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './payment-panel.component.html',
  styleUrl: './payment-panel.component.scss'
})
export class PaymentPanelComponent {
  customers = input<Customer[]>([]);

  getBalanceClass(balance: number): string {
    if (balance >= 1000) return 'balance-high';
    if (balance >= 100) return 'balance-medium';
    return 'balance-low';
  }
}
