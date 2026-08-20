import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';

import { DashboardComponent } from './dashboard.component';
import { ConventionService } from '../../services/convention.service';
import { AuthService } from '../../services/auth.service';
import { UfrService } from '../../services/ufr.service';
import { EtapeService } from '../../services/etape.service';
import { MessageService } from '../../services/message.service';
import { ConfigService } from '../../services/config.service';
import { ContenuPipe } from '../../pipes/contenu.pipe';
import { LangueConventionService } from '../../services/langue-convention.service';
import { TypeConventionService } from '../../services/type-convention.service';
import { PaysService } from '../../services/pays.service';

describe('DashboardComponent', () => {
  let component: DashboardComponent;

  const paginated = { getPaginated: () => of({ data: [] }) };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      providers: [
        { provide: ConventionService, useValue: { getListAnnee: () => of([]) } },
        { provide: AuthService, useValue: { isGestionnaire: () => true, isEnseignant: () => false, isEtudiant: () => false } },
        { provide: Router, useValue: { getCurrentNavigation: () => null, navigate: () => {} } },
        { provide: UfrService, useValue: paginated },
        { provide: EtapeService, useValue: paginated },
        { provide: MessageService, useValue: { setError: () => {} } },
        { provide: ConfigService, useValue: { getConfigGenerale: () => of({}) } },
        { provide: ContenuPipe, useValue: { transform: (v: any) => v } },
        { provide: LangueConventionService, useValue: paginated },
        { provide: TypeConventionService, useValue: paginated },
        { provide: PaysService, useValue: paginated },
      ]
    }).compileComponents();

    // createComponent seul : on n'exécute pas ngOnInit, qui déclenche des appels réseau.
    component = TestBed.createComponent(DashboardComponent).componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('modèles de colonnes de l\u2019export', () => {
    /** Toutes les clés de colonnes proposées à l'export, tous onglets confondus. */
    const clesDisponibles = (exportColumns: any): Set<string> => {
      const feuilles = exportColumns.singleExcelSheet || exportColumns.multipleExcelSheets || [];
      const cles = new Set<string>();
      feuilles.forEach((f: any) => Object.keys(f.columns).forEach(k => cles.add(k)));
      return cles;
    };

    beforeEach(() => component.setDataGestionnaire());

    it('déclare des modèles non vides', () => {
      expect(component.exportPresets.length).toBeGreaterThan(0);
      component.exportPresets.forEach(preset => {
        expect(preset.libelle).toBeTruthy();
        expect(preset.cles.length).toBeGreaterThan(0);
      });
    });

    // Garde-fou : une clé erronée n'échoue pas à l'application, elle est ignorée
    // et la colonne manque silencieusement dans le fichier exporté.
    it('n\u2019utilise que des clés réellement exportables', () => {
      const disponibles = clesDisponibles(component.exportColumns);
      component.exportPresets.forEach(preset => {
        preset.cles.forEach((cle: string) => {
          expect(disponibles.has(cle))
            .withContext(`modèle « ${preset.libelle} » : clé inconnue « ${cle} »`)
            .toBeTrue();
        });
      });
    });

    it('ne répète pas deux fois la même clé dans un modèle', () => {
      component.exportPresets.forEach(preset => {
        expect(new Set(preset.cles).size)
          .withContext(`modèle « ${preset.libelle} »`)
          .toBe(preset.cles.length);
      });
    });
  });
});
