import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {ReponseEvaluationService} from "../../../services/reponse-evaluation.service";
import {FicheEvaluationService} from "../../../services/fiche-evaluation.service";
import {MessageService} from "../../../services/message.service";
import {MatExpansionPanel} from "@angular/material/expansion";
import {AuthService} from "../../../services/auth.service";
import * as FileSaver from 'file-saver';
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";
import {ConfirmEnvoieMailComponent} from "./confirm-envoie-mail/confirm-envoie-mail.component";
import {QuestionsEvaluationService} from "../../../services/questions-evaluation.service";
import {forkJoin} from "rxjs";
import {DbQuestion} from "../../../models/question-evaluation.model";
import {TypeQuestionEvaluation} from "../../../constants/type-question-evaluation";
import {values} from "lodash";

@Component({
  selector: 'app-evaluation-stage',
  templateUrl: './evaluation-stage.component.html',
  styleUrls: ['./evaluation-stage.component.scss']
})
export class EvaluationStageComponent implements OnInit {

  ficheEvaluation: any;
  reponseEvaluation: any;
  questionsSupplementaires: any;
  @Input() convention: any;
  @Output() conventionChange = new EventEmitter<any>();

  reponseEtudiantForm: FormGroup;
  reponseEnseignantForm: FormGroup;
  reponseEntrepriseForm: FormGroup;

  reponseSupplementaireEtudiantForm: FormGroup;
  reponseSupplementaireEnseignantForm: FormGroup;
  reponseSupplementaireEntrepriseForm: FormGroup;

  edit: boolean = false;
  editEtu: boolean = false;
  editEns: boolean = false;
  editEnt: boolean = false;

  isEtudiant:boolean = false;
  isEnseignant:boolean = false;
  isGestionnaireOrAdmin:boolean = false;

  @ViewChild("generalPanel") generalPanel: MatExpansionPanel|undefined;

  controlsIndexToLetter:any = ['a','b','c','d','e','f','g','h']

  FicheEtudiantIQuestions: any[] = [];
  FicheEtudiantIIQuestions: any[] = [];
  FicheEtudiantIIIQuestions: any[] = [];

  FicheEnseignantIQuestions: any[] = [];
  FicheEnseignantIIQuestions: any[] = [];

  FicheEntrepriseIQuestions: any[] = [];
  FicheEntrepriseIIQuestions: any[] = [];
  FicheEntrepriseIIIQuestions: any[] = [];

  private LIKERT_5 = ['Excellent','Très bien','Bien','Satisfaisant','Insuffisant'];
  private AGREEMENT_5 = [
    'Tout à fait d\'accord','Plutôt d\'accord','Sans avis',
    'Plutôt pas d\'accord','Pas du tout d\'accord'
  ];

