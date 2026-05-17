export type GameStatus = 'WAITING' | 'DEPLOYING' | 'PLAYING' | 'FINISHED';

export type PlayerRole = 'PLAYER_A' | 'PLAYER_B' | 'SPECTATOR';

export type PlayerSide = 'A' | 'B';

export type Direction = 'UP' | 'DOWN' | 'LEFT' | 'RIGHT';

export type PlanePartType = 'HEAD' | 'WING' | 'BODY' | 'TAIL';

export type AttackResult = 'MISS' | 'HIT_PLANE' | 'HIT_HEAD';

export interface Cell {
  row: number;
  col: number;
}

export interface PlanePart {
  type: PlanePartType;
  row: number;
  col: number;
  hit: boolean;
}

export interface Plane {
  id: string;
  head: Cell;
  direction: Direction;
  parts: PlanePart[];
}

export interface AttackRecord {
  row: number;
  col: number;
  result: AttackResult;
}

export interface PlayerBoard {
  owner: PlayerSide;
  planes: Plane[];
  receivedAttacks: AttackRecord[];
}

export interface GameState {
  status: GameStatus;
  currentTurn: PlayerSide | null;
  winner: PlayerSide | null;
  playerABoard: PlayerBoard | null;
  playerBBoard: PlayerBoard | null;
  playerASeated: boolean;
  playerBSeated: boolean;
  playerAReady: boolean;
  playerBReady: boolean;
}

export interface ClientView {
  role: PlayerRole;
  side: PlayerSide | null;
  gameState: GameState;
}

export type ClientMessageType = 'JOIN' | 'SIT_DOWN' | 'STAND_UP' | 'SUBMIT_DEPLOYMENT' | 'ATTACK' | 'RESET';

export interface ClientMessage<T = unknown> {
  type: ClientMessageType;
  data?: T;
}

export type ServerMessageType = 'CONNECTED' | 'STATE_UPDATE' | 'ATTACK_RESULT' | 'ERROR';

export interface ServerMessage<T = unknown> {
  type: ServerMessageType;
  data?: T;
  error?: string;
}
