import { getClientId } from '../../services/clientIdentity';
import { PLANE_IDS, type DraftPlane } from './planeShape';

interface StoredDeploymentDraft {
  selectedPlaneId: string;
  draftPlanes: DraftPlane[];
}

const DRAFT_STORAGE_PREFIX = 'plane-battle.deploymentDraft';

export function loadDeploymentDraft() {
  const rawDraft = window.localStorage.getItem(storageKey());
  if (!rawDraft) {
    return null;
  }

  try {
    const draft = JSON.parse(rawDraft) as StoredDeploymentDraft;
    if (!isValidDraft(draft)) {
      return null;
    }
    return draft;
  } catch {
    return null;
  }
}

export function saveDeploymentDraft(draft: StoredDeploymentDraft) {
  window.localStorage.setItem(storageKey(), JSON.stringify(draft));
}

export function clearDeploymentDraft() {
  window.localStorage.removeItem(storageKey());
}

function storageKey() {
  return `${DRAFT_STORAGE_PREFIX}.${getClientId()}`;
}

function isValidDraft(draft: StoredDeploymentDraft) {
  return (
    PLANE_IDS.includes(draft.selectedPlaneId)
    && Array.isArray(draft.draftPlanes)
    && draft.draftPlanes.length === PLANE_IDS.length
    && draft.draftPlanes.every((plane) => PLANE_IDS.includes(plane.id))
  );
}
