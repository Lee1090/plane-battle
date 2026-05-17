import { useState } from 'react';
import type { GameStatus, PlayerRole, PlayerSide } from '../types/game';

export type Locale = 'en-US' | 'zh-CN';

export type MessageKey =
  | 'appTitle'
  | 'connection'
  | 'language'
  | 'role'
  | 'side'
  | 'status'
  | 'seatA'
  | 'seatB'
  | 'available'
  | 'occupied'
  | 'sitDown'
  | 'standUp'
  | 'waitingHint'
  | 'deployingHint'
  | 'playingHint'
  | 'finishedHint'
  | 'socketNotReady'
  | 'connectionFailed'
  | 'invalidMessage'
  | 'unknownError'
  | `status.${GameStatus}`
  | `role.${PlayerRole}`
  | `side.${PlayerSide}`;

const messages: Record<Locale, Record<MessageKey, string>> = {
  'en-US': {
    appTitle: 'Plane Battle',
    connection: 'Connection',
    language: '\u8bed\u8a00 / Language',
    role: 'Role',
    side: 'Side',
    status: 'Status',
    seatA: 'Player A Seat',
    seatB: 'Player B Seat',
    available: 'Available',
    occupied: 'Occupied',
    sitDown: 'Sit Down',
    standUp: 'Stand Up',
    waitingHint: 'Choose an empty seat to join the table.',
    deployingHint: 'Both players are seated. Deployment starts next.',
    playingHint: 'The game is in progress.',
    finishedHint: 'The game is finished.',
    socketNotReady: 'Connection is not ready.',
    connectionFailed: 'WebSocket connection failed.',
    invalidMessage: 'Received an invalid server message.',
    unknownError: 'Unknown error.',
    'status.WAITING': 'Waiting',
    'status.DEPLOYING': 'Deploying',
    'status.PLAYING': 'Playing',
    'status.FINISHED': 'Finished',
    'role.PLAYER_A': 'Player A',
    'role.PLAYER_B': 'Player B',
    'role.SPECTATOR': 'Spectator',
    'side.A': 'A',
    'side.B': 'B',
  },
  'zh-CN': {
    appTitle: '\u98de\u673a\u68cb\u76d8\u5bf9\u6218',
    connection: '\u8fde\u63a5',
    language: '\u8bed\u8a00 / Language',
    role: '\u8eab\u4efd',
    side: '\u9635\u8425',
    status: '\u72b6\u6001',
    seatA: '\u73a9\u5bb6 A \u5ea7\u4f4d',
    seatB: '\u73a9\u5bb6 B \u5ea7\u4f4d',
    available: '\u7a7a\u4f4d',
    occupied: '\u5df2\u5360\u7528',
    sitDown: '\u5750\u4e0b',
    standUp: '\u8d77\u8eab',
    waitingHint: '\u9009\u62e9\u4e00\u4e2a\u7a7a\u5ea7\u4f4d\u52a0\u5165\u684c\u5b50\u3002',
    deployingHint: '\u53cc\u65b9\u5df2\u5750\u4e0b\uff0c\u4e0b\u4e00\u6b65\u5f00\u59cb\u90e8\u7f72\u3002',
    playingHint: '\u6e38\u620f\u6b63\u5728\u8fdb\u884c\u3002',
    finishedHint: '\u6e38\u620f\u5df2\u7ed3\u675f\u3002',
    socketNotReady: '\u8fde\u63a5\u5c1a\u672a\u5c31\u7eea\u3002',
    connectionFailed: 'WebSocket \u8fde\u63a5\u5931\u8d25\u3002',
    invalidMessage: '\u6536\u5230\u65e0\u6548\u7684\u670d\u52a1\u7aef\u6d88\u606f\u3002',
    unknownError: '\u672a\u77e5\u9519\u8bef\u3002',
    'status.WAITING': '\u7b49\u5f85\u4e2d',
    'status.DEPLOYING': '\u90e8\u7f72\u4e2d',
    'status.PLAYING': '\u5bf9\u6218\u4e2d',
    'status.FINISHED': '\u5df2\u7ed3\u675f',
    'role.PLAYER_A': '\u73a9\u5bb6 A',
    'role.PLAYER_B': '\u73a9\u5bb6 B',
    'role.SPECTATOR': '\u89c2\u6218\u8005',
    'side.A': 'A',
    'side.B': 'B',
  },
};

function getDefaultLocale(): Locale {
  return navigator.language.toLowerCase().startsWith('zh') ? 'zh-CN' : 'en-US';
}

export function useMessages() {
  const [locale, setLocale] = useState<Locale>(getDefaultLocale);
  const t = (key: MessageKey) => messages[locale][key] ?? messages['en-US'][key] ?? key;

  return { locale, setLocale, t };
}
