import { Component, input } from '@angular/core';
import { ProtocolExecution } from '../../models/protocol.model';

@Component({
  selector: 'app-protocol-panel',
  standalone: true,
  templateUrl: './protocol-panel.component.html',
  styleUrl: './protocol-panel.component.scss'
})
export class ProtocolPanelComponent {
  executions = input<ProtocolExecution[]>([]);

  getPhaseClass(execution: ProtocolExecution, phase: string): string {
    const phases = ['PRE_VALIDATION', 'COLLECTING_INTENTS', 'DECISION'];
    const currentIndex = phases.indexOf(execution.phase);
    const phaseIndex = phases.indexOf(phase);

    if (execution.status === 'COMMITTED' || execution.status === 'REJECTED') {
      if (phaseIndex <= currentIndex) {
        return execution.status === 'COMMITTED' ? 'phase-complete' : 'phase-failed';
      }
      return 'phase-skipped';
    }

    if (phaseIndex < currentIndex) return 'phase-complete';
    if (phaseIndex === currentIndex) return 'phase-active';
    return 'phase-pending';
  }

  getDuration(execution: ProtocolExecution): string {
    if (!execution.completedAt || !execution.startedAt) return '-';
    const start = new Date(execution.startedAt).getTime();
    const end = new Date(execution.completedAt).getTime();
    const ms = end - start;
    return ms < 1000 ? `${ms}ms` : `${(ms / 1000).toFixed(1)}s`;
  }

  truncateId(id: string): string {
    return id.substring(0, 8);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'COMMITTED': return 'status-committed';
      case 'REJECTED': return 'status-rejected';
      default: return 'status-in-progress';
    }
  }
}
