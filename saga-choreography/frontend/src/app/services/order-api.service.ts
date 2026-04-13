import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlaceOrderRequest, PlaceOrderResponse, OrderResponse } from '../models/order.model';
import { SagaExecutionResponse } from '../models/saga.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OrderApiService {

  private readonly baseUrl = environment.orderApiUrl;

  constructor(private readonly http: HttpClient) {}

  placeOrder(request: PlaceOrderRequest): Observable<PlaceOrderResponse> {
    return this.http.post<PlaceOrderResponse>(`${this.baseUrl}/orders`, request);
  }

  getOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.baseUrl}/orders`);
  }

  getSagaExecutions(): Observable<SagaExecutionResponse[]> {
    return this.http.get<SagaExecutionResponse[]>(`${this.baseUrl}/orders/sagas`);
  }

  getSagaExecution(sagaId: number): Observable<SagaExecutionResponse> {
    return this.http.get<SagaExecutionResponse>(`${this.baseUrl}/orders/sagas/${sagaId}`);
  }

  resetAllDatabases(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/demo/reset`, {});
  }
}
