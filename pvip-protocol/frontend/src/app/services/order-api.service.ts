import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order } from '../models/order.model';
import { PlaceOrderRequest, PlaceOrderResponse } from '../models/place-order.model';
import { ProtocolExecution } from '../models/protocol.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class OrderApiService {
  private http = inject(HttpClient);
  private baseUrl = environment.orderApiUrl;

  placeOrder(request: PlaceOrderRequest): Observable<PlaceOrderResponse> {
    return this.http.post<PlaceOrderResponse>(`${this.baseUrl}/api/orders`, request);
  }

  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.baseUrl}/api/orders`);
  }

  getOrder(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.baseUrl}/api/orders/${id}`);
  }

  getProtocolExecutions(): Observable<ProtocolExecution[]> {
    return this.http.get<ProtocolExecution[]>(`${this.baseUrl}/api/orders/protocol`);
  }

  getProtocolExecution(transactionId: string): Observable<ProtocolExecution> {
    return this.http.get<ProtocolExecution>(`${this.baseUrl}/api/orders/protocol/${transactionId}`);
  }

  resetAll(): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/demo/reset`, {});
  }
}
