import { Component, input } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { Chart, registerables, ChartConfiguration, ChartData } from 'chart.js';
import {
  BenchmarkResults,
  IMPLEMENTATION_COLORS,
  IMPLEMENTATION_LABELS,
  MetricType,
  METRIC_LABELS,
  SCENARIO_LABELS
} from '../../models/benchmark.models';

Chart.register(...registerables);

@Component({
  selector: 'app-comparison-charts',
  standalone: true,
  imports: [NgChartsModule],
  templateUrl: './comparison-charts.component.html',
  styleUrl: './comparison-charts.component.scss'
})
export class ComparisonChartsComponent {
  results = input<BenchmarkResults | null>(null);

  metrics: MetricType[] = ['asyncMessages', 'failureDetectionTime', 'compensations', 'dbWritesOnFailure'];

  chartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          color: '#cdd6f4',
          font: { size: 12, weight: 'bold' },
          padding: 16
        }
      }
    },
    scales: {
      x: {
        ticks: { color: '#a6adc8', font: { size: 11 } },
        grid: { color: 'rgba(69, 71, 90, 0.5)' }
      },
      y: {
        beginAtZero: true,
        ticks: { color: '#a6adc8', font: { size: 11 } },
        grid: { color: 'rgba(69, 71, 90, 0.5)' }
      }
    }
  };

  getChartData(metric: MetricType): ChartData<'bar'> {
    const data = this.results();
    if (!data) {
      return { labels: [], datasets: [] };
    }

    const metricData = data[metric];
    const scenarios = Object.keys(metricData);
    const labels = scenarios.map(s => SCENARIO_LABELS[s] || s);

    return {
      labels,
      datasets: [
        {
          label: IMPLEMENTATION_LABELS['orchestrator'],
          data: scenarios.map(s => metricData[s]?.orchestrator ?? 0),
          backgroundColor: IMPLEMENTATION_COLORS['orchestrator'],
          borderRadius: 4
        },
        {
          label: IMPLEMENTATION_LABELS['choreography'],
          data: scenarios.map(s => metricData[s]?.choreography ?? 0),
          backgroundColor: IMPLEMENTATION_COLORS['choreography'],
          borderRadius: 4
        },
        {
          label: IMPLEMENTATION_LABELS['pvip'],
          data: scenarios.map(s => metricData[s]?.pvip ?? 0),
          backgroundColor: IMPLEMENTATION_COLORS['pvip'],
          borderRadius: 4
        }
      ]
    };
  }

  getMetricLabel(metric: MetricType): string {
    return METRIC_LABELS[metric];
  }

  getMetricDescription(metric: MetricType): string {
    const descriptions: Record<MetricType, string> = {
      asyncMessages: 'Kafka messages exchanged between services (orchestrator uses sync HTTP = 0)',
      failureDetectionTime: 'Time from request to error detection — lower is better',
      compensations: 'Rollback operations needed to restore consistency — lower is better',
      dbWritesOnFailure: 'Database write operations when transaction fails — lower is better'
    };
    return descriptions[metric];
  }
}
