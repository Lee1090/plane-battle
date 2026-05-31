import { useEffect, useMemo, useState } from 'react';
import { DeploymentPanel } from '../DeploymentPanel/DeploymentPanel';
import type { MessageKey } from '../../i18n/messages';
import {
  directionFromArrowKey,
  findNextUndeployedPlane,
  shouldIgnoreDirectionShortcut,
  validateDraftPlacement,
} from './deploymentInteraction';
import {
  clearDeploymentDraft,
  loadDeploymentDraft,
  saveDeploymentDraft,
} from './deploymentDraftStorage';
import {
  BOARD_SIZE,
  DIRECTIONS,
  PLANE_IDS,
  buildDraftParts,
  createPlaneDragImage,
  type DraftPart,
  type DraftPlane,
} from './planeShape';
import type {
  AttackResult,
  Cell,
  ClientView,
  Direction,
  PlaneDeploymentRequest,
  PlayerBoard,
  PlayerSide,
} from '../../types/game';

interface GameBoardAreaProps {
  clientView: ClientView | null;
  canSit: boolean;
  canStandUp: boolean;
  onSitDown: (side: PlayerSide) => void;
  onStandUp: () => void;
  onSubmitDeployment: (planes: PlaneDeploymentRequest[]) => void;
  onAttack: (row: number, col: number) => void;
  translate: (key: MessageKey) => string;
}

interface BoardPanelModel {
  side: PlayerSide;
  title: string;
  subtitle: string;
  board: PlayerBoard | null;
  editable: boolean;
  hidden: boolean;
  ready: boolean;
  seated: boolean;
  showAttackLegend: boolean;
}

