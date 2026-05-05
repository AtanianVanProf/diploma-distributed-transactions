import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  BenchmarkRun,
  BenchmarkStatusResponse,
  StartBenchmarkResponse
} from '../models/benchmark.models';

@Injectable({
  providedIn: 'root'
})
export class BenchmarkService {
  private readonly baseUrl = `${environment.apiUrl}/api/benchmark`;

  constructor(private http: HttpClient) {}

  startBenchmark(): Observable<StartBenchmarkResponse> {
    return this.http.post<StartBenchmarkResponse>(`${this.baseUrl}/run`, {});
  }

  getStatus(): Observable<BenchmarkStatusResponse> {
    return this.http.get<BenchmarkStatusResponse>(`${this.baseUrl}/status`);
  }

  getRun(runId: string): Observable<BenchmarkRun> {
    return this.http.get<BenchmarkRun>(`${this.baseUrl}/runs/${runId}`);
  }

  getAllRuns(): Observable<BenchmarkRun[]> {
    return this.http.get<BenchmarkRun[]>(`${this.baseUrl}/runs`);
  }

  reset(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/reset`, {});
  }
}
