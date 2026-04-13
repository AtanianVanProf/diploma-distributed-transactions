export interface SagaStepResponse {
  id: number;
  stepName: string;
  stepOrder: number;
  status: string;
  completedAt: string | null;
}

export interface SagaExecutionResponse {
  id: number;
  orderId: number | null;
  status: string;
  requestPayload: string;
  failureReason: string | null;
  steps: SagaStepResponse[];
  createdAt: string;
  updatedAt: string;
}
