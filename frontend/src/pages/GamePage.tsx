import { GameBoardArea } from '../components/GameBoardArea/GameBoardArea';
import { StatusPanel } from '../components/StatusPanel/StatusPanel';
import { useGameSocket } from '../hooks/useGameSocket';
import { useMessages } from '../i18n/messages';
import type { Locale, MessageKey } from '../i18n/messages';

export function GamePage() {
  const { locale, setLocale, t } = useMessages();
  const { clientView, connectionStatus, error, sitDown, standUp, submitDeployment, attack } = useGameSocket();
  const gameState = clientView?.gameState;
  const role = clientView?.role ?? 'SPECTATOR';
  const canSit = role === 'SPECTATOR' && gameState?.status === 'WAITING';
  const canStandUp = role !== 'SPECTATOR' && gameState?.status !== 'PLAYING' && gameState?.status !== 'FINISHED';

  const hintKeys: Record<string, MessageKey> = {
    WAITING: 'waitingHint',
    DEPLOYING: 'deployingHint',
    PLAYING: 'playingHint',
    FINISHED: 'finishedHint',
  };
  const hintKey = gameState ? hintKeys[gameState.status] : 'waitingHint';

  return (
    <main className="appShell">
      <header className="topBar">
        <div>
          <h1>{t('appTitle')}</h1>
          <p>{t(hintKey)}</p>
        </div>
        <div className="topActions">
          <label>
            <span>{t('language')}</span>
            <select value={locale} onChange={(event) => setLocale(event.target.value as Locale)}>
              <option value="en-US">English</option>
              <option value="zh-CN">{'\u7b80\u4f53\u4e2d\u6587'}</option>
            </select>
          </label>
        </div>
      </header>

      <GameBoardArea
        clientView={clientView}
        canSit={canSit}
        canStandUp={canStandUp}
        onSitDown={sitDown}
        onStandUp={standUp}
        onSubmitDeployment={submitDeployment}
        onAttack={attack}
        translate={t}
      />

      <StatusPanel clientView={clientView} connectionStatus={connectionStatus} error={error} translate={t} />
    </main>
  );
}
