import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { MatLegacyDialog as MatDialog, MatLegacyDialogConfig as MatDialogConfig } from '@angular/material/legacy-dialog';
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

  @Input() convention: any;

  form: UntypedFormGroup;

  texteLimiteRenumeration: string = '';

  periodesInterruptionsValid:boolean = false;
  minDateDebutStage: Date;
  maxDateDebutStage: Date;
  minDateFinStage: Date;
  maxDateFinStage: Date;
  previousValues: any;
  singleFieldUpdateLock: boolean = false;
  singleFieldUpdateQueue : any[] = [];
  updatingPeriode = false;

  @Output() validated = new EventEmitter<number>();
  @Output() updateField = new EventEmitter<any>();

  @Input() modifiable: boolean;
  @Input() enMasse: boolean;

  constructor(public conventionService: ConventionService,
              private fb: UntypedFormBuilder,
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
              public matDialog: MatDialog,
  ) {
  }

  ngOnInit(): void {

    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServPays: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.countries = response.data;
    });
    this.themeService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.thematiques = response.data;
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

    this.form = this.fb.group({
      // - Modèle de la convention
      idPays: [this.convention.paysConvention ? this.convention.paysConvention.id : null, [Validators.required]],
      // - Description du stage
      idTheme: [this.convention.theme ? this.convention.theme.id : null, [Validators.required]],
      sujetStage: [this.convention.sujetStage, [Validators.required]],
      competences: [this.convention.competences, [Validators.required]],
      fonctionsEtTaches: [this.convention.fonctionsEtTaches, [Validators.required]],
      details: [this.convention.details],
      // - Partie Dates / horaires
      dateDebutStage: [this.convention.dateDebutStage, [Validators.required]],
      dateFinStage: [this.convention.dateFinStage, [Validators.required]],
      interruptionStage: [this.convention.interruptionStage, [Validators.required]],
      horairesReguliers: [this.convention.horairesReguliers, [Validators.required]],
      nbHeuresHebdo: [this.convention.nbHeuresHebdo, this.fieldValidators['nbHeuresHebdo']],
      nbConges: [this.convention.nbConges],
      dureeExceptionnelle: [this.convention.dureeExceptionnelle, this.fieldValidators['dureeExceptionnelle']],
      idTempsTravail: [this.convention.tempsTravail ? this.convention.tempsTravail.id : null, [Validators.required]],
      commentaireDureeTravail: [this.convention.commentaireDureeTravail],
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
    this.toggleValidators(['idOrigineStage','confidentiel','idNatureTravail','idModeValidationStage'],!this.enMasse);

    this.loadInterruptionsStage();

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

  }

  ngOnChanges(): void{
    this.singleFieldUpdateLock = false;
    if(this.singleFieldUpdateQueue.length > 0){
      const data = this.singleFieldUpdateQueue.pop();
      this.updateSingleField(data.field,data.value);
    }
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

  setHorairesReguliersFormControls(event : any): void {
    this.toggleValidators(['nbHeuresHebdo',],event.value);
  }

  setGratificationStageFormControls(event : any): void {
    this.toggleValidators(['montantGratification','idUniteGratification','idUniteDuree','idDevise','idModeVersGratification'],event.value);
  }

  dateDebutChanged(event: any): void {
    this.updateDateFinBounds(event.value);
  }

  updateDateFinBounds(dateDebut: Date): void {
    this.minDateFinStage = new Date(dateDebut.getTime() + (1000 * 60 * 60 * 24));
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

  openCalendar(): void {
    this.openCalendarModal();
  }

  openCalendarModal(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {convention: this.convention,interruptionsStage: this.interruptionsStage};
    const modalDialog = this.matDialog.open(CalendrierComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        this.periodesCalculHeuresStage = dialogResponse;
        this.updateHeuresTravail();
      }
    });
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

  //fix new date('2021-02-02') != new date(2021,2,2)
  dateFromBackend(dateString: string):Date{
    let date = new Date(dateString)
    date = new Date(date.getFullYear(),date.getMonth(),date.getDate());
    return date;
  }

  updateHeuresTravail():void {
    this.updatingPeriode = true;
    setTimeout(() => {
      this.updatingPeriode = false;
    }, 2000);
      if (this.form.get('horairesReguliers')!.value){

        const dateDebutStage = this.dateFromBackend(this.form.get('dateDebutStage')!.value);
        const dateFinStage = this.dateFromBackend(this.form.get('dateFinStage')!.value);
        if (this.form.get('nbHeuresHebdo')?.valid) {
          const nbHeuresJournalieres = this.form.get('nbHeuresHebdo')!.value/5;

          const periodes = [{'dateDebut':dateDebutStage,'dateFin':dateFinStage,'nbHeuresJournalieres':nbHeuresJournalieres}];

          this.form.get('dureeExceptionnelle')?.setValue(this.calculHeuresTravails(periodes));
        }

      }else{
        this.form.get('dureeExceptionnelle')?.setValue(this.calculHeuresTravails(this.periodesCalculHeuresStage));
      }
  }

  calculHeuresTravails(periodes: any[]):number {
    let heuresTravails = 0;
    for (const periode of periodes){
      let loopDate = periode.dateDebut;
      let endDate = periode.dateFin;
      let nbHeuresJournalieres = periode.nbHeuresJournalieres;
      while(loopDate <= endDate){
        let valid = true;
        let dayOfWeek = loopDate.getDay();
        //skip weekends
        if ((dayOfWeek === 6) || (dayOfWeek  === 0)){
          valid = false;
        }
        //skip périodes d'interruptions
        if (this.form.get('interruptionStage')!.value){
          for (const interruptionStage of this.interruptionsStage) {
              if (loopDate >= this.dateFromBackend(interruptionStage.dateDebutInterruption) && loopDate <= this.dateFromBackend(interruptionStage.dateFinInterruption)){
                valid = false;
              }
          }
        }
        //skip jours fériés
        for (const joursFerie of this.joursFeries) {
            if (loopDate.getTime() === joursFerie.getTime()){
              valid = false;
            }
        }

        if (valid){
          heuresTravails = heuresTravails + nbHeuresJournalieres
        }
        loopDate.setDate(loopDate.getDate() + 1);
      }
    }

    return Math.round(heuresTravails * 100) / 100;
  }
}
