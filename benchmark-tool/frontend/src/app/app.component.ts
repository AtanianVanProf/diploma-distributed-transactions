import { Component } from '@angular/core';
import { BenchmarkPanelComponent } from './components/benchmark-panel/benchmark-panel.component';
import { ComparisonChartsComponent } from './components/comparison-charts/comparison-charts.component';
import { ResultsTableComponent } from './components/results-table/results-table.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [BenchmarkPanelComponent, ComparisonChartsComponent, ResultsTableComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
}
