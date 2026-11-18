const CAS_LOGIN_PATH = '/login/cas';
const FRONTEND_SEGMENT = /\/frontend(?=\/|$)/;

/**
 * Construit le chemin absolu du filtre CAS à partir du pathname courant.
 *
 * Le contexte de déploiement est la partie du pathname qui précède `/frontend`
 * (`/esup-stage` pour `/esup-stage/frontend/`). Un chemin relatif n'est pas utilisable :
 * avec `<base href="/">`, `../login/cas` est résolu à la racine de l'hôte, hors contexte.
 */
export function buildCasLoginPath(pathname: string): string {
  const match = FRONTEND_SEGMENT.exec(pathname);
  const base = match ? pathname.substring(0, match.index) : pathname;
  const contextPath = base.replace(/\/+$/, '');
  return `${contextPath}${CAS_LOGIN_PATH}`;
}

/** Chemin complet (pathname + search + hash) à mémoriser avant la redirection CAS. */
export function buildRedirectTarget(
  pathname: string,
  search: string,
  hash: string,
): string {
  return `${pathname}${search}${hash}`;
}

/**
 * Valide une cible mémorisée : seuls les chemins absolus de la même origine sont acceptés.
 * Retourne la cible normalisée, ou `null` si elle est absente ou invalide.
 */
export function sanitizeRedirectTarget(
  stored: string | null,
  origin: string,
): string | null {
  if (!stored || !stored.startsWith('/') || stored.startsWith('//')) {
    return null;
  }
  try {
    const url = new URL(stored, origin);
    if (url.origin !== origin) {
      return null;
    }
    return `${url.pathname}${url.search}${url.hash}`;
  } catch {
    return null;
  }
}

/**
 * Décide de la cible à restaurer au retour de CAS.
 *
 * La restauration n'a lieu que si une redirection CAS a été initiée par l'application
 * (marqueur présent) et si la cible est un chemin de la même origine. Une cible identique
 * à la page courante est ignorée pour ne pas recharger une fiche déjà ouverte.
 */
export function resolveCasRedirect(
  stored: string | null,
  casRedirectInProgress: boolean,
  origin: string,
  currentTarget: string,
): string | null {
  if (!casRedirectInProgress) {
    return null;
  }
  const target = sanitizeRedirectTarget(stored, origin);
  if (target === null || target === currentTarget) {
    return null;
  }
  return target;
}