  //-----------------------------------------------|INIT |-----------------------------------------------//
  constructor(private reponseEvaluationService: ReponseEvaluationService,
              private ficheEvaluationService: FicheEvaluationService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private authService: AuthService,
              private matDialog: MatDialog,
              private questionsEvaluationService : QuestionsEvaluationService,
  ) {
    this.reponseEtudiantForm = this.fb.group({
      reponseEtuI1: [null, [Validators.required]],
      reponseEtuI1bis: [null],
      reponseEtuI2: [null, [Validators.required]],
      reponseEtuI3: [null, [Validators.required]],
      reponseEtuI4a: [null, [Validators.required]],
      reponseEtuI4b: [null, [Validators.required]],
      reponseEtuI4c: [null, [Validators.required]],
      reponseEtuI4d: [null, [Validators.required]],
      reponseEtuI5: [null],
      reponseEtuI6: [null, [Validators.required]],
      reponseEtuI7: [null, [Validators.required]],
      reponseEtuI7bis1: [null],
      reponseEtuI7bis1a: [null],
      reponseEtuI7bis1b: [null],
      reponseEtuI7bis2: [null],
      reponseEtuI8: [null, [Validators.required]],
      reponseEtuII1: [null, [Validators.required]],
      reponseEtuII1bis: [null],
      reponseEtuII2: [null, [Validators.required]],
      reponseEtuII2bis: [null],
      reponseEtuII3: [null, [Validators.required]],
      reponseEtuII3bis: [null],
      reponseEtuII4: [null, [Validators.required]],
      reponseEtuII5: [null, [Validators.required]],
      reponseEtuII5a: [null],
      reponseEtuII5b: [null],
      reponseEtuII6: [null, [Validators.required]],
      reponseEtuIII1: [null, [Validators.required]],
      reponseEtuIII1bis: [null],
      reponseEtuIII2: [null, [Validators.required]],
      reponseEtuIII2bis: [null],
      reponseEtuIII4: [null, [Validators.required]],
      reponseEtuIII5a: [null, [Validators.required]],
      reponseEtuIII5b: [null, [Validators.required]],
      reponseEtuIII5c: [null, [Validators.required]],
      reponseEtuIII5bis: [null],
      reponseEtuIII6: [null, [Validators.required]],
      reponseEtuIII6bis: [null],
      reponseEtuIII7: [null, [Validators.required]],
      reponseEtuIII7bis: [null],
      reponseEtuIII8: [null, [Validators.required]],
      reponseEtuIII8bis: [null],
      reponseEtuIII9: [null, [Validators.required]],
      reponseEtuIII9bis: [null],
      reponseEtuIII10: [null, [Validators.required]],
      reponseEtuIII11: [null, [Validators.required]],
      reponseEtuIII12: [null, [Validators.required]],
      reponseEtuIII14: [null, [Validators.required]],
      reponseEtuIII15: [null, [Validators.required]],
      reponseEtuIII15bis: [null],
      reponseEtuIII16: [null, [Validators.required]],
      reponseEtuIII16bis: [null, [Validators.required]],
    });

    this.reponseEnseignantForm = this.fb.group({
      reponseEnsI1a: [null, [Validators.required]],
      reponseEnsI1b: [null, [Validators.required]],
      reponseEnsI1c: [null, [Validators.required]],
      reponseEnsI2a: [null, [Validators.required]],
      reponseEnsI2b: [null, [Validators.required]],
      reponseEnsI2c: [null, [Validators.required]],
      reponseEnsI3: [null, [Validators.required]],
      reponseEnsII1: [null, [Validators.required]],
      reponseEnsII2: [null, [Validators.required]],
      reponseEnsII3: [null, [Validators.required]],
      reponseEnsII4: [null, [Validators.required]],
      reponseEnsII5: [null, [Validators.required]],
      reponseEnsII6: [null, [Validators.required]],
      reponseEnsII7: [null, [Validators.required]],
      reponseEnsII8: [null, [Validators.required]],
      reponseEnsII9: [null, [Validators.required]],
      reponseEnsII10: [null, [Validators.required]],
      reponseEnsII11: [null, [Validators.required]],
    });

    this.reponseEntrepriseForm = this.fb.group({
      reponseEnt1: [null, [Validators.required]],
      reponseEnt1bis: [null],
      reponseEnt2: [null, [Validators.required]],
      reponseEnt2bis: [null],
      reponseEnt3: [null, [Validators.required]],
      reponseEnt4: [null, [Validators.required]],
      reponseEnt4bis: [null],
      reponseEnt5: [null, [Validators.required]],
      reponseEnt5bis: [null],
      reponseEnt6: [null, [Validators.required]],
      reponseEnt6bis: [null],
      reponseEnt7: [null, [Validators.required]],
      reponseEnt7bis: [null],
      reponseEnt8: [null, [Validators.required]],
      reponseEnt8bis: [null],
      reponseEnt9: [null, [Validators.required]],
      reponseEnt9bis: [null],
      reponseEnt10: [null, [Validators.required]],
      reponseEnt10bis: [null],
      reponseEnt11: [null, [Validators.required]],
      reponseEnt11bis: [null],
      reponseEnt12: [null, [Validators.required]],
      reponseEnt12bis: [null],
      reponseEnt13: [null, [Validators.required]],
      reponseEnt13bis: [null],
      reponseEnt14: [null, [Validators.required]],
      reponseEnt14bis: [null],
      reponseEnt15: [null, [Validators.required]],
      reponseEnt15bis: [null],
      reponseEnt16: [null, [Validators.required]],
      reponseEnt16bis: [null],
      reponseEnt17: [null, [Validators.required]],
      reponseEnt17bis: [null],
      reponseEnt18: [null, [Validators.required]],
      reponseEnt18bis: [null],
      reponseEnt19: [null, [Validators.required]],
    });

    //gestion des champs required conditionnels
    for(let question of this.FicheEtudiantIQuestions.concat(this.FicheEtudiantIIQuestions).concat(this.FicheEtudiantIIIQuestions)){
      if(question.bisQuestionLowNotation || question.bisQuestionTrue || question.bisQuestionFalse ||
        question.controlName == 'EtuI7' || question.controlName == 'EtuII5'){
        let key = 'reponse' + question.controlName;
        let bisKey = key + 'bis';
        this.reponseEtudiantForm.get(key)?.valueChanges.subscribe(val => {
          let questionKey = 'question' + question.controlName;
          if(this.ficheEvaluation[questionKey]){
            if((question.bisQuestionLowNotation && val>=3) || (question.bisQuestionTrue && val) || (question.bisQuestionFalse && !val) ){
              this.toggleValidators(this.reponseEtudiantForm,[bisKey],true);
            }else if(question.controlName == 'EtuI7'){
              let bisKey1 = key + 'bis1';
              let bisKey2 = key + 'bis2';
              if(val){
                this.toggleValidators(this.reponseEtudiantForm,[bisKey1],true);
                this.toggleValidators(this.reponseEtudiantForm,[bisKey2],false);
              }else{
                this.toggleValidators(this.reponseEtudiantForm,[bisKey2],true);
                this.toggleValidators(this.reponseEtudiantForm,[bisKey1],false);
              }
            }else if(question.controlName == 'EtuII5'){
              let bisKey1 = key + 'a';
              let bisKey2 = key + 'b';
              if(val){
                this.toggleValidators(this.reponseEtudiantForm,[bisKey1],true);
                this.toggleValidators(this.reponseEtudiantForm,[bisKey2],true);
              }else{
                this.toggleValidators(this.reponseEtudiantForm,[bisKey1],false);
                this.toggleValidators(this.reponseEtudiantForm,[bisKey2],false);
              }
            }else{
              this.toggleValidators(this.reponseEtudiantForm,[bisKey],false);
            }
          }
        });
      }
    }
    for(let question of this.FicheEnseignantIQuestions.concat(this.FicheEnseignantIIQuestions)){
      if(question.bisQuestionLowNotation || question.bisQuestionTrue || question.bisQuestionFalse){
        let key = 'reponse' + question.controlName;
        let bisKey = key + 'bis';
        this.reponseEnseignantForm.get(key)?.valueChanges.subscribe(val => {
          let questionKey = 'question' + question.controlName;
          if(this.ficheEvaluation[questionKey]){
            if((question.bisQuestionLowNotation && val>=3) || (question.bisQuestionTrue && val) || (question.bisQuestionFalse && !val) ){
              this.toggleValidators(this.reponseEnseignantForm,[bisKey],true);
            }else{
              this.toggleValidators(this.reponseEnseignantForm,[bisKey],false);
            }
          }
        });
      }
    }
    for(let question of this.FicheEntrepriseIQuestions.concat(this.FicheEntrepriseIIQuestions).concat(this.FicheEntrepriseIIIQuestions)){
      if(question.bisQuestionLowNotation || question.bisQuestionTrue || question.bisQuestionFalse){
        let key = 'reponse' + question.controlName;
        let bisKey = key + 'bis';
        this.reponseEntrepriseForm.get(key)?.valueChanges.subscribe(val => {
          let questionKey = 'question' + question.controlName;
          if(this.ficheEvaluation[questionKey]){
            if((question.bisQuestionLowNotation && val>=3) || (question.bisQuestionTrue && val) || (question.bisQuestionFalse && !val) ){
              this.toggleValidators(this.reponseEntrepriseForm,[bisKey],true);
            }else{
              this.toggleValidators(this.reponseEntrepriseForm,[bisKey],false);
            }
          }
        });
      }
    }

    this.reponseSupplementaireEtudiantForm = this.fb.group({});
    this.reponseSupplementaireEnseignantForm = this.fb.group({});
    this.reponseSupplementaireEntrepriseForm = this.fb.group({});
  }

