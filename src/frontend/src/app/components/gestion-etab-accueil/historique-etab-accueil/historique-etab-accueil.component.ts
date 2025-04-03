import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';

interface JsonDifference {
  field: string;
  displayName: string;
  previousValue: any;
  currentValue: any;
  type: 'object' | 'array' | 'primitive';
}

@Component({
  selector: 'app-historique-etab-accueil',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatDividerModule
  ],
  templateUrl: './historique-etab-accueil.component.html',
  styleUrl: './historique-etab-accueil.component.scss'
})
export class HistoriqueEtabAccueilComponent implements OnInit {
  differences: JsonDifference[] = [];
  displayFieldMapping: { [key: string]: string } = {
    'raisonSociale': 'Raison sociale',
    'numeroSiret': 'N° SIRET',
    'activitePrincipale': 'Activité principale',
    'telephone': 'Téléphone',
    'fax': 'Fax',
    'mail': 'Email',
    'siteWeb': 'Site web',
    'groupe': 'Groupe',
    'effectif.libelle': 'Effectif',
    'statutJuridique.libelle': 'Statut juridique',
    'voie': 'Adresse',
    'batimentResidence': 'Bâtiment/Résidence',
    'codePostal': 'Code postal',
    'commune': 'Commune',
    'libCedex': 'CEDEX',
    'pays.lib': 'Pays',
    'nomDirigeant': 'Nom du dirigeant',
    'prenomDirigeant': 'Prénom du dirigeant',
    'nafN5.code': 'Code NAF',
    'nafN5.libelle': 'Libellé NAF',
    'dateCreation': 'Date de création',
    'dateModif': 'Date de modification',
    'loginCreation': 'Créé par',
    'loginModif': 'Modifié par',
    'estValidee': 'Validée',
    'dateValidation': 'Date de validation',
    'loginValidation': 'Validé par'
  };

  constructor(
    public dialogRef: MatDialogRef<HistoriqueEtabAccueilComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  ngOnInit(): void {
    if (this.data && this.data.etatPrecedent && this.data.etatActuel) {
      // Parse les deux états JSON
      const previousState = JSON.parse(this.data.etatPrecedent);
      const currentState = JSON.parse(this.data.etatActuel);

      // Compare les objets et extrait les différences
      this.differences = this.compareObjects(previousState, currentState);
    }
  }

  /**
   * Compare deux objets et retourne les différences
   */
  compareObjects(oldObj: any, newObj: any, parentPath: string = ''): JsonDifference[] {
    const differences: JsonDifference[] = [];

    // Récupère toutes les clés des deux objets
    const allKeys = new Set([
      ...Object.keys(oldObj || {}),
      ...Object.keys(newObj || {})
    ]);

    // Vérifie chaque clé
    allKeys.forEach(key => {
      const oldValue = oldObj?.[key];
      const newValue = newObj?.[key];

      // Construit le chemin complet pour cette propriété
      const currentPath = parentPath ? `${parentPath}.${key}` : key;

      // Si les valeurs sont différentes
      if (!this.areEqual(oldValue, newValue)) {
        // Si nous avons des objets non-null, comparer récursivement
        if (
          oldValue && newValue &&
          typeof oldValue === 'object' && typeof newValue === 'object' &&
          !Array.isArray(oldValue) && !Array.isArray(newValue)
        ) {
          // Ajoute les différences des objets enfants
          differences.push(...this.compareObjects(oldValue, newValue, currentPath));
        } else {
          // Ajoute la différence pour cette propriété primitive ou tableau
          const displayName = this.getDisplayName(currentPath);

          // Détermine le type de valeur
          let type: 'object' | 'array' | 'primitive' = 'primitive';
          if (Array.isArray(oldValue) || Array.isArray(newValue)) {
            type = 'array';
          } else if (
            (oldValue && typeof oldValue === 'object') ||
            (newValue && typeof newValue === 'object')
          ) {
            type = 'object';
          }

          // Ajoute la différence
          differences.push({
            field: currentPath,
            displayName,
            previousValue: oldValue,
            currentValue: newValue,
            type
          });
        }
      }
    });

    return differences;
  }

  /**
   * Vérifie si deux valeurs sont égales
   */
  areEqual(value1: any, value2: any): boolean {
    // Si les deux sont des dates
    if (value1 instanceof Date && value2 instanceof Date) {
      return value1.getTime() === value2.getTime();
    }

    // Si un des deux est null ou undefined
    if (value1 === null || value1 === undefined || value2 === null || value2 === undefined) {
      return value1 === value2;
    }

    // Si les deux sont des objets
    if (typeof value1 === 'object' && typeof value2 === 'object') {
      // Si les deux sont des tableaux
      if (Array.isArray(value1) && Array.isArray(value2)) {
        if (value1.length !== value2.length) return false;

        for (let i = 0; i < value1.length; i++) {
          if (!this.areEqual(value1[i], value2[i])) return false;
        }

        return true;
      }

      // Si l'un est un tableau et l'autre non
      if (Array.isArray(value1) !== Array.isArray(value2)) return false;

      // Comparer les clés des objets
      const keys1 = Object.keys(value1);
      const keys2 = Object.keys(value2);

      if (keys1.length !== keys2.length) return false;

      for (const key of keys1) {
        if (!keys2.includes(key)) return false;
        if (!this.areEqual(value1[key], value2[key])) return false;
      }

      return true;
    }

    // Pour les valeurs primitives
    return value1 === value2;
  }

  /**
   * Obtient un nom d'affichage convivial pour un champ
   */
  getDisplayName(fieldPath: string): string {
    // Utilise le mapping si disponible
    if (this.displayFieldMapping[fieldPath]) {
      return this.displayFieldMapping[fieldPath];
    }

    // Sinon, fait un formatage basique
    return fieldPath
      .split('.')
      .pop()!
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase());
  }

  /**
   * Formate une valeur pour l'affichage
   */
  formatValue(value: any): string {
    if (value === null || value === undefined) {
      return 'Non défini';
    }

    if (typeof value === 'boolean') {
      return value ? 'Oui' : 'Non';
    }

    if (typeof value === 'number' && (String(value).length === 13 || String(value).length === 10)) {
      // C'est probablement un timestamp
      const date = new Date(value);
      if (!isNaN(date.getTime())) {
        return date.toLocaleString('fr-FR');
      }
    }

    if (typeof value === 'object') {
      // Si c'est un objet, on renvoie sa représentation JSON
      if (value.libelle) {
        return value.libelle;
      } else if (value.lib) {
        return value.lib;
      } else {
        return JSON.stringify(value);
      }
    }

    return String(value);
  }

  close(): void {
    this.dialogRef.close();
  }
}