export function GameBoardArea({
  clientView,
  canSit,
  canStandUp,
  onSitDown,
  onStandUp,
  onSubmitDeployment,
  onAttack,
  translate,
}: GameBoardAreaProps) {
  const storedDraft = useMemo(loadDeploymentDraft, []);
  const [selectedPlaneId, setSelectedPlaneId] = useState(storedDraft?.selectedPlaneId ?? PLANE_IDS[0]);
  const [draftPlanes, setDraftPlanes] = useState<DraftPlane[]>(() =>
    storedDraft?.draftPlanes ?? PLANE_IDS.map((id) => ({ id, head: null, direction: 'UP' })),
  );

  const draftParts = useMemo(() => draftPlanes.flatMap(buildDraftParts), [draftPlanes]);
  const validation = validateDraftPlacement(draftPlanes, draftParts);
  const selectedPlane = draftPlanes.find((plane) => plane.id === selectedPlaneId) ?? draftPlanes[0];
  const gameState = clientView?.gameState;
  const isDeploying = gameState?.status === 'DEPLOYING';
  const isPlaying = gameState?.status === 'PLAYING';
  const isFinished = gameState?.status === 'FINISHED';
  const isPlayer = clientView?.role === 'PLAYER_A' || clientView?.role === 'PLAYER_B';
  const ownSide = clientView?.side;
  const ownReady =
    ownSide === 'A' ? Boolean(gameState?.playerAReady) : ownSide === 'B' ? Boolean(gameState?.playerBReady) : false;
  const boards = buildBoardModels(clientView, translate);

  useEffect(() => {
    if (isDeploying && isPlayer && !ownReady) {
      saveDeploymentDraft({ selectedPlaneId, draftPlanes });
      return;
    }
    if (gameState) {
      clearDeploymentDraft();
    }
  }, [draftPlanes, gameState, isDeploying, isPlayer, ownReady, selectedPlaneId]);

  useEffect(() => {
    if (!isDeploying || !isPlayer || ownReady) {
      return undefined;
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (shouldIgnoreDirectionShortcut(event.target)) {
        return;
      }

      const direction = directionFromArrowKey(event.key);
      if (!direction) {
        return;
      }

      event.preventDefault();
      updateSelectedPlane({ direction });
    }

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isDeploying, isPlayer, ownReady, selectedPlaneId]);

  function updateSelectedPlane(update: Partial<DraftPlane>) {
    setDraftPlanes((planes) =>
      planes.map((plane) => (plane.id === selectedPlaneId ? { ...plane, ...update } : plane)),
    );
  }

  function placeNextPlaneHead(row: number, col: number) {
    if (!isDeploying || !isPlayer || ownReady) {
      return;
    }

    const nextPlane = findNextUndeployedPlane(draftPlanes);
    if (!nextPlane) {
      return;
    }

    setSelectedPlaneId(nextPlane.id);
    setDraftPlanes((planes) =>
      planes.map((plane) => (plane.id === nextPlane.id ? { ...plane, head: { row, col } } : plane)),
    );
  }

  function movePlaneHead(planeId: string, row: number, col: number) {
    if (!isDeploying || !isPlayer || ownReady) {
      return;
    }

    setSelectedPlaneId(planeId);
    setDraftPlanes((planes) =>
      planes.map((plane) => (plane.id === planeId ? { ...plane, head: { row, col } } : plane)),
    );
  }

  function submitDeployment() {
    if (!validation.canSubmit || ownReady) {
      return;
    }
    clearDeploymentDraft();
    onSubmitDeployment(
      draftPlanes.map((plane) => ({
        id: plane.id,
        head: plane.head as Cell,
        direction: plane.direction,
      })),
    );
  }

  return (
    <section className="gameBoardArea" aria-label={translate('boards')}>
      <div className="boardSlots">
        {boards.map((board) => (
          <BoardSlot
            key={`${board.side}-${board.title}`}
            model={board}
            draftParts={board.editable && !ownReady ? draftParts : []}
            selectedHead={board.editable ? selectedPlane.head : null}
            onCellClick={board.editable ? placeNextPlaneHead : canAttackBoard(board, clientView) ? onAttack : undefined}
            onHeadDrop={board.editable ? movePlaneHead : undefined}
            onHeadSelect={board.editable ? setSelectedPlaneId : undefined}
            canSit={canSit}
            canStandUp={canStandUp && ownSide === board.side}
            isCurrentPlayerSide={ownSide === board.side}
            isDeploying={Boolean(isDeploying)}
            isPlaying={Boolean(isPlaying)}
            isFinished={Boolean(isFinished)}
            isReady={ownSide === board.side ? ownReady : board.ready}
            currentTurn={gameState?.currentTurn ?? null}
            winner={gameState?.winner ?? null}
            ownSide={ownSide ?? null}
            selectedPlaneId={selectedPlaneId}
            selectedDirection={selectedPlane.direction}
            canSubmit={validation.canSubmit}
            messageKey={validation.messageKey}
            onSitDown={onSitDown}
            onStandUp={onStandUp}
            onSelectPlane={setSelectedPlaneId}
            onSelectDirection={(direction) => updateSelectedPlane({ direction })}
            onSubmit={submitDeployment}
            translate={translate}
          />
        ))}
      </div>
    </section>
  );
}

