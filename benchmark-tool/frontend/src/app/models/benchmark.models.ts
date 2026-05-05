export type BenchmarkStatus = 'IDLE' | 'RUNNING' | 'COMPLETED' | 'FAILED';

export type Scenario = 'happy_path' | 'stock_failure' | 'balance_failure';

export type Implementation = 'orchestrator' | 'choreography' | 'pvip';

export type MetricType = 'asyncMessages' | 'failureDetectionTime' | 'compensations' | 'dbWritesOnFailure';

export interface ScenarioMetrics {
  orchestrator: number;
  choreography: number;
  pvip: number;
}

export interface MetricGroup {
  [scenario: string]: ScenarioMetrics;
}

export interface BenchmarkResults {
  asyncMessages: MetricGroup;
  failureDetectionTime: MetricGroup;
  compensations: MetricGroup;
  dbWritesOnFailure: MetricGroup;
}

export interface BenchmarkRun {
  runId: string;
  status: BenchmarkStatus;
  startedAt: string;
  completedAt?: string;
  results?: BenchmarkResults;
}

export interface BenchmarkStatusResponse {
  status: BenchmarkStatus;
}

export interface StartBenchmarkResponse {
  runId: string;
  status: BenchmarkStatus;
  startedAt: string;
}

export const SCENARIO_LABELS: Record<string, string> = {
  happy_path: 'Happy Path',
  stock_failure: 'Stock Failure',
  balance_failure: 'Balance Failure'
};

export const IMPLEMENTATION_LABELS: Record<Implementation, string> = {
  orchestrator: 'Orchestrator',
  choreography: 'Choreography',
  pvip: 'PVIP'
};

export const IMPLEMENTATION_COLORS: Record<Implementation, string> = {
  orchestrator: '#4285F4',
  choreography: '#34A853',
  pvip: '#FBBC04'
};

export const METRIC_LABELS: Record<MetricType, string> = {
  asyncMessages: 'Async Messages per Transaction',
  failureDetectionTime: 'Failure Detection Time (ms)',
  compensations: 'Compensating Transactions',
  dbWritesOnFailure: 'DB Writes on Failure'
};
