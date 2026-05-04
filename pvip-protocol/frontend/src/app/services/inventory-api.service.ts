import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class InventoryApiService {
  private http = inject(HttpClient);
  private baseUrl = environment.inventoryApiUrl;

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.baseUrl}/api/products`);
  }

  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/api/products/${id}`);
  }

  pauseKafka(): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/demo/pause-kafka`, {});
  }

  resumeKafka(): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/demo/resume-kafka`, {});
  }
}