function BoardSlot({
  model,
  draftParts,
  selectedHead,
  onCellClick,
  onHeadDrop,
  onHeadSelect,
  canSit,
  canStandUp,
  isCurrentPlayerSide,
  isDeploying,
  isPlaying,
  isFinished,
  isReady,
  currentTurn,
  winner,
  ownSide,
  selectedPlaneId,
  selectedDirection,
  canSubmit,
  messageKey,
  onSitDown,
  onStandUp,
  onSelectPlane,
  onSelectDirection,
  onSubmit,
  translate,
}: {
  model: BoardPanelModel;
  draftParts: DraftPart[];
  selectedHead: Cell | null;
  onCellClick?: (row: number, col: number) => void;
  onHeadDrop?: (planeId: string, row: number, col: number) => void;
  onHeadSelect?: (planeId: string) => void;
  canSit: boolean;
  canStandUp: boolean;
  isCurrentPlayerSide: boolean;
  isDeploying: boolean;
  isPlaying: boolean;
  isFinished: boolean;
  isReady: boolean;
  currentTurn: PlayerSide | null;
  winner: PlayerSide | null;
  ownSide: PlayerSide | null;
  selectedPlaneId: string;
  selectedDirection: Direction;
  canSubmit: boolean;
  messageKey: MessageKey;
  onSitDown: (side: PlayerSide) => void;
  onStandUp: () => void;
  onSelectPlane: (planeId: string) => void;
  onSelectDirection: (direction: Direction) => void;
  onSubmit: () => void;
  translate: (key: MessageKey) => string;
}) {
  return (
    <div className="boardSlot">
      <BoardPanel
        model={model}
        draftParts={draftParts}
        selectedHead={selectedHead}
        onCellClick={onCellClick}
        onHeadDrop={onHeadDrop}
        onHeadSelect={onHeadSelect}
        translate={translate}
      />
      <SeatAction
        model={model}
        canSit={canSit}
        canStandUp={canStandUp}
        onSitDown={onSitDown}
        onStandUp={onStandUp}
        translate={translate}
      />
      <SideControlPanel
        model={model}
        isCurrentPlayerSide={isCurrentPlayerSide}
        isDeploying={isDeploying}
        isPlaying={isPlaying}
        isFinished={isFinished}
        isReady={isReady}
        currentTurn={currentTurn}
        winner={winner}
        ownSide={ownSide}
        selectedPlaneId={selectedPlaneId}
        selectedDirection={selectedDirection}
        canSubmit={canSubmit}
        messageKey={messageKey}
        onSelectPlane={onSelectPlane}
        onSelectDirection={onSelectDirection}
        onSubmit={onSubmit}
        translate={translate}
      />
    </div>
  );
}

