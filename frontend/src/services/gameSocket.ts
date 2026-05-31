import type { ClientMessage, PlaneDeploymentRequest, PlayerSide } from '../types/game';

export function createGameSocket() {
  const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8090/ws/game';
  return new WebSocket(wsUrl);
}

export function joinMessage(clientId: string): ClientMessage<{ clientId: string }> {
  return { type: 'JOIN', data: { clientId } };
}

export function sitDownMessage(side: PlayerSide): ClientMessage<{ side: PlayerSide }> {
  return { type: 'SIT_DOWN', data: { side } };
}

export function standUpMessage(): ClientMessage {
  return { type: 'STAND_UP' };
}

export function submitDeploymentMessage(
  planes: PlaneDeploymentRequest[],
): ClientMessage<{ planes: PlaneDeploymentRequest[] }> {
  return { type: 'SUBMIT_DEPLOYMENT', data: { planes } };
}
