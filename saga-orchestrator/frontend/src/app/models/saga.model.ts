export interface SagaStepResponse {
  id: number;
  stepName: string;
  stepOrder: number;
  status: string;
  requestData: string;
  responseData: string | null;
  compensationData: string | null;
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
