import { TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';

import { ColumnSelectorComponent } from './column-selector.component';
import { ContenuService } from '../../../services/contenu.service';

describe('ColumnSelectorComponent', () => {
  // Deux onglets, avec des clés volontairement communes (id, etudiantNom)
  // pour couvrir le cas d'une colonne proposée par les deux.
  const donnees = () => ({
    sheets: [
      {
        title: 'Données stage',
        availableColumns: [
          { key: 'id', title: 'N° de la convention' },
          { key: 'etudiantNom', title: 'Nom Étudiant' },
          { key: 'theme', title: 'Thématique' },
          { key: 'sujetStage', title: 'Sujet' },
        ]
      },
      {
        title: 'Données structure d’accueil',
        availableColumns: [
          { key: 'id', title: 'N° de la convention' },
          { key: 'etudiantNom', title: 'Nom Étudiant' },
          { key: 'structure', title: 'Nom structure d’accueil' },
        ]
      }
    ],
    presets: [
      { libelle: 'Annuaire', cles: ['etudiantNom', 'theme', 'structure'] }
    ]
  });

  const creer = (data: any = donnees()): ColumnSelectorComponent => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      declarations: [ColumnSelectorComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: MatDialogRef, useValue: { close: () => {} } },
        { provide: MAT_DIALOG_DATA, useValue: data },
        { provide: ContenuService, useValue: { get: () => of({ texte: '' }) } },
      ]
    });
    // Toute l'initialisation se fait dans le constructeur : pas de detectChanges.
    return TestBed.createComponent(ColumnSelectorComponent).componentInstance;
  };

  const clesSelectionnees = (c: ColumnSelectorComponent) =>
    c.sheets.map(s => s.selectedColumns.map((col: any) => col.key));

  it('should create', () => {
    expect(creer()).toBeTruthy();
  });

  describe('modèles de colonnes', () => {
    it('dirige chaque clé vers le premier onglet qui la propose, sans doublon', () => {
      const c = creer();
      c.appliquerPreset(c.presets[0]);

      // etudiantNom existe dans les deux onglets : sélectionné dans le premier seulement
      expect(clesSelectionnees(c)).toEqual([['etudiantNom', 'theme'], ['structure']]);
      expect(c.sheets[1].availableColumns.some((col: any) => col.key === 'etudiantNom')).toBeTrue();
    });

    it('respecte l\u2019ordre déclaré dans le modèle', () => {
      const c = creer({
        ...donnees(),
        presets: [{ libelle: 'Ordre inversé', cles: ['sujetStage', 'theme', 'etudiantNom'] }]
      });
      c.appliquerPreset(c.presets[0]);

      expect(clesSelectionnees(c)[0]).toEqual(['sujetStage', 'theme', 'etudiantNom']);
    });

    it('remplace intégralement une sélection existante', () => {
      const c = creer();
      // Sélection manuelle préalable, sans rapport avec le modèle
      c.selectedAvailableKeys = new Set(['id', 'sujetStage']);
      c.addSelected(c.sheets[0]);
      expect(clesSelectionnees(c)[0]).toEqual(['id', 'sujetStage']);

      c.appliquerPreset(c.presets[0]);

      expect(clesSelectionnees(c)).toEqual([['etudiantNom', 'theme'], ['structure']]);
      expect(c.selectedAvailableKeys.size).toBe(0);
      expect(c.selectedChosenKeys.size).toBe(0);
    });

    it('ignore une clé inconnue sans lever d\u2019erreur', () => {
      const c = creer({
        ...donnees(),
        presets: [{ libelle: 'Avec faute', cles: ['theme', 'cleInexistante', 'structure'] }]
      });

      expect(() => c.appliquerPreset(c.presets[0])).not.toThrow();
      expect(clesSelectionnees(c)).toEqual([['theme'], ['structure']]);
    });

    it('ne fait rien si aucun modèle n\u2019est fourni', () => {
      const c = creer({ sheets: donnees().sheets });

      expect(c.presets).toEqual([]);
      expect(() => c.appliquerPreset(null)).not.toThrow();
      expect(clesSelectionnees(c)).toEqual([[], []]);
    });

    it('retire le modèle affiché lors d\u2019une réinitialisation', () => {
      const c = creer();
      c.presetChoisi = c.presets[0];
      c.appliquerPreset(c.presets[0]);

      c.reinitialiser(c.sheets[0]);

      expect(c.presetChoisi).toBeNull();
      expect(c.sheets[0].selectedColumns).toEqual([]);
    });
  });
});
