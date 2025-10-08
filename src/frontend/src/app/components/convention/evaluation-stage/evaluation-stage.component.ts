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

      this.reponseEvaluation = rep;
      if (rep) {
        this.reponseEtudiantForm.patchValue(rep);
        this.reponseEnseignantForm.patchValue(rep);
        this.reponseEntrepriseForm.patchValue(rep);
      }

      // 5) Charger les questions suppl. une fois la base posée
      this.getQuestionSupplementaire();
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

    console.log("reponseSupplementaireForm : ",reponseSupplementaireForm.valid);
    console.log("reponseForm : ",reponseForm.valid);
    console.log("EtuFrom : ",this.reponseEtudiantForm.valid);
    console.log("EnsFrom : ",this.reponseEnseignantForm.valid);
    console.log("EntFrom : ",this.reponseEntrepriseForm.valid);
    console.log("valid : ",valid);

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
    });
  }

  // getQestionTexte(question: any): string {
  //
  //   let htmlTexte = "";
  //
  //   htmlTexte += "<p style=\"margin-left: 16px\"><span class=\"text-small\"><strong> - "+question.title+"</strong></span></p>";
  //
  //   if(question.type == 'boolean'){
  //     if(this.reponseEvaluation['reponse' + question.controlName]){
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Oui</span></p>";
  //     }else{
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Non</span></p>";
  //     }
  //   }
  //   if(question.type == 'multiple-choice'){
  //     let formControlName = 'reponse' + question.controlName;
  //
  //     let line = question.texte[this.reponseEvaluation[formControlName]];
  //     htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
  //
  //     if(question.bisQuestionLowNotation &&
  //       (this.reponseEvaluation[formControlName] !== null) &&
  //       (this.reponseEvaluation[formControlName] >= 3)){
  //       let line = question.bisQuestionLowNotation;
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong> - "+line+"</strong>";
  //       let formControlName = "reponse" + question.controlName + 'bis';
  //       line = this.reponseEvaluation[formControlName];
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
  //     }
  //     if(question.bisQuestion){
  //       let formControlName = "reponse" + question.controlName + 'bis';
  //       let bisLine = this.reponseEvaluation[formControlName];
  //       if(bisLine && bisLine !== null){
  //         let line = question.bisQuestion;
  //         htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong> - "+line+"</strong>";
  //         htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+bisLine+"</span></p>";
  //       }
  //     }
  //   }
  //   if(question.type == 'multiple-boolean'){
  //     for (var i = 0; i < question.texte.length; i++) {
  //       let line = question.texte[i];
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong> - "+line+" : </strong>";
  //       let formControlName = "reponse" + question.controlName + this.controlsIndexToLetter[i];
  //       if(this.reponseEvaluation[formControlName]){
  //         htmlTexte += "Oui</span></p>";
  //       }else{
  //         htmlTexte += "Non</span></p>";
  //       }
  //     }
  //   }
  //   if(question.type == 'texte'){
  //     let line = this.reponseEvaluation['reponse' + question.controlName];
  //     htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
  //   }
  //   if(question.type == 'EtuI5'){
  //     htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+this.convention.origineStage.libelle+"</span></p>";
  //   }
  //   if(question.type == 'EtuI7'){
  //     if(this.reponseEvaluation['reponse' + question.controlName]){
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Oui</span></p>";
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong>- Si oui, par qui ?</strong></span></p>";
  //       let line = question.texte[this.reponseEvaluation['reponseEtuI7bis1']];
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
  //     }else{
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Non</span></p>";
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong>- Si non, pourquoi ?</strong></span></p>";
  //       let line = question.texte[this.reponseEvaluation['reponseEtuI7bis2']];
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
  //     }
  //   }
  //   if(question.type == 'EtuII5'){
  //     if(this.reponseEvaluation['reponse' + question.controlName]){
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Oui</span></p>";
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong>- Si oui : a) De quel ordre ?</strong></span></p>";
  //       let line = question.texte[this.reponseEvaluation['reponseEtuII5a']];
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
  //       htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong>b) Avec autonomie ?</strong></span></p>";
  //       if(this.reponseEvaluation['reponseEtuII5b']){
  //         htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Oui</span></p>";
  //       }else{
  //         htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Non</span></p>";
  //       }
  //     }
  //   }
  //   if(question.type == 'EtuIII1'){
  //     htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+this.convention.sujetStage+"</span></p>";
  //   }
  //
  //   return htmlTexte;
  // }

  // getQuestionSupplementaireTexte(reponseSupplementaireForm:any, questionSupplementaire: any): string {
  //
  //   let htmlTexte = "";
  //
  //   let line = questionSupplementaire.question;
  //   htmlTexte += "<p style=\"margin-left: 16px\"><span class=\"text-small\"><strong> - "+line+"</strong></span></p>";
  //
  //   if(questionSupplementaire.typeQuestion == 'txt'){
  //     line = reponseSupplementaireForm.get(questionSupplementaire.formControlName)!.value;
  //   }
  //   if(questionSupplementaire.typeQuestion == 'not'){
  //     let notation = reponseSupplementaireForm.get(questionSupplementaire.formControlName)!.value;
  //     if(notation === 0)line = 'Excellent';
  //     if(notation === 1)line = 'Très bien';
  //     if(notation === 2)line = 'Bien';
  //     if(notation === 3)line = 'Satisfaisant';
  //     if(notation === 4)line = 'Insuffisant';
  //   }
  //   if(questionSupplementaire.typeQuestion == 'yn'){
  //     line = (reponseSupplementaireForm.get(questionSupplementaire.formControlName)!.value === true)?'Oui':'Non';
  //   }
  //   htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
  //
  //   return htmlTexte;
  // }

  //-----------------------------------------------| Question |-----------------------------------------------//

  private codeToControlName(code: string): string {
    // ETU I / II / III
    if (code.startsWith('ETU')) {
      if (code.startsWith('ETUIII')) return 'EtuIII' + code.substring(6);
      if (code.startsWith('ETUII'))  return 'EtuII'  + code.substring(5);
      if (code.startsWith('ETUI'))   return 'EtuI'   + code.substring(4);
    }
    // ENS I / II
    if (code.startsWith('ENS')) {
      if (code.startsWith('ENSII'))  return 'EnsII'  + code.substring(5);
      if (code.startsWith('ENSI'))   return 'EnsI'   + code.substring(4);
    }
    // ENT
    if (code.startsWith('ENT')) return 'Ent' + code.substring(3);
    return code; // fallback
  }

  private mapDbTypeToUi(t: TypeQuestionEvaluation):
    'boolean' | 'multiple-choice' | 'multiple-boolean' | 'texte' | 'automatique' {
    switch (t) {
      case TypeQuestionEvaluation.YES_NO:            return 'boolean';
      case TypeQuestionEvaluation.TEXT:              return 'texte';
      case TypeQuestionEvaluation.SINGLE_CHOICE:     return 'multiple-choice';
      case TypeQuestionEvaluation.MULTI_CHOICE:      return 'multiple-boolean';
      case TypeQuestionEvaluation.BOOLEAN_GROUP:     return 'multiple-boolean';
      case TypeQuestionEvaluation.SCALE_LIKERT_5:    return 'multiple-choice';
      case TypeQuestionEvaluation.SCALE_AGREEMENT_5: return 'multiple-choice';
      case TypeQuestionEvaluation.AUTO:              return 'automatique';
      default: return 'texte';
    }
  }

  private extractOptions(q: DbQuestion): string[] {
    if (q.options?.length) return q.options;
    if (q.paramsJson) {
      try {
        const p = JSON.parse(q.paramsJson);
        if (Array.isArray(p.items)) return p.items as string[];
      } catch {}
    }
    if (q.type === TypeQuestionEvaluation.SCALE_LIKERT_5)    return this.LIKERT_5;
    if (q.type === TypeQuestionEvaluation.SCALE_AGREEMENT_5) return this.AGREEMENT_5;

    // Fallbacks legacy – à supprimer quand la DB est corrigée
    const fb: Record<string,string[]> = {
      ETUI2:  ['1 jour à 1 semaine','2 semaines à 1 mois','1 mois à 3 mois','3 mois à 6 mois','+ de 6 mois'],
      ETUI3:  ['1 à 5','6 à 10','11 à 20','20 et plus'],
      ETUI5:  ['Réponse à une offre de stage','Candidature spontanée','Réseau de connaissance','Proposé par le département'],
      ETUI6:  ['Proposé par votre tuteur professionnel','Proposé par votre tuteur enseignant','Élaboré par vous-même','Négocié entre les parties'],
      ETUII1: ['Avant le début du stage','Pendant le stage','À la fin du stage'],
      ETUIII5:['Très au-dessous de vos compétences','Au-dessous de vos compétences','A votre niveau de compétences','Au-dessus de vos compétences','Très au-dessus de vos compétences','Inatteignables'],
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
        const cn = vm.controlName;

        // Étudiant
        if (cn.startsWith('EtuIII')) return section3!;
        if (cn.startsWith('EtuII'))  return section2!;
        if (cn.startsWith('EtuI'))   return section1;

        // Enseignant
        if (cn.startsWith('EnsII'))  return section2!;
        if (cn.startsWith('EnsI'))   return section1;

        // Entreprise
        if (cn.startsWith('Ent')) {
          const num = parseInt(cn.replace('Ent',''), 10);
          if (num <= 9)  return section1;
          if (num <= 14) return section2!;
          return section3!;
        }
        return section1;
      })();

      // Upsert dans la section (on garde éventuels champs bis existants)
      const idx = target.findIndex((x: any) => x.controlName === vm.controlName);
      if (idx > -1) target[idx] = { ...target[idx], ...vm };
      else target.push(vm);

      // Et on crée les controls qui vont avec
      this.ensureFormControls(vm);
    }
  }


  private toVM(q: DbQuestion) {
    const controlName = this.codeToControlName(q.code); // déjà dans ton code
    const uiType = this.mapDbTypeToUi(q.type);
    const texte = (uiType === 'multiple-choice' || uiType === 'multiple-boolean')
      ? this.extractOptions(q)
      : undefined;
    return { title: q.texte, type: uiType, texte, controlName };
  }

