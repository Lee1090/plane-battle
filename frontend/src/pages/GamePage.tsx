import { Seat } from '../components/Seat/Seat';
import { StatusPanel } from '../components/StatusPanel/StatusPanel';
import { useGameSocket } from '../hooks/useGameSocket';
import { useMessages } from '../i18n/messages';
import type { Locale, MessageKey } from '../i18n/messages';

export function GamePage() {
  const { locale, setLocale, t } = useMessages();
  const { clientView, connectionStatus, error, sitDown, standUp } = useGameSocket();
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
          {canStandUp ? (
            <button type="button" className="secondaryButton" onClick={standUp}>
              {t('standUp')}
            </button>
          ) : null}
        </div>
      </header>

      <section className="tableArea" aria-label={t('appTitle')}>
        <Seat
          label={t('seatA')}
          side="A"
          occupied={Boolean(gameState?.playerASeated)}
          canSit={canSit}
          availableText={t('available')}
          occupiedText={t('occupied')}
          sitDownText={t('sitDown')}
          onSitDown={sitDown}
        />

        <div className="gameTable" aria-hidden="true">
          <div className="tableCenter">
            <span>{gameState ? t(`status.${gameState.status}`) : t('connection')}</span>
          </div>
        </div>

        <Seat
          label={t('seatB')}
          side="B"
          occupied={Boolean(gameState?.playerBSeated)}
          canSit={canSit}
          availableText={t('available')}
          occupiedText={t('occupied')}
          sitDownText={t('sitDown')}
          onSitDown={sitDown}
        />
      </section>

      <StatusPanel clientView={clientView} connectionStatus={connectionStatus} error={error} translate={t} />
    </main>
  );
}
