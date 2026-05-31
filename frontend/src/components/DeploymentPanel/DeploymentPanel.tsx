import type { Direction, PlayerRole } from '../../types/game';
import type { MessageKey } from '../../i18n/messages';
import { isPlacementWarning } from '../GameBoardArea/deploymentInteraction';

interface DeploymentPanelProps {
  role: PlayerRole;
  isReady: boolean;
  selectedPlaneId: string;
  selectedDirection: Direction;
  planeIds: string[];
  directions: Direction[];
  canSubmit: boolean;
  messageKey: MessageKey;
  onSelectPlane: (planeId: string) => void;
  onSelectDirection: (direction: Direction) => void;
  onSubmit: () => void;
  translate: (key: MessageKey) => string;
}

export function DeploymentPanel({
  role,
  isReady,
  selectedPlaneId,
  selectedDirection,
  planeIds,
  directions,
  canSubmit,
  messageKey,
  onSelectPlane,
  onSelectDirection,
  onSubmit,
  translate,
}: DeploymentPanelProps) {
  const statusClassName = [
    'deploymentMessage',
    canSubmit && !isReady ? 'deploymentMessageValid' : '',
    !canSubmit && isPlacementWarning(messageKey) ? 'deploymentMessageWarning' : '',
  ]
    .filter(Boolean)
    .join(' ');

  if (role === 'SPECTATOR') {
    return (
      <aside className="controlPanel">
        <h2>{translate('deployment')}</h2>
        <p>{translate('spectatorDeploying')}</p>
      </aside>
    );
  }

  return (
    <aside className="controlPanel">
      <div className="deploymentHeader">
        <div>
          <h2>{translate('deployment')}</h2>
          <p className={statusClassName}>{isReady ? translate('deploymentSubmitted') : translate(messageKey)}</p>
        </div>
        <button type="button" disabled={!canSubmit || isReady} onClick={onSubmit}>
          {translate('submitDeployment')}
        </button>
      </div>

      <div className="deploymentControls">
        <label>
          <span>{translate('plane')}</span>
          <select value={selectedPlaneId} onChange={(event) => onSelectPlane(event.target.value)} disabled={isReady}>
            {planeIds.map((planeId) => (
              <option key={planeId} value={planeId}>
                {planeId}
              </option>
            ))}
          </select>
        </label>

        <label>
          <span>{translate('direction')}</span>
          <select value={selectedDirection} onChange={(event) => onSelectDirection(event.target.value as Direction)} disabled={isReady}>
            {directions.map((direction) => (
              <option key={direction} value={direction}>
                {translate(`direction.${direction}` as MessageKey)}
              </option>
            ))}
          </select>
        </label>
      </div>
    </aside>
  );
}
