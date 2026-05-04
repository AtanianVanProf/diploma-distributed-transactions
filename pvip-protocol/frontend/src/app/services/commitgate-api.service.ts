import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TransactionRegistry } from '../models/transaction.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CommitgateApiService {
  private http = inject(HttpClient);
  private baseUrl = environment.commitgateApiUrl;

  getTransactions(): Observable<TransactionRegistry[]> {
    return this.http.get<TransactionRegistry[]>(`${this.baseUrl}/api/transactions`);
  }

  getTransaction(transactionId: string): Observable<TransactionRegistry> {
    return this.http.get<TransactionRegistry>(`${this.baseUrl}/api/transactions/${transactionId}`);
  }
}
