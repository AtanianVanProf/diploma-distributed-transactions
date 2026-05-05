import { Component, input } from '@angular/core';
import {
  BenchmarkResults,
  Implementation,
  IMPLEMENTATION_LABELS,
  MetricType,
  METRIC_LABELS,
  SCENARIO_LABELS
} from '../../models/benchmark.models';

@Component({
  selector: 'app-results-table',
  standalone: true,
  templateUrl: './results-table.component.html',
  styleUrl: './results-table.component.scss'
})
export class ResultsTableComponent {
  results = input<BenchmarkResults | null>(null);

  metrics: MetricType[] = ['asyncMessages', 'failureDetectionTime', 'compensations', 'dbWritesOnFailure'];
  implementations: Implementation[] = ['orchestrator', 'choreography', 'pvip'];

  getMetricLabel(metric: MetricType): string {
    return METRIC_LABELS[metric];
  }

  getScenarioLabel(scenario: string): string {
    return SCENARIO_LABELS[scenario] || scenario;
  }

  getImplementationLabel(impl: Implementation): string {
    return IMPLEMENTATION_LABELS[impl];
  }

  getScenariosForMetric(metric: MetricType): string[] {
    const data = this.results();
    if (!data) return [];
    return Object.keys(data[metric]);
  }

  getValue(metric: MetricType, scenario: string, impl: Implementation): number {
    const data = this.results();
    if (!data || !data[metric][scenario]) return 0;
    return data[metric][scenario][impl];
  }

  getImplClass(impl: Implementation): string {
    return `impl-${impl}`;
  }
}
