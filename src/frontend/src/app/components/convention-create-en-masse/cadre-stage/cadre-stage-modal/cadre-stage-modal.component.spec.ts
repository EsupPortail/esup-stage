import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
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

@Pipe({ name: 'contenu', standalone: false })
class ContenuPipeStub implements PipeTransform {
  transform(value: any): any {
    return value;
  }
}

describe('CadreStageModalComponent', () => {
  let component: CadreStageModalComponent;
  let fixture: ComponentFixture<CadreStageModalComponent>;
  let getApogeeInscriptions$: Subject<any>;
  let getConsigneByCentre: jasmine.Spy;

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
    getConsigneByCentre = jasmine.createSpy('getConsigneByCentre').and.returnValue(of(null));

    await TestBed.configureTestingModule({
      declarations: [CadreStageModalComponent, ContenuPipeStub],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
        { provide: MAT_DIALOG_DATA, useValue: { convention: conventionTypeGroupe } },
        { provide: EtudiantService, useValue: { getApogeeInscriptions: () => getApogeeInscriptions$.asObservable() } },
        { provide: CPAMService, useValue: { findAll: () => of([{ region: 'Grand Est', libelle: 'CPAM Bas-Rhin', adresse: 'Strasbourg' }]) } },
        { provide: LangueConventionService, useValue: { getListActiveByTypeConvention: () => of({ data: [{ code: 'fr', libelle: 'Français' }] }) } },
        { provide: CentreGestionService, useValue: { getCentreEtablissement: () => of({ id: 99, nomCentre: 'ETAB' }) } },
        { provide: ConventionService, useValue: { update: () => of({}) } },
        { provide: ConfigService, useValue: { getConfigGenerale: () => of({ autoriserElementPedagogiqueFacultatif: true }) } },
        { provide: ConsigneService, useValue: { getConsigneByCentre, getDocument: () => of(new Blob()) } },
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

  it('laisse inscription vide si une seule inscription Apogée ne matche pas l\'étape', fakeAsync(() => {
    initForm();

    const inscriptionNonMatchee = {
      ...inscriptionSansTypeGroupe,
      etapeInscription: { ...inscriptionSansTypeGroupe.etapeInscription, codeEtp: 'AUTRE', codVrsVet: '9' },
    };
    getApogeeInscriptions$.next([inscriptionNonMatchee]);
    tick();

    expect(component.inscriptions.length).toBe(1);
    expect(component.formConvention.get('inscription')?.value).toBeNull();
    expect(component.selectedInscription).toBeFalsy();
    expect(component.centreGestion).toBeFalsy();
  }));

  it('active centreGestion après sélection manuelle d\'une inscription non matchée', fakeAsync(() => {
    initForm();

    const inscriptionNonMatchee = {
      ...inscriptionSansTypeGroupe,
      etapeInscription: { ...inscriptionSansTypeGroupe.etapeInscription, codeEtp: 'AUTRE', codVrsVet: '9' },
    };
    getApogeeInscriptions$.next([inscriptionNonMatchee]);
    tick();

    component.formConvention.get('inscription')?.setValue(inscriptionNonMatchee);
    tick();

    expect(component.selectedInscription).toBe(inscriptionNonMatchee);
    expect(component.centreGestion?.id).toBe(1);
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

  it('remplit les libellés CPAM après sélection de région même sans consigne', fakeAsync(() => {
    getConsigneByCentre.and.returnValue(of(null));
    initForm();
    getApogeeInscriptions$.next([inscriptionSansTypeGroupe]);
    tick();
    component.formConvention.get('inscription')?.setValue(inscriptionSansTypeGroupe);
    tick();

    expect(component.consigneEtablissement).toBeUndefined();
    component.setCPAMLibelles({ value: 'Grand Est' });

    expect(component.libelles).toContain('CPAM Bas-Rhin');
    expect(component.formConvention.get('libelleCPAM')?.disabled).toBeFalse();
    // Valider utilisable : centre + inscription sélectionnée (condition HTML réelle)
    expect(!!component.centreGestion && component.inscriptions.length > 0 && !!component.selectedInscription).toBeTrue();
  }));

  describe('rendu consignes (template allégé sans Material)', () => {
    /**
     * Reproduit les gardes consignes + bouton Valider du HTML réel
     * sans importer MatAutocomplete.
     */
    const templateGardesConsigne = `
      @if (selectedInscription && (consigneEtablissement || centreGestion?.consigne)) {
        <div class="consignes-block">
          <h4>Consignes</h4>
          @if (consigneEtablissement) {
            <div>
              @if (consigneEtablissement.texte != null) {
                <div class="consigne-texte" [innerHTML]="consigneEtablissement.texte"></div>
              }
              @for (doc of consigneEtablissement.documents; track doc) {
                <div class="consigne-doc">{{doc.nomReel}}</div>
              }
            </div>
          }
        </div>
      }
      <button class="valid-button"
        [disabled]="!centreGestion || inscriptions.length === 0 || !selectedInscription">
        Valider
      </button>
    `;

    function configureWithTemplate(template: string, consigneResponse: any): void {
      TestBed.resetTestingModule();
      getApogeeInscriptions$ = new Subject<any>();
      getConsigneByCentre = jasmine.createSpy('getConsigneByCentre').and.returnValue(of(consigneResponse));

      TestBed.configureTestingModule({
        declarations: [CadreStageModalComponent, ContenuPipeStub],
        imports: [ReactiveFormsModule],
        providers: [
          { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
          { provide: MAT_DIALOG_DATA, useValue: { convention: conventionTypeGroupe } },
          { provide: EtudiantService, useValue: { getApogeeInscriptions: () => getApogeeInscriptions$.asObservable() } },
          { provide: CPAMService, useValue: { findAll: () => of([{ region: 'Grand Est', libelle: 'CPAM Bas-Rhin', adresse: 'Strasbourg' }]) } },
          { provide: LangueConventionService, useValue: { getListActiveByTypeConvention: () => of({ data: [{ code: 'fr', libelle: 'Français' }] }) } },
          { provide: CentreGestionService, useValue: { getCentreEtablissement: () => of({ id: 99, nomCentre: 'ETAB' }) } },
          { provide: ConventionService, useValue: { update: () => of({}) } },
          { provide: ConfigService, useValue: { getConfigGenerale: () => of({ autoriserElementPedagogiqueFacultatif: true }) } },
          { provide: ConsigneService, useValue: { getConsigneByCentre, getDocument: () => of(new Blob()) } },
          { provide: MessageService, useValue: { setWarning: jasmine.createSpy('setWarning'), setError: jasmine.createSpy('setError'), setSuccess: jasmine.createSpy('setSuccess') } },
        ],
        schemas: [NO_ERRORS_SCHEMA],
      }).overrideComponent(CadreStageModalComponent, { set: { template } });

      fixture = TestBed.createComponent(CadreStageModalComponent);
      component = fixture.componentInstance;
    }

    function validerSeraitDesactive(): boolean {
      return !component.centreGestion
        || component.inscriptions.length === 0
        || !component.selectedInscription;
    }

    /** Premier detectChanges = ngOnInit framework ; ensuite on pose l'état métier. */
    function bootAndSelectInscription(opts: { clearConsigne?: boolean } = {}): void {
      fixture.detectChanges();
      tick();
      component.inscriptions = [inscriptionSansTypeGroupe];
      component.centreGestion = inscriptionSansTypeGroupe.centreGestion;
      if (opts.clearConsigne) {
        component.consigneEtablissement = undefined;
      }
      component.formConvention.patchValue({ inscription: inscriptionSansTypeGroupe });
      expect(component.selectedInscription).toEqual(inscriptionSansTypeGroupe);
      expect(validerSeraitDesactive()).toBeFalse();
    }

    it('ne plante pas au rendu si consigne établissement absente et Valider reste utilisable', fakeAsync(() => {
      configureWithTemplate(templateGardesConsigne, { texte: '', documents: [] });
      bootAndSelectInscription({ clearConsigne: true });

      expect(component.consigneEtablissement).toBeUndefined();
      expect(() => fixture.detectChanges()).not.toThrow();
      // Pas de bloc consignes si ni consigne établissement ni consigne centre
      expect(fixture.nativeElement.querySelector('.consignes-block')).toBeNull();
      expect(validerSeraitDesactive()).toBeFalse();
      expect(fixture.nativeElement.querySelector('button.valid-button')).toBeTruthy();
      expect((fixture.nativeElement.querySelector('button.valid-button') as HTMLButtonElement).disabled).toBeFalse();
    }));

    it('affiche la consigne établissement quand elle est renseignée', fakeAsync(() => {
      configureWithTemplate(templateGardesConsigne, {
        texte: '<p>Consigne ETAB OK</p>',
        documents: [{ id: 1, nomReel: 'guide.pdf' }],
      });
      bootAndSelectInscription();
      expect(component.consigneEtablissement?.texte).toContain('Consigne ETAB OK');

      expect(() => fixture.detectChanges()).not.toThrow();
      expect(fixture.nativeElement.querySelector('.consignes-block')).toBeTruthy();
      expect(fixture.nativeElement.innerHTML).toContain('Consigne ETAB OK');
      expect(fixture.nativeElement.textContent).toContain('guide.pdf');
      expect(validerSeraitDesactive()).toBeFalse();
      expect((fixture.nativeElement.querySelector('button.valid-button') as HTMLButtonElement).disabled).toBeFalse();
    }));
  });
});
