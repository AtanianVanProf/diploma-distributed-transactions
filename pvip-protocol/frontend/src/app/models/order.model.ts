export interface Order {
  id: number;
  transactionId: string;
  customerId: number;
  customerName: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
  totalAmount: number;
  failureReason: string | null;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[];
}

export interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  price: number;
}
