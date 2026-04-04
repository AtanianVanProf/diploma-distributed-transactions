import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Customer } from '../models/customer.model';
import { Product } from '../models/product.model';
import { CreateOrderRequest, OrderResponse } from '../models/order.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  getCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.baseUrl}/customers`);
  }

  getCustomerById(id: number): Observable<Customer> {
    return this.http.get<Customer>(`${this.baseUrl}/customers/${id}`);
  }

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.baseUrl}/products`);
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/products/${id}`);
  }

  placeOrder(request: CreateOrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.baseUrl}/orders`, request);
  }

  getOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.baseUrl}/orders`);
  }

  getOrderById(id: number): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.baseUrl}/orders/${id}`);
  }

  resetDatabase(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/demo/reset`, {});
  }
}
