import { Component, input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-inventory-panel',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './inventory-panel.component.html',
  styleUrl: './inventory-panel.component.scss'
})
export class InventoryPanelComponent {
  products = input<Product[]>([]);
}
