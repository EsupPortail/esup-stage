import { Component, OnInit, OnDestroy, Input, Output, EventEmitter, ViewChildren, QueryList } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from "@angular/forms";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { MessageService } from "../../../services/message.service";
import { LdapService } from "../../../services/ldap.service";
import { EnseignantService } from "../../../services/enseignant.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { debounceTime, takeUntil } from "rxjs/operators";
import { ConfigService } from "../../../services/config.service";
import { CdkDragDrop, moveItemInArray } from "@angular/cdk/drag-drop";
import { Subject } from "rxjs";
import { AccessibilityService } from "../../../services/accessibility.service";

@Component({
    selector: 'app-param-centre',
    templateUrl: './param-centre.component.html',
    styleUrls: ['./param-centre.component.scss'],
    standalone: false
})
export class ParamCentreComponent implements OnInit, OnDestroy {

  @Input() centreGestion: any;
  @Input() form!: FormGroup;

  dureeRecupList = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]

  viseurForm: FormGroup;
  columns = ['nomprenom', 'mail', 'departement', 'action'];
  enseignants: any[] = [];
  enseignant: any;
  confidentialites: any[] = [];
  etablissementConfidentialite: any;

  delegataireForm: FormGroup;
  delegataires: any[] = [];
  delegataire: any;

  validationLibelles: any = {};
  validationsActives: any[] = [];

  disableAutoSearch: boolean = false;
  hasPendingSearchViseur: boolean = false;
  hasPendingSearchDelegataire: boolean = false;

  private _destroy$ = new Subject<void>();

  @ViewChildren(MatExpansionPanel) pannels!: QueryList<MatExpansionPanel>;

  @Output() update = new EventEmitter<any>();

  constructor(
    private centreGestionService: CentreGestionService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private ldapService: LdapService,
    private enseignantService: EnseignantService,
    private configService: ConfigService,
    private accessibilityService: AccessibilityService,
    ) {
    this.viseurForm = this.fb.group({
      nom: [null, []],
      prenom: [null, []],
    });
    this.delegataireForm = this.fb.group({
      nom: [null, []],
      prenom: [null, []],
    });
  }

  ngOnInit(): void {
    this.loadDisableAutoSearchPref();

    this.configService.getConfigGenerale().subscribe((response: any) => {
      this.validationLibelles.validationPedagogique = response.validationPedagogiqueLibelle;
      this.validationLibelles.validationConvention = response.validationAdministrativeLibelle;
      if (this.centreGestion.id) {
        this.setFormData();
      }
    });
    this.viseurForm.valueChanges.pipe(debounceTime(1000)).subscribe(() => {
      if (this.disableAutoSearch) {
        this.hasPendingSearchViseur = this.hasViseurCriteria();
        if (!this.hasPendingSearchViseur) {
          this.enseignants = [];
        }
        return;
      }
      this.search();
    });
    this.getConfidentialites();
    this.getEtablissementConfidentialite();

    for (let validation of ['validationPedagogique', 'verificationAdministrative', 'validationConvention']) {
      this.form.get(validation)?.valueChanges.subscribe(val => {
        if (val) {
          this.addValidation(validation);
        } else {
          this.removeValidation(validation);
          // On désactive la vérification administrative
          if (validation === 'validationPedagogique') {
            this.form.get('verificationAdministrative')?.setValue(false);
          }
        }
      });
    }
    this.form.get('qualiteViseur')?.valueChanges.pipe(debounceTime(500)).subscribe((val) => {
      if (this.form.get('qualiteViseur')?.dirty) {
        this.centreGestion.qualiteViseur = val;
        this.update.emit(this.form.getRawValue());
      }
    });
    this.form.get('qualiteDelegataireViseur')?.valueChanges.pipe(debounceTime(500)).subscribe((val) => {
      if (this.form.get('qualiteDelegataireViseur')?.dirty) {
        this.centreGestion.qualiteDelegataireViseur = val;
        this.update.emit(this.form.getRawValue());
      }
    });
    this.delegataireForm.valueChanges.pipe(debounceTime(1000)).subscribe(() => {
      if (this.disableAutoSearch) {
        this.hasPendingSearchDelegataire = this.hasDelegataireCriteria();
        if (!this.hasPendingSearchDelegataire) {
          this.delegataires = [];
        }
        return;
      }
      this.searchDelegataire();
    });

    this.accessibilityService.disableAutoSearch$
      .pipe(takeUntil(this._destroy$))
      .subscribe((value) => {
        const previous = this.disableAutoSearch;
        this.disableAutoSearch = value;

        if (this.disableAutoSearch && !previous) {
          this.hasPendingSearchViseur = this.hasViseurCriteria();
          this.hasPendingSearchDelegataire = this.hasDelegataireCriteria();
        } else if (!this.disableAutoSearch && previous) {
          if (this.hasPendingSearchViseur) {
            this.runSearchViseur();
          }
          if (this.hasPendingSearchDelegataire) {
            this.runSearchDelegataire();
          }
        }
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  getConfidentialites() {
    this.centreGestionService.getConfidentialites().subscribe((response: any) => {
      this.confidentialites = response;
      if (this.centreGestion.id && this.centreGestion.niveauCentre.libelle !== 'ETABLISSEMENT') {
        this.confidentialites.pop();
      }
    });
  }

  getEtablissementConfidentialite() {
    this.centreGestionService.getEtablissementConfidentialite().subscribe((response: any) => {
      this.etablissementConfidentialite = response;
      if (this.centreGestion.id && this.centreGestion.niveauCentre.libelle !== 'ETABLISSEMENT' && this.etablissementConfidentialite.code != 2) {
        this.form.get('codeConfidentialite')?.disable();
      }
    });
  }

  setFormData(): void {
    this.form.setValue({
      codeConfidentialite: this.centreGestion.codeConfidentialite,
      saisieTuteurProParEtudiant: this.centreGestion.saisieTuteurProParEtudiant,
      autoriserImpressionConvention: this.centreGestion.autoriserImpressionConvention,
      conditionValidationImpression: this.centreGestion.conditionValidationImpression,
      autorisationEtudiantCreationConvention: this.centreGestion.autorisationEtudiantCreationConvention,
      validationPedagogique: this.centreGestion.validationPedagogique,
      verificationAdministrative: this.centreGestion.verificationAdministrative,
      validationConvention: this.centreGestion.validationConvention,
      validationPedagogiqueOrdre: this.centreGestion.validationPedagogiqueOrdre,
      verificationAdministrativeOrdre: this.centreGestion.verificationAdministrativeOrdre,
      validationConventionOrdre: this.centreGestion.validationConventionOrdre,
      recupInscriptionAnterieure: this.centreGestion.recupInscriptionAnterieure,
      dureeRecupInscriptionAnterieure: this.centreGestion.dureeRecupInscriptionAnterieure ?? 1,
      urlPageInstruction: this.centreGestion.urlPageInstruction,
      nomViseur: this.centreGestion.nomViseur,
      prenomViseur: this.centreGestion.prenomViseur,
      mailViseur: this.centreGestion.mailViseur,
      qualiteViseur: this.centreGestion.qualiteViseur,
      delaiAlerteConvention: this.centreGestion.delaiAlerteConvention,
      onlyMailCentreGestion: this.centreGestion.onlyMailCentreGestion,
      autoriserChevauchement : this.centreGestion.autoriserChevauchement,
      autoriserImpressionConventionApresCreationAvenant : this.centreGestion.autoriserImpressionConventionApresCreationAvenant,
      nomDelegataireViseur: this.centreGestion.nomDelegataireViseur,
      prenomDelegataireViseur: this.centreGestion.prenomDelegataireViseur,
      mailDelegataireViseur: this.centreGestion.mailDelegataireViseur,
      qualiteDelegataireViseur: this.centreGestion.qualiteDelegataireViseur,
    }, {
      emitEvent: false,
    });
    for (let validation of ['validationPedagogique', 'verificationAdministrative', 'validationConvention']) {
      if (this.centreGestion[validation]) {
        this.validationsActives.push({ id: validation, ordre: this.centreGestion[validation + 'Ordre'], libelle: this.validationLibelles[validation] ?? 'Vérification administrative'})
      }
    }
    if (this.validationsActives.length > 1) {
      this.validationsActives.sort((a, b) => {
        return a.ordre - b.ordre;
      });
    }
  }

  toggleRecupInscription(): void {
    if (!this.form.get('recupInscriptionAnterieure')?.value) {
      this.form.get('dureeRecupInscriptionAnterieure')?.setValue(1); // Limite de durée par défaut à 1 mois
    }
  }

  search(): void {
    if (!this.viseurForm.get('nom')?.value && !this.viseurForm.get('prenom')?.value) {
      this.messageService.setError(`Veuillez renseigner au moins l'un des critères`);
      return;
    this.hasPendingSearchViseur = false;
  }
    this.enseignant = undefined;
    this.ldapService.searchUsersByName(this.viseurForm.value.nom, this.viseurForm.value.prenom).subscribe((response: any) => {
      this.enseignants = response;
      if (this.enseignants.length === 1) {
        this.choose(this.enseignants[0]);
      }
    });
  }

  choose(row: any): void {
    if (this.pannels) {
      this.pannels.first.open();
    }
    this.enseignantService.getByUid(row.supannAliasLogin).subscribe((response: any) => {
      this.enseignant = response;
      if (this.enseignant == null){
        this.createEnseignant(row, 'viseur');
      }else{
        this.updateEnseignant(this.enseignant.id, row, 'viseur');
      }
    });
  }

  createEnseignant(row: any, type: 'viseur' | 'delegataire'): void {
    const data = {
      "nom": row.sn.join(' '),
      "prenom": row.givenName.join(' '),
      "mail": row.mail,
      "typePersonne": row.eduPersonPrimaryAffiliation,
      "uidEnseignant": row.uid,
      "tel": row.telephoneNumber,
    };

    this.enseignantService.create(data).subscribe((response: any) => {
      if (type === 'viseur') {
        this.enseignant = response;
        this.setViseur(this.enseignant);
      } else {
        this.delegataire = response;
        this.setDelegataire(this.delegataire);
      }
    });
  }

  updateEnseignant(id: number, row: any, type: 'viseur' | 'delegataire'): void {
    const data = {
      nom: row.sn.join(' '),
      prenom: row.givenName.join(' '),
      mail: row.mail,
      typePersonne: row.eduPersonPrimaryAffiliation,
      uidEnseignant: row.uid,
      tel: row.telephoneNumber,
    };

    this.enseignantService.update(id, data).subscribe((response: any) => {
      if (type === 'viseur') {
        this.enseignant = response;
        this.setViseur(this.enseignant);
      } else {
        this.delegataire = response;
        this.setDelegataire(this.delegataire);
      }
    });
  }

  setViseur(enseignant: any) {
    this.form.get('nomViseur')?.setValue(enseignant.nom);
    this.form.get('prenomViseur')?.setValue(enseignant.prenom);
    this.form.get('mailViseur')?.setValue(enseignant.mail);
    this.form.get('qualiteViseur')?.reset();

    this.centreGestion.nomViseur = enseignant.nom;
    this.centreGestion.prenomViseur = enseignant.prenom;
    this.centreGestion.mailViseur = enseignant.mail;
    this.centreGestion.qualiteViseur = null;

    this.form.markAsDirty();
    this.update.emit(this.form.getRawValue());
  }

  resetViseur() {
    this.form.get('nomViseur')?.reset();
    this.form.get('prenomViseur')?.reset();
    this.form.get('mailViseur')?.reset();
    this.form.get('qualiteViseur')?.reset();

    this.centreGestion.nomViseur = null;
    this.centreGestion.prenomViseur = null;
    this.centreGestion.mailViseur = null;
    this.centreGestion.qualiteViseur = null;

    this.form.markAsDirty();
    this.update.emit(this.form.getRawValue());

    this.resetDelegataire() // reset aussi le délégataire, car lié au viseur
  }

  compareCode(option: any, value: any): boolean {
    if (option && value) {
      return option.code === value.code;
    }
    return false;
  }

  dropValidation(event: CdkDragDrop<string[]>): void {
    moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    this.reorderValidations();
  }

  reorderValidations(): void {
    let ordre = 1;
    this.validationsActives.map((v: any) => v.ordre = ordre++);
    let lastOrdre = 0;
    this.validationsActives.forEach((v: any) => {
      this.form.get(v.id + 'Ordre')?.setValue(v.ordre);
      lastOrdre = v.ordre;
    });
    for (let validation of ['validationPedagogique', 'verificationAdministrative', 'validationConvention']) {
      if (!this.form.get(validation)?.value) {
        this.form.get(validation + 'Ordre')?.setValue(++lastOrdre);
      }
    }
  }

  addValidation(validation: string): void {
    const ordres = this.validationsActives.map((v: any) => v.ordre);
    let lastOrdre = 0;
    if (ordres.length > 0) {
      lastOrdre = Math.max.apply(Math, ordres);
    }
    this.validationsActives.push({ id: validation, ordre: lastOrdre + 1, libelle: this.validationLibelles[validation] ?? 'Vérification administrative'})
    this.reorderValidations();
  }

  removeValidation(validation: string): void {
    const index = this.validationsActives.findIndex((v: any) => v.id === validation);
    if (index > -1) {
      this.validationsActives.splice(index, 1);
      this.reorderValidations();
    }
  }

  searchDelegataire(): void {
    if (!this.delegataireForm.get('nom')?.value && !this.delegataireForm.get('prenom')?.value) {
      this.messageService.setError(`Veuillez renseigner au moins l'un des critères`);
      return;
    this.hasPendingSearchDelegataire = false;
  }
    this.delegataire = undefined;
    this.ldapService.searchUsersByName(this.delegataireForm.value.nom, this.delegataireForm.value.prenom).subscribe((response: any) => {
      this.delegataires = response;
      if (this.delegataires.length === 1) {
        this.chooseDelegataire(this.delegataires[0]);
      }
    });
  }


  chooseDelegataire(row: any): void {
    if (!this.hasViseur()) {
      this.messageService.setError(`Vous devez d'abord sélectionner un viseur avant de définir un délégataire`);
      return;
    }
    if (this.pannels) {
      this.pannels.first.open();
    }
    this.enseignantService.getByUid(row.supannAliasLogin).subscribe((response: any) => {
      this.delegataire = response;
      if (this.delegataire == null){
        this.createEnseignant(row, 'delegataire');
      }else{
        this.updateEnseignant(this.delegataire.id, row, 'delegataire');
      }
    });
  }

  setDelegataire(enseignant: any) {
    this.form.get('nomDelegataireViseur')?.setValue(enseignant.nom);
    this.form.get('prenomDelegataireViseur')?.setValue(enseignant.prenom);
    this.form.get('mailDelegataireViseur')?.setValue(enseignant.mail);
    this.form.get('qualiteDelegataireViseur')?.reset();

    this.centreGestion.nomDelegataireViseur = enseignant.nom;
    this.centreGestion.prenomDelegataireViseur = enseignant.prenom;
    this.centreGestion.mailDelegataireViseur = enseignant.mail;
    this.centreGestion.qualiteDelegataireViseur = null;

    this.form.markAsDirty();
    this.update.emit(this.form.getRawValue());
  }

  resetDelegataire() {
    this.form.get('nomDelegataireViseur')?.reset();
    this.form.get('prenomDelegataireViseur')?.reset();
    this.form.get('mailDelegataireViseur')?.reset();
    this.form.get('qualiteDelegataireViseur')?.reset();

    this.centreGestion.nomDelegataireViseur = null;
    this.centreGestion.prenomDelegataireViseur = null;
    this.centreGestion.mailDelegataireViseur = null;
    this.centreGestion.qualiteDelegataireViseur = null;

    this.form.markAsDirty();
    this.update.emit(this.form.getRawValue());
  }

  isDisabledChoose(): boolean {
    return !this.hasViseur();
  }

  hasViseur(): boolean {
    return !!this.form.get('nomViseur')?.value;
  }
  runSearchViseur(): void {
    if (!this.hasViseurCriteria()) {
      this.hasPendingSearchViseur = false;
      return;
    }
    this.search();
  }

  runSearchDelegataire(): void {
    if (!this.hasDelegataireCriteria()) {
      this.hasPendingSearchDelegataire = false;
      return;
    }
    this.searchDelegataire();
  }

  private hasViseurCriteria(): boolean {
    const nom = (this.viseurForm.get('nom')?.value || '').toString().trim();
    const prenom = (this.viseurForm.get('prenom')?.value || '').toString().trim();
    return nom.length > 0 || prenom.length > 0;
  }

  private hasDelegataireCriteria(): boolean {
    const nom = (this.delegataireForm.get('nom')?.value || '').toString().trim();
    const prenom = (this.delegataireForm.get('prenom')?.value || '').toString().trim();
    return nom.length > 0 || prenom.length > 0;
  }

  private loadDisableAutoSearchPref(): void {
    const saved = localStorage.getItem('accessibilityPreferences');
    if (!saved) return;
    try {
      const prefs = JSON.parse(saved);
      this.disableAutoSearch = !!prefs?.disableAutoSearch;
    } catch {}
  }

  /**
   * Déplace une validation vers le haut dans la liste (alternative accessible)
   * @param index Index actuel de la validation
   */
  moveValidationUp(index: number): void {
    if (index > 0) {
      moveItemInArray(this.validationsActives, index, index - 1);
      this.reorderValidations();
    }
  }

  /**
   * Déplace une validation vers le bas dans la liste (alternative accessible)
   * @param index Index actuel de la validation
   */
  moveValidationDown(index: number): void {
    if (index < this.validationsActives.length - 1) {
      moveItemInArray(this.validationsActives, index, index + 1);
      this.reorderValidations();
    }
  }
}