function BoardPanel({
  model,
  draftParts,
  selectedHead,
  onCellClick,
  onHeadDrop,
  onHeadSelect,
  translate,
}: {
  model: BoardPanelModel;
  draftParts: DraftPart[];
  selectedHead: Cell | null;
  onCellClick?: (row: number, col: number) => void;
  onHeadDrop?: (planeId: string, row: number, col: number) => void;
  onHeadSelect?: (planeId: string) => void;
  translate: (key: MessageKey) => string;
}) {
  const boardParts = model.board?.planes.flatMap((plane) =>
    plane.parts.map((part) => ({ ...part, planeId: plane.id })),
  );
  const parts = draftParts.length > 0 ? draftParts : boardParts ?? [];
  const attacks = model.board?.receivedAttacks ?? [];

  return (
    <section className="boardPanel" aria-label={model.title}>
      <div className="boardHeader">
        <div>
          <h2>{model.title}</h2>
          <p>{model.subtitle}</p>
        </div>
        {model.ready ? <span className="readyBadge">{translate('ready')}</span> : null}
      </div>

      <div className={`boardGridWrap ${model.showAttackLegend ? 'boardGridWrapWithLegend' : ''}`}>
        <div className="boardGrid">
          {Array.from({ length: BOARD_SIZE * BOARD_SIZE }, (_, index) => {
            const row = Math.floor(index / BOARD_SIZE);
            const col = index % BOARD_SIZE;
            const cellParts = model.hidden ? [] : parts.filter((part) => part.row === row && part.col === col);
            const attack = attacks.find((record) => record.row === row && record.col === col);
            const hasOverlap = cellParts.length > 1;
            const part = cellParts.find((cellPart) => cellPart.type === 'HEAD') ?? cellParts[0];
            const isSelectedHead = selectedHead?.row === row && selectedHead?.col === col;
            const canDragHead = Boolean(onHeadDrop && part?.type === 'HEAD');
            const disabled = !onCellClick || Boolean(attack);
            const className = [
              'boardCell',
              part ? `part${part.type}` : '',
              part ? `plane${part.planeId}` : '',
              attack ? `attack${attack.result}` : '',
              hasOverlap ? 'cellInvalid' : '',
              isSelectedHead ? 'selectedHead' : '',
            ]
              .filter(Boolean)
              .join(' ');

            return (
              <button
                key={`${model.side}-${row}-${col}`}
                type="button"
                className={className}
                onClick={() => {
                  if (part?.type === 'HEAD' && onHeadSelect) {
                    onHeadSelect(part.planeId);
                    return;
                  }
                  if (part) {
                    return;
                  }
                  if (attack) {
                    return;
                  }
                  onCellClick?.(row, col);
                }}
                draggable={canDragHead}
                onDragStart={(event) => {
                  if (!canDragHead || !part?.planeId) {
                    return;
                  }
                  const draggedPlaneParts = parts.filter((planePart) => planePart.planeId === part.planeId);
                  const dragImage = createPlaneDragImage(draggedPlaneParts, event.currentTarget.clientWidth);
                  if (dragImage) {
                    document.body.appendChild(dragImage.element);
                    event.dataTransfer.setDragImage(dragImage.element, dragImage.offsetX, dragImage.offsetY);
                    event.currentTarget.dataset.dragImageId = dragImage.element.id;
                  }
                  event.dataTransfer.effectAllowed = 'move';
                  event.dataTransfer.setData('text/plain', part.planeId);
                }}
                onDragEnd={(event) => {
                  const dragImageId = event.currentTarget.dataset.dragImageId;
                  if (dragImageId) {
                    document.getElementById(dragImageId)?.remove();
                    delete event.currentTarget.dataset.dragImageId;
                  }
                }}
                onDragOver={(event) => {
                  if (!onHeadDrop) {
                    return;
                  }
                  event.preventDefault();
                  event.dataTransfer.dropEffect = 'move';
                }}
                onDrop={(event) => {
                  if (!onHeadDrop) {
                    return;
                  }
                  event.preventDefault();
                  const planeId = event.dataTransfer.getData('text/plain');
                  if (planeId) {
                    onHeadDrop(planeId, row, col);
                  }
                }}
                disabled={disabled}
                aria-label={getCellAriaLabel(model.title, row, col, attack?.result, translate)}
              >
                {attack ? (
                  <span className="attackMarker">{getAttackMarker(attack.result)}</span>
                ) : part?.type === 'HEAD' ? (
                  part.planeId
                ) : (
                  ''
                )}
              </button>
            );
          })}
        </div>
        {model.showAttackLegend ? <AttackLegend translate={translate} /> : null}
        {model.hidden ? <div className="boardOverlay">{model.subtitle}</div> : null}
      </div>
    </section>
  );
}

function AttackLegend({ translate }: { translate: (key: MessageKey) => string }) {
  return (
    <div className="attackLegend" aria-label={translate('attackLegend')}>
      <div className="attackLegendRow">
        <span className="legendBlast legendBlastHead">✕</span>
        <span>{translate('hitHead')}</span>
      </div>
      <div className="attackLegendRow">
        <span className="legendBlast legendBlastPlane" />
        <span>{translate('hitPlane')}</span>
      </div>
      <div className="attackLegendRow">
        <span className="legendMiss">MISS</span>
        <span>{translate('miss')}</span>
      </div>
    </div>
  );
}

function SeatAction({
  model,
  canSit,
  canStandUp,
  onSitDown,
  onStandUp,
  translate,
}: {
  model: BoardPanelModel;
  canSit: boolean;
  canStandUp: boolean;
  onSitDown: (side: PlayerSide) => void;
  onStandUp: () => void;
  translate: (key: MessageKey) => string;
}) {
  return (
    <div className={`seatAction ${model.seated ? 'seatActionOccupied' : ''}`}>
      <div>
        <strong>{model.side === 'A' ? translate('seatA') : translate('seatB')}</strong>
        <span>{model.seated ? translate('occupied') : translate('available')}</span>
      </div>
      {canStandUp ? (
        <button type="button" className="secondaryButton" onClick={onStandUp}>
          {translate('standUp')}
        </button>
      ) : (
        <button type="button" disabled={!canSit || model.seated} onClick={() => onSitDown(model.side)}>
          {translate('sitDown')}
        </button>
      )}
    </div>
  );
}

