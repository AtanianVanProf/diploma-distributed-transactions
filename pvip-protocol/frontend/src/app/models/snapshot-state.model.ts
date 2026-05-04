import { Product } from './product.model';
import { Customer } from './customer.model';

export interface SnapshotState {
  products: Product[];
  customers: Customer[];
}