// Ajoute les FormControls requis par question, avec les mêmes noms qu’aujourd’hui
  private ensureFormControls(vm: any): void {
    const form =
      vm.controlName.startsWith('Etu') ? this.reponseEtudiantForm :
        vm.controlName.startsWith('Ens') ? this.reponseEnseignantForm :
          this.reponseEntrepriseForm;

    const base = 'reponse' + vm.controlName;

    if (vm.type === 'boolean' || vm.type === 'texte' || vm.type === 'multiple-choice') {
      if (!form.contains(base)) form.addControl(base, new FormControl(null, Validators.required));
      // si tu as des “bis” conditionnels, tu peux ajouter le control ici si besoin
    }

    if (vm.type === 'multiple-boolean') {
      const items = vm.texte || [];
      items.forEach((_: string, i: number) => {
        const key = base + this.controlsIndexToLetter[i]; // a, b, c, d...
        if (!form.contains(key)) form.addControl(key, new FormControl(null, Validators.required));
      });
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
    const questionKey = 'question' + q.controlName;
    const active = !!this.ficheEvaluation?.[questionKey];

    const base = 'reponse' + q.controlName;
    const keys: string[] = [];
    if (q.type === 'multiple-boolean') {
      (q.texte || []).forEach((_: string, i: number) => keys.push(base + this.controlsIndexToLetter[i]));
    } else {
      keys.push(base);
      if (q.bisQuestionLowNotation || q.bisQuestionTrue || q.bisQuestionFalse) keys.push(base + 'bis');
      if (q.controlName === 'EtuI7')  keys.push(base + 'bis1', base + 'bis2');
      if (q.controlName === 'EtuII5') keys.push(base + 'a', base + 'b');
    }
    this.toggleValidators(form, keys, active);
  }

  /** Branche les valueChanges conditionnels une fois les questions présentes */
  private wireConditionalValidators(): void {
    const hook = (form: FormGroup, q: any) => {
      if (q.bisQuestionLowNotation || q.bisQuestionTrue || q.bisQuestionFalse ||
        q.controlName === 'EtuI7' || q.controlName === 'EtuII5') {
        const key = 'reponse' + q.controlName;
        const bisKey = key + 'bis';
        form.get(key)?.valueChanges.subscribe(val => {
          const active = !!this.ficheEvaluation?.['question' + q.controlName];
          if (!active) return;

          if ((q.bisQuestionLowNotation && val >= 3) ||
            (q.bisQuestionTrue && !!val) ||
            (q.bisQuestionFalse && !val)) {
            this.toggleValidators(form, [bisKey], true);
          } else if (q.controlName === 'EtuI7') {
            this.toggleValidators(form, [key + 'bis1'], !!val);
            this.toggleValidators(form, [key + 'bis2'], !val);
          } else if (q.controlName === 'EtuII5') {
            this.toggleValidators(form, [key + 'a', key + 'b'], !!val);
          } else {
            this.toggleValidators(form, [bisKey], false);
          }
        });
      }
    };

    this.FicheEtudiantIQuestions.concat(this.FicheEtudiantIIQuestions, this.FicheEtudiantIIIQuestions)
      .forEach(q => hook(this.reponseEtudiantForm, q));
    this.FicheEnseignantIQuestions.concat(this.FicheEnseignantIIQuestions)
      .forEach(q => hook(this.reponseEnseignantForm, q));
    this.FicheEntrepriseIQuestions.concat(this.FicheEntrepriseIIQuestions, this.FicheEntrepriseIIIQuestions)
      .forEach(q => hook(this.reponseEntrepriseForm, q));
  }
}
