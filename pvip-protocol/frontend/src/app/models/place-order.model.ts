export interface PlaceOrderRequest {
  customerId: number;
  items: { productId: number; quantity: number }[];
}

export interface PlaceOrderResponse {
  orderId: number;
  transactionId: string;
  status: string;
  phase: string;
  kafkaMessages: number;
  compensations: number;
  reason: string | null;
  totalAmount: number | null;
}
