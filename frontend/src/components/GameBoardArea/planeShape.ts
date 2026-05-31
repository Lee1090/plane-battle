import type { Cell, Direction, PlanePartType } from '../../types/game';

export interface DraftPlane {
  id: string;
  head: Cell | null;
  direction: Direction;
}

export interface DraftPart {
  planeId: string;
  type: PlanePartType;
  row: number;
  col: number;
}

export const PLANE_IDS = ['P1', 'P2', 'P3'];
export const DIRECTIONS: Direction[] = ['UP', 'RIGHT', 'DOWN', 'LEFT'];
export const BOARD_SIZE = 10;

const UP_SHAPE: Array<{ type: PlanePartType; row: number; col: number }> = [
  { type: 'HEAD', row: 0, col: 0 },
  { type: 'WING', row: 1, col: -2 },
  { type: 'WING', row: 1, col: -1 },
  { type: 'WING', row: 1, col: 0 },
  { type: 'WING', row: 1, col: 1 },
  { type: 'WING', row: 1, col: 2 },
  { type: 'BODY', row: 2, col: 0 },
  { type: 'TAIL', row: 3, col: -1 },
  { type: 'TAIL', row: 3, col: 0 },
  { type: 'TAIL', row: 3, col: 1 },
];

export function buildDraftParts(plane: DraftPlane): DraftPart[] {
  if (!plane.head) {
    return [];
  }
  const head = plane.head;
  return UP_SHAPE.map((shapePart) => {
    const [rowOffset, colOffset] = rotate(shapePart.row, shapePart.col, plane.direction);
    return {
      planeId: plane.id,
      type: shapePart.type,
      row: head.row + rowOffset,
      col: head.col + colOffset,
    };
  });
}

export function createPlaneDragImage(planeParts: DraftPart[], cellSize: number) {
  if (planeParts.length === 0 || cellSize <= 0) {
    return null;
  }

  const minRow = Math.min(...planeParts.map((part) => part.row));
  const maxRow = Math.max(...planeParts.map((part) => part.row));
  const minCol = Math.min(...planeParts.map((part) => part.col));
  const maxCol = Math.max(...planeParts.map((part) => part.col));
  const head = planeParts.find((part) => part.type === 'HEAD');

  if (!head) {
    return null;
  }

  const element = document.createElement('div');
  element.id = `plane-drag-${head.planeId}-${Date.now()}`;
  element.className = 'planeDragImage';
  element.style.gridTemplateColumns = `repeat(${maxCol - minCol + 1}, ${cellSize}px)`;
  element.style.gridTemplateRows = `repeat(${maxRow - minRow + 1}, ${cellSize}px)`;

  for (let row = minRow; row <= maxRow; row += 1) {
    for (let col = minCol; col <= maxCol; col += 1) {
      const planePart = planeParts.find((part) => part.row === row && part.col === col);
      const cell = document.createElement('div');
      cell.className = [
        'planeDragCell',
        planePart ? `part${planePart.type}` : '',
        planePart ? `plane${planePart.planeId}` : '',
      ]
        .filter(Boolean)
        .join(' ');
      cell.textContent = planePart?.type === 'HEAD' ? planePart.planeId : '';
      element.appendChild(cell);
    }
  }

  return {
    element,
    offsetX: (head.col - minCol + 0.5) * cellSize,
    offsetY: (head.row - minRow + 0.5) * cellSize,
  };
}

function rotate(rowOffset: number, colOffset: number, direction: Direction): [number, number] {
  switch (direction) {
    case 'UP':
      return [rowOffset, colOffset];
    case 'DOWN':
      return [-rowOffset, -colOffset];
    case 'LEFT':
      return [colOffset, rowOffset];
    case 'RIGHT':
      return [-colOffset, -rowOffset];
  }
}
