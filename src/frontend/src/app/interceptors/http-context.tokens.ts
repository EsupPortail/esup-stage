import { HttpContextToken } from '@angular/common/http';

/**
 * Marque une requête comme « silencieuse ».
 *
 * Le TechnicalInterceptor n'affiche alors ni le loader plein écran ni la popup
 * d'erreur générique : la requête reste totalement discrète et son résultat
 * (succès comme erreur) est géré uniquement par l'appelant.
 *
 * Utilisé par le keep-alive de session (ping périodique de rafraîchissement)
 * pour ne pas perturber l'utilisateur en cours de rédaction.
 */
export const SILENT_REQUEST = new HttpContextToken<boolean>(() => false);