  ngOnInit(): void {
    this.isEtudiant = this.authService.isEtudiant();
    this.isEnseignant = this.authService.isEnseignant();
    this.isGestionnaireOrAdmin = this.authService.isGestionnaire() || this.authService.isAdmin();

    forkJoin({
      etu: this.questionsEvaluationService.getQuestionsEtu(),
      ens: this.questionsEvaluationService.getQuestionsEns(),
      ent: this.questionsEvaluationService.getQuestionsEnt(),
      fiche: this.ficheEvaluationService.getByCentreGestion(this.convention.centreGestion.id),
      rep: this.reponseEvaluationService.getByConvention(this.convention.id)
    }).subscribe(({ etu, ens, ent, fiche, rep }) => {
      this.ficheEvaluation = fiche;

      this.applyDbQuestions(etu, this.FicheEtudiantIQuestions, this.FicheEtudiantIIQuestions, this.FicheEtudiantIIIQuestions);
      this.applyDbQuestions(ens, this.FicheEnseignantIQuestions, this.FicheEnseignantIIQuestions);
      this.applyDbQuestions(ent, this.FicheEntrepriseIQuestions, this.FicheEntrepriseIIQuestions, this.FicheEntrepriseIIIQuestions);

      this.wireConditionalValidators();

      this.applyFicheVisibilityToValidators();

      this.reponseEvaluationService.getByConvention(this.convention.id).subscribe((rep: any) => {
        this.reponseEvaluation = rep;
        this.getQuestionSupplementaire();

        if (rep) {
          this.reponseEvaluation = rep;
          this.reponseEtudiantForm.patchValue(rep);
          this.reponseEnseignantForm.patchValue(rep);
          this.reponseEntrepriseForm.patchValue(rep);
        }
      });

    });
  }

