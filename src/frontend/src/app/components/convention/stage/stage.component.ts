import { Component, EventEmitter, OnInit, Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
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
import { AuthService } from "../../../services/auth.service";
import { ConventionService } from "../../../services/convention.service";
import {pairwise,debounceTime,startWith}from 'rxjs/operators'

@Component({
  selector: 'app-stage',
  templateUrl: './stage.component.html',
  styleUrls: ['./stage.component.scss']
})
export class StageComponent implements OnInit {

  fieldValidators : any = {
      'dateDebutInterruption': [Validators.required],
      'dateFinInterruption': [Validators.required],
      'nbHeuresHebdo': [Validators.required, Validators.pattern('[0-9]{1,2}([,.][0-9]{1,2})?')],
      //'quotiteTravail': [Validators.required, Validators.pattern('[0-9]+')],
      'montantGratification': [Validators.required, Validators.pattern('[0-9]{1,10}([,.][0-9]{1,2})?')],
      'idUniteGratification': [Validators.required],
      'idUniteDuree': [Validators.required],
      'idDevise': [Validators.required],
      'idModeVersGratification': [Validators.required],
  }

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

  @Input() convention: any;

  form: FormGroup;

  minDateDebutStage: Date;
  maxDateDebutStage: Date;
  minDateFinStage: Date;
  maxDateFinStage: Date;
  previousValues: any;

  @Output() validated = new EventEmitter<number>();
  @Output() updateField = new EventEmitter<any>();

  @Input() modifiable: boolean;

  constructor(public conventionService: ConventionService,
              private fb: FormBuilder,
              private authService: AuthService,
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
      codeLangueConvention: [this.convention.langueConvention ? this.convention.langueConvention.code : null, [Validators.required]],
      idPays: [this.convention.paysConvention ? this.convention.paysConvention.id : null, [Validators.required]],
      idTypeConvention: [this.convention.typeConvention ? this.convention.typeConvention.id : null, [Validators.required]],
      // - Description du stage
      idTheme: [this.convention.theme ? this.convention.theme.id : null, [Validators.required]],
      sujetStage: [this.convention.sujetStage, [Validators.required]],
      competences: [this.convention.competences, [Validators.required]],
      fonctionsEtTaches: [this.convention.fonctionsEtTaches, [Validators.required]],
      details: [this.convention.details],
      // - Partie Dates / horaires
      //TODO contrôle de la cohérence des dates saisies
      dateDebutStage: [this.convention.dateDebutStage, [Validators.required]],
      dateFinStage: [this.convention.dateFinStage, [Validators.required]],
      interruptionStage: [this.convention.interruptionStage, [Validators.required]],
      //TODO multiples dates d'interruptions
      dateDebutInterruption: [this.convention.dateDebutInterruption, this.fieldValidators['dateDebutInterruption']],
      dateFinInterruption: [this.convention.dateFinInterruption, this.fieldValidators['dateFinInterruption']],
      horairesReguliers: [this.convention.horairesReguliers, [Validators.required]],
      nbHeuresHebdo: [this.convention.nbHeuresHebdo, this.fieldValidators['nbHeuresHebdo']],
      //quotiteTravail: [this.convention.quotiteTravail, this.fieldValidators['quotiteTravail']],
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
      idOrigineStage: [this.convention.origineStage ? this.convention.origineStage.id : null, [Validators.required]],
      confidentiel: [this.convention.confidentiel, [Validators.required]],
      idNatureTravail: [this.convention.natureTravail ? this.convention.natureTravail.id : null, [Validators.required]],
      idModeValidationStage: [this.convention.modeValidationStage ? this.convention.modeValidationStage.id : null, [Validators.required]],
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
    this.toggleValidators(['dateDebutInterruption','dateFinInterruption'],this.convention.interruptionStage);
    this.toggleValidators(['nbHeuresHebdo',],this.convention.horairesReguliers);
    this.toggleValidators(['montantGratification','idUniteGratification','idUniteDuree','idDevise','idModeVersGratification'],this.convention.gratificationStage);

    if (this.form.valid) {
      this.validated.emit(2);
    }

    this.previousValues={...this.form.value}
    this.form.valueChanges.pipe(debounceTime(500)).subscribe(res=>{
      const keys=Object.keys(res).filter(k=>res[k]!=this.previousValues[k])
      this.previousValues={...this.form.value}
      keys.forEach((key: string) => {
        this.updateSingleField(key,res[key]);
      });
    })

    if (!this.modifiable) {
      this.form.disable();
    }

    this.minDateDebutStage = new Date(new Date().getTime() + (1000 * 60 * 60 * 24));
    this.maxDateDebutStage = new Date(new Date().getFullYear()+1, 0, 1);
    if (this.convention.dateDebutStage){
      this.updateDateFinBounds(new Date(this.convention.dateDebutStage));
    }else{
      this.minDateFinStage = new Date(new Date().getFullYear()-1, 0, 2);
      this.maxDateFinStage = new Date(new Date().getFullYear()+2, 0, 1);
    }

  }

  updateSingleField(key: string,value: any): void {

    if (this.form.get(key)!.valid) {
      const data = {
        "field":key,
        "value":value,
      };
      this.updateField.emit(data);
    }
    if (this.form.valid) {
      this.validated.emit(2);
    }else{
      this.validated.emit(0);
    }
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

  setInterruptionStageFormControls(event : any): void {
    this.toggleValidators(['dateDebutInterruption','dateFinInterruption'],event.value);
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
}
