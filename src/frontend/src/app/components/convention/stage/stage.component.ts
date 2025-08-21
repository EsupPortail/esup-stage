import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { PaysService } from "../../../services/pays.service";
import { ThemeService } from "../../../services/theme.service";
import { LangueConventionService } from "../../../services/langue-convention.service";
import { ModeVersGratificationService } from "../../../services/mode-vers-gratification.service";
import { UniteDureeService } from "../../../services/unite-duree.service";
import { UniteGratificationService } from "../../../services/unite-gratification.service";
import { DeviseService } from "../../../services/devise.service";
import { TempsTravailService } from "../../../services/temps-travail.service";
import { OrigineStageService } from "../../../services/origine-stage.service";
import { NatureTravailService } from "../../../services/nature-travail.service";
import { ModeValidationStageService } from "../../../services/mode-validation-stage.service";
import { TypeConventionService } from "../../../services/type-convention.service";
import { PeriodeInterruptionStageService } from "../../../services/periode-interruption-stage.service";
import { AuthService } from "../../../services/auth.service";
import { ContenuService } from "../../../services/contenu.service";
import { ConventionService } from "../../../services/convention.service";
import { debounceTime } from 'rxjs/operators'
import { CalendrierComponent } from './calendrier/calendrier.component';
import { InterruptionsFormComponent } from './interruptions-form/interruptions-form.component';
import { PeriodeStageService } from'../../../services/periode-stage.service';

@Component({
  selector: 'app-stage',
  templateUrl: './stage.component.html',
  styleUrls: ['./stage.component.scss']
})
export class StageComponent implements OnInit {

  fieldValidators : any = {
    'nbHeuresHebdo': [Validators.required, Validators.pattern('[0-9]{1,2}([,.][0-9]{1,2})?')],
    'dureeExceptionnelle': [Validators.required, Validators.pattern('[0-9]+([,.][0-9]{1,2})?')],
    'montantGratification': [Validators.required, Validators.pattern('[0-9]{1,10}([,.][0-9]{1,2})?')],
    'sujetStage': [Validators.required],
    'competences': [Validators.required],
    'fonctionsEtTaches': [Validators.required],
    'idUniteGratification': [Validators.required],
    'idUniteDuree': [Validators.required],
    'idDevise': [Validators.required],
    'idModeVersGratification': [Validators.required],
    'idOrigineStage': [Validators.required],
    'confidentiel': [Validators.required],
    'idNatureTravail': [Validators.required],
    'idModeValidationStage': [Validators.required],
  }
  interruptionsStageTableColumns = ['dateDebutInterruption', 'dateFinInterruption', 'actions'];

  countries: any[] = [];
  thematiques: any[] = [];
  langueConventions: any[] = [];
  modeVersGratifications: any[] = [];
  uniteDurees: any[] = [];
  uniteGratifications: any[] = [];
  devises: any[] = [];
  tempsTravails: any[] = [];
  origineStages: any[] = [];
  natureTravails: any[] = [];
  modeValidationStages: any[] = [];
  typeConventions: any[] = [];
  interruptionsStage: any[] = [];
  periodesCalculHeuresStage : any[] = [];
  joursFeries : any[] = [];

  dureeStage = {
    dureeMois: 0,
    dureeJours: 0,
    dureeHeures: 0
  };

  @Input() convention: any;
  @Input() groupeConvention: any;

  form!: FormGroup;

  texteLimiteRenumeration: string = '';

  periodesInterruptionsValid:boolean = false;
  minDateDebutStage!: Date;
  maxDateDebutStage!: Date;
  minDateFinStage!: Date;
  maxDateFinStage!: Date;
  previousValues: any;
  singleFieldUpdateLock: boolean = false;
  singleFieldUpdateQueue : any[] = [];
  updatingPeriode = false;
  initialLoading: boolean = true;

  @Output() validated = new EventEmitter<number>();
  @Output() updateField = new EventEmitter<any>();

  @Input() modifiable!: boolean;
  @Input() enMasse!: boolean;

  constructor(public conventionService: ConventionService,
              private fb: FormBuilder,
              private authService: AuthService,
              private contenuService: ContenuService,
              private paysService: PaysService,
              private themeService: ThemeService,
              private langueConventionService: LangueConventionService,
              private modeVersGratificationService: ModeVersGratificationService,
              private uniteDureeService: UniteDureeService,
              private uniteGratificationService: UniteGratificationService,
              private deviseService: DeviseService,
              private tempsTravailService: TempsTravailService,
              private origineStageService: OrigineStageService,
              private natureTravailService: NatureTravailService,
              private modeValidationStageService: ModeValidationStageService,
              private typeConventionService: TypeConventionService,
              private periodeInterruptionStageService: PeriodeInterruptionStageService,
              private periodeStageService : PeriodeStageService,
              public matDialog: MatDialog,
  ) {
  }