  //-----------------------------------------------| Fonctionnement |-----------------------------------------------//
  toggleValidators(form: FormGroup, keys: string[], toggle: boolean): void {
    keys.forEach((key: string) => {
      const control = form.get(key);
      if (control) {
        if (toggle) {
          control.addValidators(Validators.required);
        } else {
          control.clearValidators();
        }
        control.updateValueAndValidity();
      }
    });
  }

  saveReponse(typeFiche: number): void {

    let reponseForm = this.fb.group({});
    let reponseSupplementaireForm = this.fb.group({});
    let questionsSupplementaires = [];

    if(typeFiche == 0){
      reponseForm = this.reponseEtudiantForm;
      reponseSupplementaireForm = this.reponseSupplementaireEtudiantForm;
      questionsSupplementaires = this.questionsSupplementaires[0].concat(this.questionsSupplementaires[1]).concat(this.questionsSupplementaires[2]);
    }

    if(typeFiche == 1){
      reponseForm = this.reponseEnseignantForm;
      reponseSupplementaireForm = this.reponseSupplementaireEnseignantForm;
      questionsSupplementaires = this.questionsSupplementaires[3].concat(this.questionsSupplementaires[4]);
    }

    if(typeFiche == 2){
      reponseForm = this.reponseEntrepriseForm;
      reponseSupplementaireForm = this.reponseSupplementaireEntrepriseForm;
      questionsSupplementaires = this.questionsSupplementaires[5].concat(this.questionsSupplementaires[6]).concat(this.questionsSupplementaires[7]);
    }

    const valid = reponseForm.valid && reponseSupplementaireForm.valid

    const data = {...reponseForm.value};

    for(let questionSupplementaire of questionsSupplementaires){
      let reponseSupplementaireData = {'reponseTxt':null,'reponseInt':null,'reponseBool':null,};
      if(questionSupplementaire.typeQuestion == 'txt'){
        reponseSupplementaireData.reponseTxt = reponseSupplementaireForm.get(questionSupplementaire.formControlName)!.value;
      }
      if(questionSupplementaire.typeQuestion == 'not'){
        reponseSupplementaireData.reponseInt = reponseSupplementaireForm.get(questionSupplementaire.formControlName)!.value;
      }
      if(questionSupplementaire.typeQuestion == 'yn'){
        reponseSupplementaireData.reponseBool = reponseSupplementaireForm.get(questionSupplementaire.formControlName)!.value;
      }
      if(questionSupplementaire.reponse){
        this.reponseEvaluationService.updateReponseSupplementaire(this.convention.id, questionSupplementaire.id, reponseSupplementaireData).subscribe((response: any) => {
        });
      }else{
        this.reponseEvaluationService.createReponseSupplementaire(this.convention.id, questionSupplementaire.id, reponseSupplementaireData).subscribe((response: any) => {
        });
      }
    }

    if(typeFiche == 0){
      if(this.reponseEvaluation){
        this.reponseEvaluationService.updateReponseEtudiant(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }else{
        this.reponseEvaluationService.createReponseEtudiant(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }
    }

    if(typeFiche == 1){
      if(this.reponseEvaluation){
        this.reponseEvaluationService.updateReponseEnseignant(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }else{
        this.reponseEvaluationService.createReponseEnseignant(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }
    }

    if(typeFiche == 2){
      if(this.reponseEvaluation){
        this.reponseEvaluationService.updateReponseEntreprise(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }else{
        this.reponseEvaluationService.createReponseEntreprise(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }
    }
  }

  printFiche(typeFiche: number): void {
    this.reponseEvaluationService.getFichePDF(this.convention.id, typeFiche).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: "application/pdf"});
      let filename;
      if (typeFiche==0){
        filename = 'FicheEtudiant_' + this.convention.id + '.pdf';
      }
      if (typeFiche==1){
        filename = 'FicheEnseignant_' + this.convention.id + '.pdf';
      }
      if (typeFiche==2){
        filename = 'FicheEntreprise_' + this.convention.id + '.pdf';
      }
      FileSaver.saveAs(blob, filename);
    });
  }

  openConfirmEnvoiMailEvaluation(typeFiche: number): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.disableClose = true;
    dialogConfig.data = { typeFiche, convention: this.convention };

    const modalDialog = this.matDialog.open(ConfirmEnvoieMailComponent, dialogConfig);

    modalDialog.afterClosed().subscribe((result?: { convention?: any }) => {
      if (result?.convention) {
        this.convention = result.convention;
        this.conventionChange.emit(result.convention);
      }
    });
  }

  //-----------------------------------------------| Questionn Réponse Supplémentaire |-----------------------------------------------//

  getQuestionSupplementaire(): void {

    this.ficheEvaluationService.getQuestionsSupplementaires(this.ficheEvaluation.id).subscribe((response: any) => {

      let questionsSupplementaires = response;

      for(let questionSupplementaire of questionsSupplementaires){

        let form = this.fb.group({});

        if(questionSupplementaire.idPlacement == 0 || questionSupplementaire.idPlacement == 1 || questionSupplementaire.idPlacement == 2){
          form = this.reponseSupplementaireEtudiantForm;
        }

        if(questionSupplementaire.idPlacement == 3 || questionSupplementaire.idPlacement == 4){
          form = this.reponseSupplementaireEnseignantForm;
        }

        if(questionSupplementaire.idPlacement == 5 || questionSupplementaire.idPlacement == 6 || questionSupplementaire.idPlacement == 7){
          form = this.reponseSupplementaireEntrepriseForm;
        }
        const questionSupplementaireFormControlName = 'questionSupplementaire' + questionSupplementaire.id
        form.addControl(questionSupplementaireFormControlName,new FormControl(null, Validators.required));
        questionSupplementaire.formControlName = questionSupplementaireFormControlName

        if(this.reponseEvaluation){
          this.reponseEvaluationService.getReponseSupplementaire(this.convention.id, questionSupplementaire.id).subscribe((response2: any) => {

            questionSupplementaire.reponse = false;
            if (response2){
              questionSupplementaire.reponse = true;
              if(questionSupplementaire.typeQuestion == 'txt'){
                form.get(questionSupplementaireFormControlName)!.setValue(response2.reponseTxt);
              }
              if(questionSupplementaire.typeQuestion == 'not'){
                form.get(questionSupplementaireFormControlName)!.setValue(response2.reponseInt);
              }
              if(questionSupplementaire.typeQuestion == 'yn'){
                form.get(questionSupplementaireFormControlName)!.setValue(response2.reponseBool);
              }
            }
          });
        }
      }

      this.questionsSupplementaires = [];
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 0));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 1));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 2));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 3));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 4));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 5));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 6));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 7));

      this.reponseSupplementaireEtudiantForm.patchValue(response)
      this.reponseSupplementaireEnseignantForm.patchValue(response)
      this.reponseSupplementaireEntrepriseForm.patchValue(response)
    });
  }

  //-----------------------------------------------| Question |-----------------------------------------------//

  private parseParamsJsonLoose(paramsJson?: string | null): string[] {
    if (!paramsJson) return [];
    try {
      // Tentative JSON stricte
      const obj = JSON.parse(paramsJson);
      if (Array.isArray(obj?.items)) return obj.items;
    } catch {
      // version "lax": extrait le tableau à l’intérieur des []
      const m = paramsJson.match(/\[([\s\S]*)\]/);
      if (!m) return [];
      const inside = m[1];

      // découpe par virgules, garde ce qui est entre guillemets, nettoie les espaces
      return inside
        .split(',')
        .map(s => s.trim())
        .map(s => s.replace(/^"|"$/g, '')) // "Texte" -> Texte
        .filter(Boolean);
    }
    return [];
  }

  private extractOptions(q: { code: string; type: TypeQuestionEvaluation; paramsJson?: string | null; options?: string[] | null }): string[] {
    if (q.options?.length) return q.options;
    const fromParams = this.parseParamsJsonLoose(q.paramsJson);
    if (fromParams.length) return fromParams;

    // Échelles par défaut si rien en DB
    if (q.type === TypeQuestionEvaluation.SCALE_LIKERT_5)
      return this.LIKERT_5;
    if (q.type === TypeQuestionEvaluation.SCALE_AGREEMENT_5)
      return this.AGREEMENT_5;

    // Fallbacks legacy utiles
    const fb: Record<string, string[]> = {
      ETUI1:  ['Non, il est automatiquement proposé dans le cadre de la formation','Non, je l’ai trouvé assez facilement par moi-même','Oui j’ai eu des difficultés'],
      ETUI2:  ['1 jour à 1 semaine','2 semaines à 1 mois','1 mois à 3 mois','3 mois à 6 mois','+ de 6 mois'],
      ETUI3:  ['1 à 5','6 à 10','11 à 20','20 et plus'],
      ETUI6:  ['Proposé par votre tuteur professionnel','Proposé par votre tuteur enseignant','Élaboré par vous-même','Négocié entre les parties'],
      ETUIII4:['Très au-dessous de vos compétences','Au-dessous de vos compétences','A votre niveau de compétences','Au-dessus de vos compétences','Très au-dessus de vos compétences','Inatteignables'],
      ETUIII5:['Compétences techniques','Nouvelles méthodologies','Nouvelles connaissances théoriques'],
    };
    return fb[q.code] || [];
  }

  private applyDbQuestions(
    dbQuestions: DbQuestion[],
    section1: any[],
    section2?: any[],
    section3?: any[]
  ): void {
    for (const q of dbQuestions) {
      const vm = this.toVM(q);

      const target = (() => {
        const cn = vm.code;

        // Étudiant
        if (cn.startsWith('ETUIII')) return section3!;
        if (cn.startsWith('ETUII'))  return section2!;
        if (cn.startsWith('ETUI'))   return section1;

        // Enseignant
        if (cn.startsWith('ENSII'))  return section2!;
        if (cn.startsWith('ENSI'))   return section1;

        // Entreprise
        if (cn.startsWith('ENT')) {
          const num = parseInt(cn.replace('ENT',''), 10);
          if (num <= 9)  return section1;
          if (num <= 14) return section2!;
          return section3!;
        }
        return section1;
      })();

      // Upsert dans la section (on garde éventuels champs bis existants)
      const idx = target.findIndex((x: any) => x.code === vm.code);
      if (idx > -1) target[idx] = { ...target[idx], ...vm };
      else target.push(vm);

      // Et on crée les controls qui vont avec
      this.ensureFormControls(vm);
    }
  }


  private toVM(q: DbQuestion) {
    const optionsNeeded =
      q.type === TypeQuestionEvaluation.SINGLE_CHOICE ||
      q.type === TypeQuestionEvaluation.MULTI_CHOICE ||
      q.type === TypeQuestionEvaluation.BOOLEAN_GROUP ||
      q.type === TypeQuestionEvaluation.SCALE_LIKERT_5 ||
      q.type === TypeQuestionEvaluation.SCALE_AGREEMENT_5;

    const options = optionsNeeded
      ? this.extractOptions({
        code: q.code,
        type: q.type,
        paramsJson: q.paramsJson ?? undefined,
        options: q.options ?? undefined,
      })
      : [];

    return {
      code: q.code,
      title: q.texte,
      type: q.type as TypeQuestionEvaluation,
      options,
      bisQuestion: q.bisQuestion,
    };
  }


  private ensureFormControls(vm: any): void {
    const form =
      vm.code.startsWith('ETU') ? this.reponseEtudiantForm :
        vm.code.startsWith('ENS') ? this.reponseEnseignantForm :
          this.reponseEntrepriseForm;

    const base = this.toControlBase(vm.code); // ✅ reponseEtu…, reponseEns…, reponseEnt…

    // principal
    if (!form.contains(base)) {
      form.addControl(base, new FormControl(null, Validators.required));
    }

    // BOOLEAN_GROUP -> suffixes a,b,c…
    if (vm.type === TypeQuestionEvaluation.BOOLEAN_GROUP && Array.isArray(vm.options)) {
      vm.options.forEach((_: string, i: number) => {
        const key = base + this.controlsIndexToLetter[i];
        if (!form.contains(key)) {
          form.addControl(key, new FormControl(null, Validators.required));
        }
      });
    }

    // "bis" générique
    if (vm.bisQuestion || vm.bisQuestionLowNotation || vm.bisQuestionTrue || vm.bisQuestionFalse) {
      const bisKey = base + 'bis';
      if (!form.contains(bisKey)) form.addControl(bisKey, new FormControl(null));
    }

    if (vm.code === 'ETUI7') {
      if (!form.contains(base + 'bis1')) form.addControl(base + 'bis1', new FormControl(null));
      if (!form.contains(base + 'bis2')) form.addControl(base + 'bis2', new FormControl(null));
    }
    if (vm.code === 'ETUII5') {
      if (!form.contains(base + 'a')) form.addControl(base + 'a', new FormControl(null));
      if (!form.contains(base + 'b')) form.addControl(base + 'b', new FormControl(null));
    }
  }

  private applyFicheVisibilityToValidators(): void {
    const allEtud = this.FicheEtudiantIQuestions.concat(this.FicheEtudiantIIQuestions).concat(this.FicheEtudiantIIIQuestions);
    for (const q of allEtud) this.applyVisibilityForQuestion(this.reponseEtudiantForm, q);

    const allEns = this.FicheEnseignantIQuestions.concat(this.FicheEnseignantIIQuestions);
    for (const q of allEns) this.applyVisibilityForQuestion(this.reponseEnseignantForm, q);

    const allEnt = this.FicheEntrepriseIQuestions.concat(this.FicheEntrepriseIIQuestions).concat(this.FicheEntrepriseIIIQuestions);
    for (const q of allEnt) this.applyVisibilityForQuestion(this.reponseEntrepriseForm, q);
  }

  private applyVisibilityForQuestion(form: FormGroup, q: any): void {
    const active = this.isQuestionActive(q.code);
    const base = this.toControlBase(q.code);
    const keys: string[] = [];

    if (q.type === TypeQuestionEvaluation.BOOLEAN_GROUP) {
      (q.options || []).forEach((_: string, i: number) => keys.push(base + this.controlsIndexToLetter[i]));
    } else {
      keys.push(base);
      if (q.bisQuestionLowNotation || q.bisQuestionTrue || q.bisQuestionFalse) keys.push(base + 'bis');
      if (q.code === 'ETUI7') keys.push(base + 'bis1', base + 'bis2');
      if (q.code === 'ETUII5') keys.push(base + 'a', base + 'b');
    }
    this.toggleValidators(form, keys, active);
  }


  private wireConditionalValidators(): void {
    const hook = (form: FormGroup, q: any) => {
      if (q.bisQuestionLowNotation || q.bisQuestionTrue || q.bisQuestionFalse || q.code === 'ETUI7' || q.code === 'ETUII5') {
        const base = this.toControlBase(q.code);
        const main = form.get(base);
        if (!main) return;

        main.valueChanges.subscribe(val => {
          if (!this.isQuestionActive(q.code)) return;

          if ((q.bisQuestionLowNotation && val >= 3) || (q.bisQuestionTrue && !!val) || (q.bisQuestionFalse && !val)) {
            this.toggleValidators(form, [base + 'bis'], true);
          } else if (q.code === 'ETUI7') {
            this.toggleValidators(form, [base + 'bis1'], !!val);
            this.toggleValidators(form, [base + 'bis2'], !val);
          } else if (q.code === 'ETUII5') {
            this.toggleValidators(form, [base + 'a', base + 'b'], !!val);
          } else {
            this.toggleValidators(form, [base + 'bis'], false);
          }
        });
      }
    };

    this.FicheEtudiantIQuestions.concat(this.FicheEtudiantIIQuestions, this.FicheEtudiantIIIQuestions).forEach(q => hook(this.reponseEtudiantForm, q));
    this.FicheEnseignantIQuestions.concat(this.FicheEnseignantIIQuestions).forEach(q => hook(this.reponseEnseignantForm, q));
    this.FicheEntrepriseIQuestions.concat(this.FicheEntrepriseIIQuestions, this.FicheEntrepriseIIIQuestions).forEach(q => hook(this.reponseEntrepriseForm, q));
  }

  getAutoValue(code: string): string {
    switch (code) {
      case 'ETUI5':
        return this.convention?.origineStage?.libelle || '';
      case 'ETUIII0':
        return this.convention?.sujetStage || '';
      default:
        return '';
    }
  }

  protected readonly TypeQuestionEvaluation = TypeQuestionEvaluation;

  toControlBase(code: string): string {
    if (!code) return '';
    if (code.startsWith('ETU')) return 'reponseEtu' + code.substring(3);
    if (code.startsWith('ENS')) return 'reponseEns' + code.substring(3);
    if (code.startsWith('ENT')) return 'reponseEnt' + code.substring(3);
    return 'reponse' + code; // fallback
  }

  /** Clé "legacy" pour ficheEvaluation (questionEtuII1, questionEnt4, …) */
  private toLegacyQuestionKey(code: string): string {
    if (code.startsWith('ETU')) return 'questionEtu' + code.substring(3);
    if (code.startsWith('ENS')) return 'questionEns' + code.substring(3);
    if (code.startsWith('ENT')) return 'questionEnt' + code.substring(3);
    return 'question' + code;
  }

  private isQuestionActive(code: string): boolean {
    const k = this.toLegacyQuestionKey(code);
    return !!this.ficheEvaluation?.[k];
  }

  getMainValue(q: any): any {
    const base = this.toControlBase(q.code);
    const ctrl =
      this.reponseEtudiantForm.get(base) ||
      this.reponseEnseignantForm.get(base) ||
      this.reponseEntrepriseForm.get(base);
    return ctrl ? ctrl.value : null;
  }


  // Gestion des cas spéciaux
  /** Parse JSON tolérant (objet) – renvoie {} si invalide */
  private parseObjectLoose(s?: string | null): any {
    if (!s) return {};
    try { return JSON.parse(s); } catch { return {}; }
  }

  /** Options spéciales pour ETUI7 : { oui: string[], non: string[] } */
  getETUI7Options(q: any): { oui: string[]; non: string[] } {
    // 1) JSON prioritaire
    const obj = this.parseObjectLoose(q?.paramsJson);
    const ouiFromJson: string[] = Array.isArray(obj?.oui) ? obj.oui : [];
    const nonFromJson: string[] = Array.isArray(obj?.non) ? obj.non : [];
    if (ouiFromJson.length || nonFromJson.length) {
      return { oui: ouiFromJson, non: nonFromJson };
    }

    // 2) Fallback: on réutilise le comportement historique (slice)
    const opts: string[] = Array.isArray(q?.options) ? q.options : [];
    const oui = opts.slice(0, 5);
    const non = opts.slice(5, 7);

    // 3) Fallback “dur” si vraiment rien
    if (!oui.length && !non.length) {
      return {
        oui: [
          "Proposé par votre tuteur professionnel",
          "Proposé par votre tuteur enseignant",
          "Élaboré par vous-même",
          "Négocié entre les parties",
          "Autre"
        ],
        non: [
          "Je n'ai pas eu besoin d'aide",
          "Je ne savais pas à qui m'adresser"
        ]
      };
    }
    return { oui, non };
  }

  /** Options spéciales pour ETUII5 : { a: string[] } */
  getETUII5Options(q: any): { a: string[] } {
    // 1) JSON prioritaire
    const obj = this.parseObjectLoose(q?.paramsJson);
    const aFromJson: string[] = Array.isArray(obj?.a) ? obj.a : [];
    if (aFromJson.length) return { a: aFromJson };

    // 2) Fallback: utilise q.options
    const opts: string[] = Array.isArray(q?.options) ? q.options : [];

    // 3) Fallback “dur” si vide
    if (!opts.length) {
      return { a: ["Technique", "Organisationnelle", "Communication"] };
    }
    return { a: opts };
  }

  shouldShowBis(q: any): boolean {
    if (!q?.bisQuestion) return false;

    const v = this.getMainValue(q);

    if (q.code === 'ETUIII1') return v === true;

    if((q.type == TypeQuestionEvaluation.SCALE_LIKERT_5 || q.type == TypeQuestionEvaluation.SCALE_AGREEMENT_5) && v > 2 && q.code.startsWith('ETU')) return true;

    if((q.type == TypeQuestionEvaluation.SCALE_LIKERT_5 || q.type == TypeQuestionEvaluation.SCALE_AGREEMENT_5) && q.code.startsWith('ENT')) return true;



    return false;
  }


}
