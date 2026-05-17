import type { ClientView } from '../../types/game';
import type { MessageKey } from '../../i18n/messages';

interface StatusPanelProps {
  clientView: ClientView | null;
  connectionStatus: string;
  error: string | null;
  translate: (key: MessageKey) => string;
}

export function StatusPanel({ clientView, connectionStatus, error, translate }: StatusPanelProps) {
  const gameState = clientView?.gameState;

  return (
    <aside className="statusPanel">
      <dl>
        <div>
          <dt>{translate('connection')}</dt>
          <dd>{connectionStatus}</dd>
        </div>
        <div>
          <dt>{translate('role')}</dt>
          <dd>{clientView ? translate(`role.${clientView.role}`) : '-'}</dd>
        </div>
        <div>
          <dt>{translate('side')}</dt>
          <dd>{clientView?.side ? translate(`side.${clientView.side}`) : '-'}</dd>
        </div>
        <div>
          <dt>{translate('status')}</dt>
          <dd>{gameState ? translate(`status.${gameState.status}`) : '-'}</dd>
        </div>
      </dl>
      {error ? <p className="errorText">{translate(error as MessageKey)}</p> : null}
    </aside>
  );
}