  ngOnInit(): void {

    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServPays: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.countries = response.data;
    });
    this.themeService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.thematiques = response.data.sort((a: { libelle: string; }, b: { libelle: any; }) =>
        a.libelle.localeCompare(b.libelle, 'fr', { sensitivity: 'base' })
      );
    });
    this.langueConventionService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.langueConventions = response.data;
    });
    this.modeVersGratificationService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.modeVersGratifications = response.data;
    });
    this.uniteDureeService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.uniteDurees = response.data;
    });
    this.uniteGratificationService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.uniteGratifications = response.data;
    });
    this.deviseService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.devises = response.data;
    });
    this.tempsTravailService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.tempsTravails = response.data;
    });
    this.origineStageService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.origineStages = response.data;
    });
    this.typeConventionService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.typeConventions = response.data;
    });
    this.natureTravailService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.natureTravails = response.data;
    });
    this.modeValidationStageService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.modeValidationStages = response.data;
    });

    if (this.enMasse && this.groupeConvention) {
      this.convention = this.mergeObject(this.convention, this.groupeConvention);
    }

    this.setDureeStageFromExceptionnelle();

    this.form = this.fb.group({
      // - Modèle de la convention
      idPays: [this.convention.paysConvention ? this.convention.paysConvention.id : null, [Validators.required]],
      // - Description du stage
      idTheme: [this.convention.theme ? this.convention.theme.id : null, [Validators.required]],
      sujetStage: [this.convention.sujetStage],
      competences: [this.convention.competences],
      fonctionsEtTaches: [this.convention.fonctionsEtTaches],
      details: [this.convention.details],
      // - Partie Dates / horaires
      dateDebutStage: [this.convention.dateDebutStage ? new Date(this.convention.dateDebutStage) : null, [Validators.required]],
      dateFinStage: [this.convention.dateFinStage ? new Date(this.convention.dateFinStage) : null, [Validators.required]],
      interruptionStage: [this.convention.interruptionStage, [Validators.required]],
      horairesReguliers: [this.convention.horairesReguliers, [Validators.required]],
      nbHeuresHebdo: [this.convention.nbHeuresHebdo, this.fieldValidators['nbHeuresHebdo']],
      nbConges: [this.convention.nbConges],
      dureeExceptionnelle: [this.convention.dureeExceptionnelle, this.fieldValidators['dureeExceptionnelle']],
      idTempsTravail: [this.convention.tempsTravail ? this.convention.tempsTravail.id : null, [Validators.required]],
      commentaireDureeTravail: [this.convention.commentaireDureeTravail],
      periodeStageMois: [this.dureeStage.dureeMois, [Validators.min(0), Validators.max(30),Validators.required]],
      periodeStageJours: [this.dureeStage.dureeJours, [Validators.min(0), Validators.max(31),Validators.required]],
      periodeStageHeures: [this.dureeStage.dureeHeures, [Validators.min(0), Validators.max(23),Validators.required]],
      // - Partie Gratification
      gratificationStage: [this.convention.gratificationStage, [Validators.required]],
      montantGratification: [this.convention.montantGratification, this.fieldValidators['montantGratification']],
      idUniteGratification: [this.convention.uniteGratification ? this.convention.uniteGratification.id : null, this.fieldValidators['idUniteGratification']],
      idUniteDuree: [this.convention.uniteDureeGratification ? this.convention.uniteDureeGratification.id : null, this.fieldValidators['idUniteDuree']],
      idDevise: [this.convention.devise ? this.convention.devise.id : null, this.fieldValidators['idDevise']],
      idModeVersGratification: [this.convention.modeVersGratification ? this.convention.modeVersGratification.id : null, this.fieldValidators['idModeVersGratification']],
      //TODO un bandeau doit permettre de mettre un message à l’attention de l’étudiant
      // - Partie Divers
      idOrigineStage: [this.convention.origineStage ? this.convention.origineStage.id : null],
      confidentiel: [this.convention.confidentiel],
      idNatureTravail: [this.convention.natureTravail ? this.convention.natureTravail.id : null],
      idModeValidationStage: [this.convention.modeValidationStage ? this.convention.modeValidationStage.id : null],
      modeEncadreSuivi: [this.convention.modeEncadreSuivi],
      avantagesNature: [this.convention.avantagesNature],
      travailNuitFerie: [this.convention.travailNuitFerie],
    }, { emitEvent: false });

    this.form.get('periodeStageMois')!.valueChanges.subscribe(value => {
      this.dureeStage.dureeMois = value;
    });

    this.form.get('periodeStageJours')!.valueChanges.subscribe(value => {
      this.dureeStage.dureeJours = value;
    });

    this.form.get('periodeStageHeures')!.valueChanges.subscribe(value => {
      this.dureeStage.dureeHeures = value;
    });

    this.form.get('nbHeuresHebdo')!.valueChanges.subscribe((value) => {
      this.convention.nbHeuresHebdo = value;
    });

    this.form.get('dureeExceptionnelle')!.valueChanges.subscribe((value) => {
      this.convention.dureeExceptionnelle = value;
      if (!this.initialLoading) {
        this.calculPeriode(this.convention.nbHeuresHebdo, this.convention.dureeExceptionnelle);

        this.form.patchValue({
          periodeStageMois: this.dureeStage.dureeMois,
          periodeStageJours: this.dureeStage.dureeJours,
          periodeStageHeures: this.dureeStage.dureeHeures
        }, { emitEvent: false });
      }
    });

    //Set default value for booleans
    if (this.convention.interruptionStage == null){
      this.form.get('interruptionStage')?.setValue(false);
    }
    if (this.convention.horairesReguliers == null){
      this.form.get('horairesReguliers')?.setValue(true);
    }
    if (this.convention.gratificationStage == null){
      this.form.get('gratificationStage')?.setValue(false);
    }
    if (this.convention.confidentiel == null){
      this.form.get('confidentiel')?.setValue(false);
    }
    //Update validators that depends on booleans
    this.toggleValidators(['nbHeuresHebdo',],this.convention.horairesReguliers);
    this.toggleValidators(['montantGratification','idUniteGratification','idUniteDuree','idDevise','idModeVersGratification'],this.convention.gratificationStage);
    this.toggleValidators(['sujetStage','competences','fonctionsEtTaches','idOrigineStage','confidentiel','idNatureTravail','idModeValidationStage'],!this.enMasse);

    this.loadInterruptionsStage();
    if (!this.form.get('horairesReguliers')?.value) {
      this.loadPeriodesStage();
    }

    this.previousValues={...this.form.value}
    this.form.valueChanges.pipe(debounceTime(1000)).subscribe(res=>{
      const keys=Object.keys(res).filter(k=>res[k]!=this.previousValues[k])
      this.previousValues={...this.form.value}
      keys.forEach((key: string) => {
        if (['interruptionStage','horairesReguliers','nbHeuresHebdo'].includes(key)){
          this.updateHeuresTravail();
        }
        // controle du chevauchement avant mise à jour
        if (['dateDebutStage','dateFinStage'].includes(key)) {
          this.conventionService.controleChevauchement(this.convention.id, this.form.get('dateDebutStage')!.value, this.form.get('dateFinStage')!.value).subscribe((response) => {
            if (!response) {
              this.updateHeuresTravail();
              this.updateSingleField(key,res[key]);
            } else {
              this.form.get(key)!.setErrors({dateStageChevauchement: true});
            }
          });
        } else {
          this.updateSingleField(key,res[key]);
        }
      });
    })

    if (!this.modifiable) {
      this.form.disable();
    }

    this.loadJoursFeries();

    this.contenuService.get('TEXTE_LIMITE_RENUMERATION').subscribe((response: any) => {
      this.texteLimiteRenumeration = response.texte;
    })

    //controles uniquement pour les non gestionnaires
    if (!this.isGestionnaire()) {
      this.minDateDebutStage = new Date(new Date().getTime() + (1000 * 60 * 60 * 24));
      this.maxDateDebutStage = new Date(new Date().getFullYear()+1, 7, 31);
    }

    if (this.convention.dateDebutStage){
      this.updateDateFinBounds(new Date(this.convention.dateDebutStage));
    }else{
      this.minDateFinStage = new Date(new Date().getFullYear()-1, 0, 2);
      this.maxDateFinStage = new Date(new Date().getFullYear()+2, 0, 1);
    }

    //le timeout permet de laisser le temps aux données d'être chargées
    setTimeout(() => {
      this.initialLoading = false;
    }, 1000);
  }

  ngOnChanges(): void{
    if (this.form) {
      this.form.patchValue({
        idPays: this.convention.paysConvention ? this.convention.paysConvention.id : null,
      }, {emitEvent: false});
    }
    this.singleFieldUpdateLock = false;
    if(this.singleFieldUpdateQueue.length > 0){
      const data = this.singleFieldUpdateQueue.pop();
      this.updateSingleField(data.field,data.value);
    }
  }

  mergeObject(mainObject: any, objectToMerge: any): any {
    Object.keys(objectToMerge).forEach(key => {
      if (mainObject[key] === null) mainObject[key] = objectToMerge[key]
    })
    return mainObject
  }

  isEtudiant(): boolean {
    return this.authService.isEtudiant();
  }

  isGestionnaire(): boolean {
    return this.authService.isGestionnaire() || this.authService.isAdmin();
  }

  updateSingleField(key: string,value: any): void {
    if (this.form.get(key)!.valid) {
      const data = {
        "field":key,
        "value":value,
      };
      if (!this.singleFieldUpdateLock){
        this.singleFieldUpdateLock = true;
        this.updateField.emit(data);
        if (key === 'dureeExceptionnelle' || key === 'periodeStageMois' || key === 'periodeStageJours' || key === 'periodeStageHeures') {
          this.updateDureeStage();
        }
      }else{
        this.singleFieldUpdateQueue.push(data);
      }
    }
    this.validateForm();
  }

  validateForm() : void{
    let status = 0;
    if (Object.keys(this.form.value).some(k => !!this.form.value[k])) status = 1;
    if ((this.form.valid || this.form.disabled) && this.periodesInterruptionsValid) status = 2;
    this.validated.emit(status);
  }

  toggleValidators(keys: string[],toggle: boolean): void {
    keys.forEach((key: string) => {
      if (toggle){
        this.form.get(key)!.setValidators(this.fieldValidators[key]);
      }else{
        this.form.get(key)!.clearValidators();
      }
      this.form.get(key)!.updateValueAndValidity();
    });
  }

  checkInterruptionsPeriodesValid():void {
    if (this.form.get('interruptionStage')?.value){
      if (this.interruptionsStage.length >= 1){
        this.periodesInterruptionsValid = true;
      }else{
        this.periodesInterruptionsValid = false;
      }
    }else{
      this.periodesInterruptionsValid = true;
    }
    this.validateForm();

  }

  setHorairesReguliersFormControls(event: any): void {
    this.toggleValidators(['nbHeuresHebdo'], event.value);
    if (!event.value) {
      this.loadPeriodesStage();
    } else {
      if (this.periodesCalculHeuresStage.length > 0) {
        this.deletePeriodeStageByConvention();
      }
      this.periodesCalculHeuresStage = [];
      this.updateHeuresTravail();
    }
  }

  setGratificationStageFormControls(event : any): void {
    this.toggleValidators(['montantGratification','idUniteGratification','idUniteDuree','idDevise','idModeVersGratification'],event.value);
  }

  dateDebutChanged(event: any): void {
    this.updateDateFinBounds(event.value);
  }

  updateDateFinBounds(dateDebut: Date): void {
    this.minDateFinStage = new Date(dateDebut);
    this.maxDateFinStage = new Date(dateDebut.getTime() + (1000 * 60 * 60 * 24 * 365));
    this.form.get('dateFinStage')!.markAsTouched();
    this.form.get('dateFinStage')!.updateValueAndValidity();
  }

  openInterruptionsCreateFormModal(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {convention: this.convention,interruptionsStage: this.interruptionsStage,interruptionStage: null};
    const modalDialog = this.matDialog.open(InterruptionsFormComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        this.addInterruptionsStage(dialogResponse);
      }
    });
  }

  openInterruptionsEditFormModal(row: any): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {convention: this.convention,interruptionsStage: this.interruptionsStage, interruptionStage: row, periodes:null};
    const modalDialog = this.matDialog.open(InterruptionsFormComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        this.editInterruptionStage(row.id, dialogResponse);
      }
    });
  }

  loadInterruptionsStage() : void{
    this.periodeInterruptionStageService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.interruptionsStage = response;
      this.checkInterruptionsPeriodesValid();
    });
  }

  refreshInterruptionsStage() : void{
    this.periodeInterruptionStageService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.interruptionsStage = response;
      this.updateHeuresTravail();
      this.checkInterruptionsPeriodesValid();
    });
  }

  addInterruptionsStage(newInterruptions: any[]){
    let finished = 0;
    for (const newInterruption of newInterruptions){
      this.periodeInterruptionStageService.create(newInterruption).subscribe((response: any) => {
        finished++;
        if (finished === newInterruptions.length){
          this.refreshInterruptionsStage();
        }
      });
    }
  }

  editInterruptionStage(id:number, data:any) : void{
    this.periodeInterruptionStageService.update(id,data).subscribe((response: any) => {
      this.refreshInterruptionsStage();
    });
  }

  deleteInterruptionStage(row:any) : void{
    this.periodeInterruptionStageService.delete(row.id).subscribe((response: any) => {
      this.refreshInterruptionsStage();
    });
  }

  deleteInterruptionStageByConvention(): void {
    if (this.interruptionsStage.length >= 1) {
      this.periodeInterruptionStageService.deleteByConvention(this.convention.id).subscribe((response: any) => {
        this.refreshInterruptionsStage();
      });
    }
  }

  loadPeriodesStage() : void {
    this.periodeStageService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.periodesCalculHeuresStage = response;
      this.updateHeuresTravail();
    });
  }

  refreshPeriodesStage() : void {
    this.periodeStageService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.periodesCalculHeuresStage = response;
      this.updateHeuresTravail();
    });
  }

  deletePeriodeStage(index: number): void {
    const periodeToDelete = this.periodesCalculHeuresStage[index];
    this.periodeStageService.delete(periodeToDelete.id).subscribe({
      next: () => {
        const periodesUpdated = [...this.periodesCalculHeuresStage];
        periodesUpdated.splice(index, 1);
        this.periodesCalculHeuresStage = periodesUpdated;
        this.updateHeuresTravail();
      },
      error: (error) => {
        console.error("Error deleting period:", error);
      }
    });
  }

  deletePeriodeStageByConvention(): void {
    if (this.periodesCalculHeuresStage.length >= 1) {
      this.periodeStageService.deleteByConvention(this.convention.id).subscribe((response: any) => {
        this.refreshPeriodesStage();
      });
    }
  }

  openCalendar(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {
      convention: this.convention,
      interruptionsStage: this.interruptionsStage,
      periodes: this.periodesCalculHeuresStage,
      joursFeries: this.joursFeries
    };

    const modalDialog = this.matDialog.open(CalendrierComponent, dialogConfig);

    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        // Handle irregular hours
        if (this.form.get('horairesReguliers')?.value === false) {
          // Handle the case where there are new periods
          if (dialogResponse.length > 0) {
            const newPeriodes = dialogResponse.map((periode: any) => {
              return {
                id: periode.id, // Include id if it exists
                idConvention: this.convention.id,
                dateDebut: periode.dateDebut,
                dateFin: periode.dateFin,
                nbHeuresJournalieres: parseFloat(periode.nbHeuresJournalieres)
              };
            });
            this.savePeriodesStage(newPeriodes);
          } else {
            // Handle the case where there are no periods
            this.periodesCalculHeuresStage = [];
            this.updateHeuresTravail();
          }
        } else {
          // For regular hours, just update the calculation
          this.periodesCalculHeuresStage = dialogResponse;
          this.updateHeuresTravail();
        }
      }
    });
  }

  savePeriodesStage(periodes: any[]): void {
    let finished = 0;
    for (const periode of periodes) {
      if (periode.id) {
        this.periodeStageService.update(periode.id, periode).subscribe((response: any) => {
          finished++;
          if (finished === periodes.length) {
            this.refreshPeriodesStage();
          }
        });
      } else {
        this.periodeStageService.create(periode).subscribe((response: any) => {
          finished++;
          if (finished === periodes.length) {
            this.refreshPeriodesStage();
          }
        });
      }
    }
  }

  loadJoursFeries():void {
    let anneeUniversitaire = this.convention.annee;
    const currentYear = (anneeUniversitaire != null && anneeUniversitaire !== '') ? parseInt(anneeUniversitaire.split('/')[0]) : new Date().getFullYear();
    const nextYear = currentYear+1;

    this.joursFeries = this.getJoursFeries(currentYear);
    const nexYearJoursFeries = this.getJoursFeries(nextYear);

    this.joursFeries.push(...nexYearJoursFeries);
  }

  getJoursFeries(annee: number){

    let JourAn = new Date(annee, 0, 1);
    let FeteTravail = new Date(annee, 4, 1);
    let Victoire1945 = new Date(annee, 4, 8);
    let FeteNationale = new Date(annee,6, 14);
    let Assomption = new Date(annee, 7, 15);
    let Toussaint = new Date(annee, 10, 1);
    let Armistice = new Date(annee, 10, 11);
    let Noel = new Date(annee, 11, 25);
    //**let SaintEtienne = new Date(annee, 11, 26);**//

    let G = annee%19;
    let C = Math.floor(annee/100);
    let H = (C - Math.floor(C/4) - Math.floor((8*C+13)/25) + 19*G + 15)%30;
    let I = H - Math.floor(H/28)*(1 - Math.floor(H/28)*Math.floor(29/(H + 1))*Math.floor((21 - G)/11));
    let J = (annee*1 + Math.floor(annee/4) + I + 2 - C + Math.floor(C/4))%7;
    let L = I - J;
    let MoisPaques = 3 + Math.floor((L + 40)/44);
    let JourPaques = L + 28 - 31*Math.floor(MoisPaques/4);
    let Paques = new Date(annee, MoisPaques-1, JourPaques);
    //**let VendrediSaint = new Date(annee, MoisPaques-1, JourPaques-2);**//
    let LundiPaques = new Date(annee, MoisPaques-1, JourPaques+1);
    let Ascension = new Date(annee, MoisPaques-1, JourPaques+39);
    let Pentecote = new Date(annee, MoisPaques-1, JourPaques+49);
    let LundiPentecote = new Date(annee, MoisPaques-1, JourPaques+50);

    //**SaintEtienne et Vendredi Saint sont des fetes exclusivement**//
    //**alscacienne. On les ignore dans notre cas.**//
    return new Array(JourAn, Paques, LundiPaques, FeteTravail, Victoire1945, Ascension, Pentecote, LundiPentecote, FeteNationale, Assomption, Toussaint, Armistice, Noel);
  }

  dateFromBackend(dateString: string | Date): Date {
    if (!dateString) {
      return new Date();
    }

    let date: Date;
    if (dateString instanceof Date) {
      date = new Date(dateString.getTime());
    } else {
      date = new Date(dateString);
    }

    // Normalize to start of day
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
  }

  updateHeuresTravail(): void {
    if (this.initialLoading) return;

    this.updatingPeriode = true;
    setTimeout(() => {
      this.updatingPeriode = false;
    }, 2000);

    // Check if form controls are valid
    if (!this.form.get('dateDebutStage')?.value || !this.form.get('dateFinStage')?.value) {
      return;
    }

    if (this.form.get('horairesReguliers')!.value) {
      // For regular hours
      if (this.form.get('nbHeuresHebdo')?.valid) {
        const dateDebutStage = this.dateFromBackend(this.form.get('dateDebutStage')!.value);
        const dateFinStage = this.dateFromBackend(this.form.get('dateFinStage')!.value);
        const nbHeuresHebdo = parseFloat(this.form.get('nbHeuresHebdo')!.value);
        const nbHeuresJournalieres = nbHeuresHebdo / 5;

        const periodes = [{
          'dateDebut': dateDebutStage,
          'dateFin': dateFinStage,
          'nbHeuresJournalieres': nbHeuresJournalieres
        }];

        // Calculate total hours
        const totalHeures = this.calculHeuresTravails(periodes);
        this.form.get('dureeExceptionnelle')?.setValue(totalHeures);
      }
    } else {
      // For irregular hours
      const totalHeures = this.calculHeuresTravails(this.periodesCalculHeuresStage);
      this.form.get('dureeExceptionnelle')?.setValue(totalHeures);
    }
  }

  calculHeuresTravails(periodes: any[]): number {
    let heuresTravails = 0;
    if (!periodes || periodes.length === 0) {
      return 0;
    }

    for (const periode of periodes) {
      if (!periode.dateDebut || !periode.dateFin || periode.nbHeuresJournalieres === undefined) {
        continue;
      }

      // Ensure we're working with Date objects
      let startDate = new Date(periode.dateDebut);
      const endDate = new Date(periode.dateFin);
      const nbHeuresJournalieres = parseFloat(periode.nbHeuresJournalieres);

      // Normalize dates to start of day to avoid time comparison issues
      startDate = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
      const normalizedEndDate = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate());

      // Create an array of interruption date ranges for faster checking
      const interruptionRanges = [];
      if (this.form.get('interruptionStage')!.value && this.interruptionsStage) {
        for (const interruption of this.interruptionsStage) {
          const startInterruption = new Date(interruption.dateDebutInterruption);
          const endInterruption = new Date(interruption.dateFinInterruption);
          interruptionRanges.push({
            start: new Date(startInterruption.getFullYear(), startInterruption.getMonth(), startInterruption.getDate()),
            end: new Date(endInterruption.getFullYear(), endInterruption.getMonth(), endInterruption.getDate())
          });
        }
      }

      // Map jours fériés to normalized dates for faster checking
      const joursFeriesMap = new Set();
      for (const jourFerie of this.joursFeries) {
        joursFeriesMap.add(new Date(jourFerie.getFullYear(), jourFerie.getMonth(), jourFerie.getDate()).getTime());
      }

      while (startDate <= normalizedEndDate) {
        let valid = true;
        const dayOfWeek = startDate.getDay();

        // Skip weekends
        if (dayOfWeek === 6 || dayOfWeek === 0) {
          valid = false;
        }

        // Skip holidays
        if (valid && joursFeriesMap.has(startDate.getTime())) {
          valid = false;
        }

        // Skip interruption periods
        if (valid && this.form.get('interruptionStage')!.value) {
          for (const range of interruptionRanges) {
            if (startDate >= range.start && startDate <= range.end) {
              valid = false;
              break;
            }
          }
        }

        if (valid) {
          heuresTravails += nbHeuresJournalieres;
        }

        // Move to next day
        startDate = new Date(startDate.setDate(startDate.getDate() + 1));
      }
    }

    return Math.round(heuresTravails * 100) / 100;
  }

  setDureeStageFromExceptionnelle(): void {
    if (this.convention.dureeExceptionnellePeriode != null) {
      const matches = this.convention.dureeExceptionnellePeriode.match(/(\d+)\s*mois\s*(\d+)\s*jour\(s\)\s*(\d+\.?\d*)\s*heure\(s\)/);
      if (matches) {
        this.dureeStage = {
          dureeMois: parseInt(matches[1], 10),
          dureeJours: parseInt(matches[2], 10),
          dureeHeures: parseFloat(matches[3])
        };
        if (this.form) {
          this.form.patchValue({
            periodeStageMois: this.dureeStage.dureeMois,
            periodeStageJours: this.dureeStage.dureeJours,
            periodeStageHeures: this.dureeStage.dureeHeures
          });
        }
      }
    }
  }

  updateDureeStage(): void {
    const dureeString = `${this.dureeStage.dureeMois} mois ${this.dureeStage.dureeJours} jour(s) ${this.dureeStage.dureeHeures} heure(s)`;
    this.conventionService.updatePeriodes(this.convention.id, dureeString)
      .subscribe({
        next: (response) => {
          this.convention.dureeExceptionnellePeriode = dureeString;
        },
      });
  }

  calculPeriode(nbHeuresHebdo: number, nbHeures: number): void {
    if (!nbHeures || isNaN(nbHeures)) {
      this.dureeStage = { dureeMois: 0, dureeJours: 0, dureeHeures: 0 };
      return;
    }

    const parsedNbHeures = parseFloat(nbHeures.toString());

    // Calculer selon le type d'horaires
    if (!this.form.get('horairesReguliers')?.value && this.periodesCalculHeuresStage && this.periodesCalculHeuresStage.length > 0) {
      this.calculPeriodeIrreguliere(parsedNbHeures);
    } else {
      this.calculPeriodeReguliere(nbHeuresHebdo, parsedNbHeures);
    }
  }

  private calculPeriodeReguliere(nbHeuresHebdo: number, nbHeures: number): void {
    const NB_JOUR_MOIS = 22; // 1 mois = 22 jours ouvrés
    const NB_JOUR_SEMAINE = 5; // 1 semaine = 5 jours ouvrés

    if (!nbHeuresHebdo || isNaN(nbHeuresHebdo)) {
      this.dureeStage = { dureeMois: 0, dureeJours: 0, dureeHeures: nbHeures };
      return;
    }

    const parsedNbHeuresHebdo = parseFloat(nbHeuresHebdo.toString());
    const nbHeuresJournalieres = parsedNbHeuresHebdo / NB_JOUR_SEMAINE;

    if (nbHeuresJournalieres === 0) {
      this.dureeStage = { dureeMois: 0, dureeJours: 0, dureeHeures: nbHeures };
      return;
    }

    const totalJours = nbHeures / nbHeuresJournalieres;
    const nbJoursEntiers = Math.floor(totalJours);

    this.dureeStage.dureeMois = Math.floor(nbJoursEntiers / NB_JOUR_MOIS);
    this.dureeStage.dureeJours = nbJoursEntiers % NB_JOUR_MOIS;

    const heuresRestantes = nbHeures - (nbJoursEntiers * nbHeuresJournalieres);
    this.dureeStage.dureeHeures = Math.round(heuresRestantes * 100) / 100;
  }

  private calculPeriodeIrreguliere(nbHeures: number): void {
    const NB_JOUR_MOIS = 22; // 1 mois = 22 jours ouvrés

    // Calculer le nombre total de jours ouvrés réellement travaillés
    const totalJoursOuvres = this.calculerJoursOuvresTotal();

    // Utiliser le nombre réel de jours ouvrés pour le calcul
    this.dureeStage.dureeMois = Math.floor(totalJoursOuvres / NB_JOUR_MOIS);
    this.dureeStage.dureeJours = totalJoursOuvres % NB_JOUR_MOIS;

    // Calculer les heures restantes
    this.dureeStage.dureeHeures = this.calculerHeuresRestantesIrregulieres(nbHeures, totalJoursOuvres);
  }

  private calculerJoursOuvresTotal(): number {
    let totalJoursOuvres = 0;

    for (const periode of this.periodesCalculHeuresStage) {
      if (!periode.dateDebut || !periode.dateFin) continue;
      totalJoursOuvres += this.calculerJoursOuvresPourPeriode(periode);
    }

    return totalJoursOuvres;
  }

  private calculerJoursOuvresPourPeriode(periode: any): number {
    let joursOuvres = 0;
    let startDate = new Date(periode.dateDebut);
    const endDate = new Date(periode.dateFin);

    // Normaliser les dates
    startDate = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
    const normalizedEndDate = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate());

    // Préparer les données pour les vérifications
    const interruptionRanges = this.getInterruptionRanges();
    const joursFeriesMap = this.getJoursFeriesMap();

    // Compter les jours ouvrés dans cette période
    while (startDate <= normalizedEndDate) {
      if (this.isJourOuvre(startDate, interruptionRanges, joursFeriesMap)) {
        joursOuvres++;
      }
      // Passer au jour suivant
      startDate = new Date(startDate.setDate(startDate.getDate() + 1));
    }

    return joursOuvres;
  }

  private getInterruptionRanges(): Array<{start: Date, end: Date}> {
    const interruptionRanges = [];

    if (this.form.get('interruptionStage')!.value && this.interruptionsStage) {
      for (const interruption of this.interruptionsStage) {
        const startInterruption = new Date(interruption.dateDebutInterruption);
        const endInterruption = new Date(interruption.dateFinInterruption);
        interruptionRanges.push({
          start: new Date(startInterruption.getFullYear(), startInterruption.getMonth(), startInterruption.getDate()),
          end: new Date(endInterruption.getFullYear(), endInterruption.getMonth(), endInterruption.getDate())
        });
      }
    }

    return interruptionRanges;
  }

  private getJoursFeriesMap(): Set<number> {
    const joursFeriesMap = new Set<number>();

    for (const jourFerie of this.joursFeries) {
      joursFeriesMap.add(new Date(jourFerie.getFullYear(), jourFerie.getMonth(), jourFerie.getDate()).getTime());
    }

    return joursFeriesMap;
  }

  private isJourOuvre(date: Date, interruptionRanges: Array<{start: Date, end: Date}>, joursFeriesMap: Set<number>): boolean {
    const dayOfWeek = date.getDay();

    // Ignorer les weekends
    if (dayOfWeek === 6 || dayOfWeek === 0) {
      return false;
    }

    // Ignorer les jours fériés
    if (joursFeriesMap.has(date.getTime())) {
      return false;
    }

    // Ignorer les périodes d'interruption
    if (this.form.get('interruptionStage')!.value) {
      for (const range of interruptionRanges) {
        if (date >= range.start && date <= range.end) {
          return false;
        }
      }
    }

    return true;
  }

  private calculerHeuresRestantesIrregulieres(nbHeures: number, totalJoursOuvres: number): number {
    if (totalJoursOuvres === 0) {
      return nbHeures;
    }

    // Calculer la moyenne des heures journalières et voir s'il y a un reste
    const moyenneHeuresJournalieres = nbHeures / totalJoursOuvres;
    const heuresNormales = Math.floor(moyenneHeuresJournalieres) * totalJoursOuvres;

    return Math.round((nbHeures - heuresNormales) * 100) / 100;
  }

}