function SideControlPanel({
  model,
  isCurrentPlayerSide,
  isDeploying,
  isPlaying,
  isFinished,
  isReady,
  currentTurn,
  winner,
  ownSide,
  selectedPlaneId,
  selectedDirection,
  canSubmit,
  messageKey,
  onSelectPlane,
  onSelectDirection,
  onSubmit,
  translate,
}: {
  model: BoardPanelModel;
  isCurrentPlayerSide: boolean;
  isDeploying: boolean;
  isPlaying: boolean;
  isFinished: boolean;
  isReady: boolean;
  currentTurn: PlayerSide | null;
  winner: PlayerSide | null;
  ownSide: PlayerSide | null;
  selectedPlaneId: string;
  selectedDirection: Direction;
  canSubmit: boolean;
  messageKey: MessageKey;
  onSelectPlane: (planeId: string) => void;
  onSelectDirection: (direction: Direction) => void;
  onSubmit: () => void;
  translate: (key: MessageKey) => string;
}) {
  if (isDeploying && isCurrentPlayerSide) {
    return (
      <DeploymentPanel
        role={model.side === 'A' ? 'PLAYER_A' : 'PLAYER_B'}
        isReady={isReady}
        selectedPlaneId={selectedPlaneId}
        selectedDirection={selectedDirection}
        planeIds={PLANE_IDS}
        directions={DIRECTIONS}
        canSubmit={canSubmit}
        messageKey={messageKey}
        onSelectPlane={onSelectPlane}
        onSelectDirection={onSelectDirection}
        onSubmit={onSubmit}
        translate={translate}
      />
    );
  }

  if (isPlaying || isFinished) {
    return (
      <aside className="controlPanel passiveControlPanel">
        <h2>{translate('battle')}</h2>
        <p>
          {getBattleControlText({
            model,
            currentTurn,
            winner,
            ownSide,
            isFinished,
            translate,
          })}
        </p>
      </aside>
    );
  }

  return (
    <aside className="controlPanel passiveControlPanel">
      <h2>{translate('deployment')}</h2>
      <p>{getPassiveControlText(model, isDeploying, translate)}</p>
    </aside>
  );
}

function getPassiveControlText(model: BoardPanelModel, isDeploying: boolean, translate: (key: MessageKey) => string) {
  if (!model.seated) {
    return translate('waitingForPlayer');
  }
  if (isDeploying) {
    return model.ready ? translate('deploymentSubmitted') : translate('spectatorDeploying');
  }
  return model.subtitle;
}

function getBattleControlText({
  model,
  currentTurn,
  winner,
  ownSide,
  isFinished,
  translate,
}: {
  model: BoardPanelModel;
  currentTurn: PlayerSide | null;
  winner: PlayerSide | null;
  ownSide: PlayerSide | null;
  isFinished: boolean;
  translate: (key: MessageKey) => string;
}) {
  if (isFinished) {
    return `${translate('winnerText')}: ${winner ?? '-'}`;
  }
  if (!ownSide) {
    return translate('spectatorPlaying');
  }
  if (model.side !== ownSide) {
    return currentTurn === ownSide ? translate('attackOpponentBoard') : translate('waitingTurn');
  }
  return currentTurn === ownSide ? translate('yourTurn') : translate('waitingTurn');
}

function canAttackBoard(model: BoardPanelModel, clientView: ClientView | null) {
  const gameState = clientView?.gameState;
  return Boolean(
    clientView?.side &&
      clientView.role !== 'SPECTATOR' &&
      gameState?.status === 'PLAYING' &&
      gameState.currentTurn === clientView.side &&
      model.side !== clientView.side,
  );
}

function getAttackMarker(result: AttackResult) {
  if (result === 'MISS') {
    return 'MISS';
  }
  if (result === 'HIT_HEAD') {
    return '✕';
  }
  return '';
}

