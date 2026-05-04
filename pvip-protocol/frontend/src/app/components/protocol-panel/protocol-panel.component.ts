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
    if (execution.status === 'COMMITTED') {
      return 'phase-complete';
    }

    if (execution.status === 'REJECTED') {
      const phases = ['PRE_VALIDATION', 'COLLECTING_INTENTS', 'DECISION'];
      const rejectPhase = this.mapPhase(execution.phase);
      const rejectIndex = phases.indexOf(rejectPhase);
      const phaseIndex = phases.indexOf(phase);

      if (phaseIndex < rejectIndex) return 'phase-complete';
      if (phaseIndex === rejectIndex) return 'phase-failed';
      return 'phase-skipped';
    }

    const phases = ['PRE_VALIDATION', 'COLLECTING_INTENTS', 'DECISION'];
    const currentIndex = phases.indexOf(this.mapPhase(execution.phase));
    const phaseIndex = phases.indexOf(phase);

    if (phaseIndex < currentIndex) return 'phase-complete';
    if (phaseIndex === currentIndex) return 'phase-active';
    return 'phase-pending';
  }

  private mapPhase(phase: string): string {
    if (phase === 'COMMITTED' || phase === 'REJECTED') return 'DECISION';
    return phase;
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
