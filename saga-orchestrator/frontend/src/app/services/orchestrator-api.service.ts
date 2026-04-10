import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateOrderRequest, OrderResponse } from '../models/order.model';
import { SagaExecutionResponse } from '../models/saga.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OrchestratorApiService {

  private readonly baseUrl = environment.orchestratorApiUrl;

  constructor(private readonly http: HttpClient) {}

  placeOrder(request: CreateOrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.baseUrl}/orchestrator/orders`, request);
  }

  getSagaExecutions(): Observable<SagaExecutionResponse[]> {
    return this.http.get<SagaExecutionResponse[]>(`${this.baseUrl}/orchestrator/sagas`);
  }

  resetAllDatabases(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/orchestrator/reset`, {});
  }
}
