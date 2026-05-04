import { PlaceOrderResponse } from './place-order.model';
import { ProtocolExecution } from './protocol.model';
import { SnapshotState } from './snapshot-state.model';

export interface TransactionResult {
  response: PlaceOrderResponse;
  execution: ProtocolExecution | null;
  beforeState: SnapshotState;
  afterState: SnapshotState | null;
}
