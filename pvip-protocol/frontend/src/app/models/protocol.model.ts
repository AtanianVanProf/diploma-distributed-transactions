export interface ProtocolExecution {
  transactionId: string;
  orderId: number;
  status: string;
  phase: string;
  kafkaMessagesSent: number;
  compensationsTriggered: number;
  preValidationPassed: boolean | null;
  intentResponsesReceived: number;
  decision: string | null;
  decisionReason: string | null;
  startedAt: string;
  completedAt: string | null;
}
