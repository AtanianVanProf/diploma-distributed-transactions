export interface TransactionRegistry {
  transactionId: string;
  status: string;
  expectedParticipants: number;
  receivedResponses: number;
  decision: string | null;
  decisionReason: string | null;
  createdAt: string;
  decidedAt: string | null;
  participants: ParticipantResponse[];
}

export interface ParticipantResponse {
  participantType: string;
  status: string;
  reason: string | null;
  receivedAt: string;
}
