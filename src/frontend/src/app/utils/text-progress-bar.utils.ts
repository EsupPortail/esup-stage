/*
* Retourne le texte de progression pour un onglet donné, uniquement à des fins d'accessibilité, n'est pas affiché à l'écran.
* @param index L'index de l'onglet
* @param statut Le statut de l'onglet (0: non complété, 1: en cours, 2: terminé)
* @returns Le texte de progression
*/
export function getProgressText(index: number, value : number, statut: number | null | undefined): string {
  if (statut === 2) {
    return `${index + 1} terminé, ${value} pour cent complétés`;
  }

  if (statut === 1) {
    return `${index + 1} en cours, ${value} pour cent complétés`;
  }

  return `${index + 1} non complété, ${value} pour cent complétés`;
}