function getCellAriaLabel(
  title: string,
  row: number,
  col: number,
  result: AttackResult | undefined,
  translate: (key: MessageKey) => string,
) {
  if (!result) {
    return `${title} ${row},${col} ${translate('attackCell')}`;
  }
  const resultKey = result === 'MISS' ? 'miss' : result === 'HIT_HEAD' ? 'hitHead' : 'hitPlane';
  return `${title} ${row},${col} ${translate('alreadyAttacked')} ${translate(resultKey)}`;
}

function buildBoardModels(clientView: ClientView | null, translate: (key: MessageKey) => string): BoardPanelModel[] {
  const gameState = clientView?.gameState;
  const isDeploying = gameState?.status === 'DEPLOYING';

  return [
    buildBoardModel('A', translate('playerA'), clientView, shouldHideBoard('A', clientView, isDeploying), isEditableBoard('A', clientView), translate),
    buildBoardModel('B', translate('playerB'), clientView, shouldHideBoard('B', clientView, isDeploying), isEditableBoard('B', clientView), translate),
  ];
}

function shouldHideBoard(side: PlayerSide, clientView: ClientView | null, isDeploying: boolean) {
  if (!isDeploying) {
    return false;
  }
  return clientView?.side !== side;
}

function isEditableBoard(side: PlayerSide, clientView: ClientView | null) {
  return clientView?.gameState.status === 'DEPLOYING' && clientView.side === side;
}

function buildBoardModel(
  side: PlayerSide,
  title: string,
  clientView: ClientView | null,
  hidden: boolean,
  editable: boolean,
  translate: (key: MessageKey) => string,
): BoardPanelModel {
  const gameState = clientView?.gameState;
  const board = side === 'A' ? gameState?.playerABoard ?? null : gameState?.playerBBoard ?? null;
  const seated = side === 'A' ? Boolean(gameState?.playerASeated) : Boolean(gameState?.playerBSeated);
  const ready = side === 'A' ? Boolean(gameState?.playerAReady) : Boolean(gameState?.playerBReady);
  const subtitle = getBoardSubtitle({ side, seated, ready, hidden, editable, clientView, translate });
  const showAttackLegend = shouldShowAttackLegend(side, clientView);

  return {
    side,
    title: `${title} (${side})`,
    subtitle,
    board,
    editable,
    hidden,
    ready,
    seated,
    showAttackLegend,
  };
}

function shouldShowAttackLegend(side: PlayerSide, clientView: ClientView | null) {
  const status = clientView?.gameState.status;
  return Boolean(
    clientView?.side &&
      side !== clientView.side &&
      (status === 'PLAYING' || status === 'FINISHED'),
  );
}

function getBoardSubtitle({
  side,
  seated,
  ready,
  hidden,
  editable,
  clientView,
  translate,
}: {
  side: PlayerSide;
  seated: boolean;
  ready: boolean;
  hidden: boolean;
  editable: boolean;
  clientView: ClientView | null;
  translate: (key: MessageKey) => string;
}) {
  if (!seated) {
    return translate('waitingForPlayer');
  }
  if (clientView?.gameState.status === 'DEPLOYING') {
    if (editable) {
      return ready ? translate('deploymentSubmitted') : translate('deployOnMyBoard');
    }
    if (hidden) {
      return ready ? translate('opponentReady') : translate('opponentDeploying');
    }
    return ready ? translate('deploymentSubmitted') : translate('spectatorDeploying');
  }
  if (clientView?.gameState.status === 'PLAYING') {
    if (clientView.side === null) {
      return translate('spectatorPlaying');
    }
    return clientView.side === side ? translate('myBoard') : translate('opponentBoard');
  }
  if (clientView?.gameState.status === 'FINISHED') {
    if (clientView.side === null) {
      return translate(`status.${clientView.gameState.status}`);
    }
    return clientView.side === side ? translate('myBoard') : translate('opponentBoard');
  }
  return translate(`status.${clientView?.gameState.status ?? 'WAITING'}`);
}
