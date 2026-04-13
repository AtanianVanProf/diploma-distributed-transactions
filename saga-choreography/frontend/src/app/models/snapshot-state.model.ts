import { Customer } from './customer.model';
import { Product } from './product.model';

export interface SnapshotState {
  customers: Customer[];
  products: Product[];
}
