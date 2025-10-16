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
    if (this.data.operationType === 'MODIFICATION' || this.data.operationType === 'MODIFICATION_AUTOMATIQUE') {
      try {
        let previousState = this.data.etatPrecedent;
        let currentState = this.data.etatActuel;

        // Si les états sont des chaînes, on les parse
        if (typeof previousState === 'string') {
          previousState = JSON.parse(previousState);
        }
        if (typeof currentState === 'string') {
          currentState = JSON.parse(currentState);
        }

        // Affiche les objets pour le debug
        console.log('Previous state:', previousState);
        console.log('Current state:', currentState);

        // Compare les objets et stocke les différences
        this.differences = this.compareObjects(previousState, currentState);

        // Affiche les différences pour le debug
        console.log('Differences:', this.differences);
      } catch (error) {
        console.error('Erreur lors du parsing JSON:', error);
      }
    }
  }

  private compareObjects(prev: any, curr: any, path: string = ''): JsonDifference[] {
    const differences: JsonDifference[] = [];

    const allKeys = new Set([...Object.keys(prev || {}), ...Object.keys(curr || {})]);

    allKeys.forEach(key => {
      const fullPath = path ? `${path}.${key}` : key;
      const prevValue = prev?.[key];
      const currValue = curr?.[key];

      // Ignore la clé "dateModif"
      if (fullPath === 'dateModif') return;

      const isObject = (val: any) => val && typeof val === 'object' && !Array.isArray(val);

      if (isObject(prevValue) || isObject(currValue)) {
        // Recursion si sous-objet
        differences.push(...this.compareObjects(prevValue || {}, currValue || {}, fullPath));
      } else {
        if (this.displayFieldMapping[fullPath] && JSON.stringify(prevValue) !== JSON.stringify(currValue)) {
          differences.push({
            field: fullPath,
            displayName: this.displayFieldMapping[fullPath],
            previousValue: this.formatValue(prevValue),
            currentValue: this.formatValue(currValue),
            type: Array.isArray(prevValue) || Array.isArray(currValue) ? 'array' : 'primitive'
          });
        }
      }
    });

    return differences;
  }

  private formatValue(value: any): string {
    if (value === null || value === undefined) return 'Non renseigné';
    if (typeof value === 'object' && value.libelle) return value.libelle;
    if (typeof value === 'boolean') return value ? 'Oui' : 'Non';
    if (value instanceof Date) return new Date(value).toLocaleDateString('fr-FR');
    return String(value);
  }

  close(): void {
    console.log(this.differences);
    this.dialogRef.close();
  }
}
