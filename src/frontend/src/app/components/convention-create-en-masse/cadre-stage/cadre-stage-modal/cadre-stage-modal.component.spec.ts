import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of, Subject } from 'rxjs';

import { CadreStageModalComponent } from './cadre-stage-modal.component';
import { EtudiantService } from '../../../../services/etudiant.service';
import { CPAMService } from '../../../../services/cpam.service';
import { LangueConventionService } from '../../../../services/langue-convention.service';
import { CentreGestionService } from '../../../../services/centre-gestion.service';
import { ConventionService } from '../../../../services/convention.service';
import { ConfigService } from '../../../../services/config.service';
import { ConsigneService } from '../../../../services/consigne.service';
import { MessageService } from '../../../../services/message.service';

describe('CadreStageModalComponent', () => {
  let component: CadreStageModalComponent;
  let fixture: ComponentFixture<CadreStageModalComponent>;
  let getApogeeInscriptions$: Subject<any>;

  const typeInitiale = { id: 7, libelle: 'FORMATION INITIALE', codeCtrl: 'INITIALE' };
  const typeAutre = { id: 42, libelle: 'Type groupe', codeCtrl: 'GROUPE-1' };

  const conventionTypeGroupe = {
    id: 100,
    adresseEtudiant: '1 rue Test',
    codePostalEtudiant: '67000',
    villeEtudiant: 'Strasbourg',
    paysEtudiant: 'France',
    telEtudiant: null,
    telPortableEtudiant: null,
    courrielPersoEtudiant: 'etu@example.com',
    regionCPAM: null,
    libelleCPAM: null,
    adresseCPAM: null,
    typeConvention: typeAutre,
    langueConvention: { code: 'fr', libelle: 'Français' },
    etudiant: { numEtudiant: '12345678', nom: 'Doe', prenom: 'Jane', mail: 'j@u.fr', identEtudiant: 'jdoe' },
    etape: { id: { code: 'ETAPE1', codeVersionEtape: '1' } },
    codeElp: null,
  };

  const inscriptionSansTypeGroupe = {
    annee: 2025,
    centreGestion: { id: 1, niveauCentre: { libelle: 'ETABLISSEMENT' } },
    typeConvention: typeInitiale,
    typeConventionsDisponibles: [typeInitiale],
    etapeInscription: {
      codeEtp: 'ETAPE1',
      codVrsVet: '1',
      codeComposante: 'COMP',
      libComposante: 'Composante',
      libWebVet: 'Étape 1',
    },
    elementPedagogiques: [],
  };

  function hasRequiredValidator(controlName: string): boolean {
    const control = component.formConvention.get(controlName);
    return !!control?.hasValidator(Validators.required);
  }

  /** Initialise le formulaire sans rendre le template Material. */
  function initForm(): void {
    component.ngOnInit();
    tick();
  }

  beforeEach(async () => {
    getApogeeInscriptions$ = new Subject<any>();

    await TestBed.configureTestingModule({
      declarations: [CadreStageModalComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
        { provide: MAT_DIALOG_DATA, useValue: { convention: conventionTypeGroupe } },
        { provide: EtudiantService, useValue: { getApogeeInscriptions: () => getApogeeInscriptions$.asObservable() } },
        { provide: CPAMService, useValue: { findAll: () => of([]) } },
        { provide: LangueConventionService, useValue: { getListActiveByTypeConvention: () => of({ data: [{ code: 'fr', libelle: 'Français' }] }) } },
        { provide: CentreGestionService, useValue: { getCentreEtablissement: () => of(null) } },
        { provide: ConventionService, useValue: { update: () => of({}) } },
        { provide: ConfigService, useValue: { getConfigGenerale: () => of({ autoriserElementPedagogiqueFacultatif: true }) } },
        { provide: ConsigneService, useValue: { getConsigneByCentre: () => of(null), getDocument: () => of(new Blob()) } },
        { provide: MessageService, useValue: { setWarning: jasmine.createSpy('setWarning'), setError: jasmine.createSpy('setError'), setSuccess: jasmine.createSpy('setSuccess') } },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(CadreStageModalComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('rend les champs CPAM facultatifs dès l\'init pour un type non-INSPE', fakeAsync(() => {
    initForm();

    expect(component.formConvention).toBeTruthy();
    expect(component.convention.typeConvention.codeCtrl).toBe('GROUPE-1');
    expect(hasRequiredValidator('regionCPAM')).toBeFalse();
    expect(hasRequiredValidator('libelleCPAM')).toBeFalse();
    expect(hasRequiredValidator('adresseCPAM')).toBeFalse();
    expect(component.typeConventions.some((tc: any) => tc.id === typeAutre.id)).toBeTrue();
  }));

  it('conserve CPAM facultatifs si l\'inscription Apogée n\'est pas trouvée', fakeAsync(() => {
    initForm();

    getApogeeInscriptions$.next([{
      ...inscriptionSansTypeGroupe,
      etapeInscription: { ...inscriptionSansTypeGroupe.etapeInscription, codeEtp: 'AUTRE' },
    }]);
    tick();

    expect(hasRequiredValidator('regionCPAM')).toBeFalse();
    expect(component.formConvention.get('idTypeConvention')?.value).toBe(typeAutre.id);
    expect(component.typeConventions.some((tc: any) => tc.id === typeAutre.id)).toBeTrue();
  }));

  it('ajoute le type du groupe aux options même s\'il est absent de typeConventionsDisponibles', fakeAsync(() => {
    initForm();

    getApogeeInscriptions$.next([inscriptionSansTypeGroupe]);
    tick();

    expect(component.typeConventions.map((tc: any) => tc.id)).toContain(typeAutre.id);
    expect(component.typeConventions.map((tc: any) => tc.id)).toContain(typeInitiale.id);
    expect(component.formConvention.get('idTypeConvention')?.value).toBe(typeAutre.id);
    expect(hasRequiredValidator('regionCPAM')).toBeFalse();
    expect(hasRequiredValidator('libelleCPAM')).toBeFalse();
    expect(hasRequiredValidator('adresseCPAM')).toBeFalse();
  }));
});
