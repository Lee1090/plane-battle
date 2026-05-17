import { useCallback, useEffect, useRef, useState } from 'react';
import { createGameSocket, joinMessage, sitDownMessage, standUpMessage } from '../services/gameSocket';
import type { ClientMessage, ClientView, PlayerSide, ServerMessage } from '../types/game';

type ConnectionStatus = 'CONNECTING' | 'OPEN' | 'CLOSED' | 'ERROR';

export function useGameSocket() {
  const socketRef = useRef<WebSocket | null>(null);
  const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus>('CONNECTING');
  const [clientView, setClientView] = useState<ClientView | null>(null);
  const [error, setError] = useState<string | null>(null);

  const send = useCallback((message: ClientMessage) => {
    const socket = socketRef.current;
    if (!socket || socket.readyState !== WebSocket.OPEN) {
      setError('socketNotReady');
      return;
    }
    socket.send(JSON.stringify(message));
  }, []);

  useEffect(() => {
    const socket = createGameSocket();
    socketRef.current = socket;
    setConnectionStatus('CONNECTING');

    socket.addEventListener('open', () => {
      setConnectionStatus('OPEN');
      socket.send(JSON.stringify(joinMessage()));
    });

    socket.addEventListener('message', (event) => {
      let message: ServerMessage<ClientView>;
      try {
        message = JSON.parse(event.data) as ServerMessage<ClientView>;
      } catch {
        setError('invalidMessage');
        return;
      }

      if (message.type === 'CONNECTED' || message.type === 'STATE_UPDATE') {
        setClientView(message.data ?? null);
        setError(null);
        return;
      }
      if (message.type === 'ERROR') {
        setError(message.error ?? 'unknownError');
      }
    });

    socket.addEventListener('close', () => {
      setConnectionStatus('CLOSED');
    });

    socket.addEventListener('error', () => {
      setConnectionStatus('ERROR');
      setError('connectionFailed');
    });

    return () => {
      socket.close();
      socketRef.current = null;
    };
  }, []);

  return {
    clientView,
    connectionStatus,
    error,
    sitDown: (side: PlayerSide) => send(sitDownMessage(side)),
    standUp: () => send(standUpMessage()),
  };
}
