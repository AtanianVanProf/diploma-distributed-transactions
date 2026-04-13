export interface PlaceOrderRequest {
  customerId: number;
  items: OrderItemRequest[];
}

export interface OrderItemRequest {
  productId: number;
  quantity: number;
}

export interface PlaceOrderResponse {
  orderId: number;
  sagaId: number;
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
  totalAmount: number | null;
  failureReason: string | null;
  items: OrderItemResponse[];
  createdAt: string;
}

export interface ErrorResponse {
  error: string;
  message: string;
  details: Record<string, any>;
}
