const CLIENT_ID_STORAGE_KEY = 'plane-battle.clientId';

export function getClientId() {
  const storedClientId = window.localStorage.getItem(CLIENT_ID_STORAGE_KEY);
  if (storedClientId) {
    return storedClientId;
  }

  const clientId = createClientId();
  window.localStorage.setItem(CLIENT_ID_STORAGE_KEY, clientId);
  return clientId;
}

function createClientId() {
  if (window.crypto?.randomUUID) {
    return window.crypto.randomUUID();
  }

  return `client-${Date.now()}-${Math.random().toString(36).slice(2)}`;
}
