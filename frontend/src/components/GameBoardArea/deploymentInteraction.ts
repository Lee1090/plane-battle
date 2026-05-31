import type { MessageKey } from '../../i18n/messages';
import type { Direction } from '../../types/game';
import { BOARD_SIZE, type DraftPart, type DraftPlane } from './planeShape';

export const deploymentInteraction = {
  emptyCellClick: 'place-next-plane',
  arrowKeyMode: 'body-extension-direction',
  dragPreview: 'whole-plane',
  selectPlaneOnHeadClick: true,
} as const;

export function directionFromArrowKey(key: string): Direction | null {
  const directionByKey: Partial<Record<string, Direction>> = {
    ArrowLeft: 'RIGHT',
    ArrowRight: 'LEFT',
    ArrowUp: 'DOWN',
    ArrowDown: 'UP',
  };
  return directionByKey[key] ?? null;
}

export function findNextUndeployedPlane(draftPlanes: DraftPlane[]) {
  return draftPlanes.find((plane) => !plane.head) ?? null;
}

export function validateDraftPlacement(draftPlanes: DraftPlane[], draftParts: DraftPart[]) {
  if (draftPlanes.some((plane) => !plane.head)) {
    return { canSubmit: false, messageKey: 'deploymentIncomplete' as MessageKey };
  }

  const occupiedCells = new Set<string>();
  for (const part of draftParts) {
    if (part.row < 0 || part.row >= BOARD_SIZE || part.col < 0 || part.col >= BOARD_SIZE) {
      return { canSubmit: false, messageKey: 'deploymentOutOfBoard' as MessageKey };
    }

    const cellKey = `${part.row}:${part.col}`;
    if (occupiedCells.has(cellKey)) {
      return { canSubmit: false, messageKey: 'deploymentOverlap' as MessageKey };
    }
    occupiedCells.add(cellKey);
  }

  return { canSubmit: true, messageKey: 'deploymentReady' as MessageKey };
}

export function isPlacementWarning(messageKey: MessageKey) {
  return messageKey === 'deploymentOutOfBoard' || messageKey === 'deploymentOverlap';
}

export function shouldIgnoreDirectionShortcut(target: EventTarget | null) {
  if (!(target instanceof HTMLElement)) {
    return false;
  }
  if (target.classList.contains('boardCell')) {
    return false;
  }

  return target.matches('button, input, select, textarea, [contenteditable="true"]');
}
