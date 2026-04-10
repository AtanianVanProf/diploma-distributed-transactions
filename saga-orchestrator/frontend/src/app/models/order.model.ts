export interface OrderItemRequest {
  productId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  customerId: number;
  items: OrderItemRequest[];
}

export interface OrderItemResponse {
  productId: number;
  productName: string;
  quantity: number;
  priceAtPurchase: number;
}

export interface OrderResponse {
  orderId: number;
  customerId: number;
  status: string;
  totalAmount: number;
  failureReason?: string;
  items: OrderItemResponse[];
  createdAt: string;
}

export interface ErrorResponse {
  error: string;
  message: string;
  details?: Record<string, any>;
}
