import { Component, EventEmitter, OnInit , Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { PaysService } from "../../../services/pays.service";
import { ThemeService } from "../../../services/theme.service";
import { DeviseService } from "../../../services/devise.service";
import { LangueConventionService } from "../../../services/langue-convention.service";
import { ModeVersGratificationService } from "../../../services/mode-vers-gratification.service";
import { UniteDureeService } from "../../../services/unite-duree.service";
import { UniteGratificationService } from "../../../services/unite-gratification.service";
import { TempsTravailService } from "../../../services/temps-travail.service";
import { OrigineStageService } from "../../../services/origine-stage.service";
import { NatureTravailService } from "../../../services/nature-travail.service";
import { ModeValidationStageService } from "../../../services/mode-validation-stage.service";
import { TypeConventionService } from "../../../services/type-convention.service";
import { AuthService } from "../../../services/auth.service";
import { ConventionService } from "../../../services/convention.service";
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-stage',
  templateUrl: './stage.component.html',
  styleUrls: ['./stage.component.scss']
})
export class StageComponent implements OnInit {

  countries: any[] = [];
  thematiques: any[] = [];
  devises: any[] = [];
  langueConventions: any[] = [];
  modeVersGratifications: any[] = [];
  uniteDurees: any[] = [];
  uniteGratifications: any[] = [];
  tempsTravails: any[] = [];
  origineStages: any[] = [];
  natureTravails: any[] = [];
  modeValidationStages: any[] = [];
  typeConventions: any[] = [];

  @Input() convention: any;

  form: FormGroup;

  @Output() validated = new EventEmitter<number>();

  constructor(public conventionService: ConventionService,
              private fb: FormBuilder,
              private authService: AuthService,
              private paysService: PaysService,
              private themeService: ThemeService,
              private deviseService: DeviseService,
              private langueConventionService: LangueConventionService,
              private modeVersGratificationService: ModeVersGratificationService,
              private uniteDureeService: UniteDureeService,
              private uniteGratificationService: UniteGratificationService,
              private tempsTravailService: TempsTravailService,
              private origineStageService: OrigineStageService,
              private natureTravailService: NatureTravailService,
              private modeValidationStageService: ModeValidationStageService,
              private typeConventionService: TypeConventionService,
  ) {
    this.form = this.fb.group({
      // - Modèle de la convention
      codeLangueConvention: [null, [Validators.required]],
      idPays: [null, [Validators.required]],
      idTypeConvention: [null, [Validators.required]],
      // - Description du stage
      idTheme: [null, [Validators.required]],
      //TODO case à cocher pour rendre les champs confidentiels cf 2.2.1.2 règles de gestions
      sujetStage: [null, [Validators.required]],
      competences: [null, [Validators.required]],
      fonctionsEtTaches: [null, [Validators.required]],
      details: [null, [Validators.required]],
      // - Partie Dates / horaires
      //TODO contrôle de la cohérence des dates saisies
      dateDebutStage: [null, [Validators.required]],
      dateFinStage: [null, [Validators.required]],
      interruptionStage: [false, [Validators.required]],
      //TODO multiples dates d'interruptions
      dateDebutInterruption: [null],
      dateFinInterruption: [null],
      horairesReguliers: [true, [Validators.required]],
      nbHeuresHebdo: [null, [Validators.required], Validators.pattern('[0-9]+([,.][0-9]{1,2})?')],
      idTempsTravail: [null, [Validators.required]],
      commentaireDureeTravail: [null],
      // - Partie Gratification
      gratificationStage: [false, [Validators.required]],
      montantGratification: [null, [Validators.required]],
      idUniteGratification: [null, [Validators.required]],
      idUniteDuree: [null, [Validators.required]],
      idDevise: [null, [Validators.required]],
      idModeVersGratification: [null, [Validators.required]],
      //TODO un bandeau doit permettre de mettre un message à l’attention de l’étudiant
      // - Partie Divers
      idOrigineStage: [null, [Validators.required]],
      idNatureTravail: [null, [Validators.required]],
      idModeValidationStage: [null, [Validators.required]],
      modeEncadreSuivi: [null],
      avantagesNature: [null],
      travailNuitFerie: [null],
      //TODO ajout de confidentiel au model convention
      confidentiel: [false, [Validators.required]],

    });
    console.log('convention : ' + JSON.stringify(this.convention, null, 2))
    this.form.valueChanges.pipe(debounceTime(1000)).subscribe(val => {
      console.log('val : ' + JSON.stringify(val, null, 2))
      this.updateBrouillon();
    });

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
      if(this.uniteDurees.length>0){
        this.form.controls['idUniteDuree'].setValue(this.uniteDurees[0].id);
      }
    });
    this.uniteGratificationService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.uniteGratifications = response.data;
      if(this.uniteGratifications.length>0){
        this.form.controls['idUniteGratification'].setValue(this.uniteGratifications[0].id);
      }
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

  }

  updateBrouillon(): void {
  //TODO
  }

}
