import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ConventionService} from "../../../../services/convention.service";
import {ReponseEvaluationService} from "../../../../services/reponse-evaluation.service";
import {FicheEvaluationService} from "../../../../services/fiche-evaluation.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-questionnaire-tuteur',
  templateUrl: './questionnaire-tuteur.component.html',
  styleUrl: './questionnaire-tuteur.component.scss'
})
export class QuestionnaireTuteurComponent implements OnInit{

  FicheEntrepriseIQuestions: any = [
    {
      title: "Adaptation au milieu professionnel :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent1",
    },
    {
      title: "Intégration au groupe de travail :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent2",
    },
    {
      title: "Assiduité - ponctualité :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      controlName: "Ent3",
    },
    {
      title: "Intérêt pour l'établissement, les services, et les métiers :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent5",
    },
    {
      title: "Sens de l'organisation :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent9",
    },
    {
      title: "Capacité d'autonomie :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent11",
    },
    {
      title: "Initiative personnelle :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent12",
    },
    {
      title: "Implication :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent13",
    },
    {
      title: "Rigueur et précision dans le travail :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent14",
    },
  ]

  FicheEntrepriseIIQuestions: any = [
    {
      title: "Aptitude à cerner et situer le projet :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent4",
    },
    {
      title: "Aptitude à appliquer ses connaissances :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent6",
    },
    {
      title: "Esprit d'observation et pertinence des remarques :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent7",
    },
    {
      title: "Esprit de synthèse :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent8",
    },
    {
      title: "Aptitude à la communication :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent15",
    },
  ]

  FicheEntrepriseIIIQuestions: any = [
    {
      title: "Les objectifs ont-ils été atteints ?",
      type: "multiple-choice",
      texte: [
        "Tout à fait d'accord",
        "Plutôt d'accord",
        "Sans avis",
        "Plutôt pas d'accord",
        "Pas du tout d'accord",
      ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent16",
    },
    {
      title: "Indiquez votre appréciation générale de ce stage :",
      type: "multiple-choice",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      bisQuestionLowNotation:"Pour quelles raisons ?",
      controlName: "Ent17",
    },
    {
      title: "Observations :",
      type: "texte",
      texte: [
        "Champ de texte libre",
      ],
      controlName: "Ent19",
    },
    {
      title: "Avez-vous remis au stagiaire une attestation de stage ?",
      type: "boolean",
      texte: [
        "Oui / Non",
      ],
      controlName: "Ent10",
    },
    {
      title: "Accepteriez-vous de reprendre un de nos étudiants en stage ?",
      type: "boolean",
      texte: [
        "Oui / Non",
      ],
      bisQuestionFalse:"Pour quelles raisons ?",
      controlName: "Ent18",
    },
  ]

  controlsIndexToLetter:any = ['a','b','c','d','e','f','g','h']
  conventionId : number = 130160; // pour test
  convention : any;
  ficheEvaluation: any;
  reponseEvaluation: any;
  questionsSupplementaires: any;
  //@Input() convention: any;
  //@Output() conventionChange = new EventEmitter<any>();

  reponseEntrepriseForm: FormGroup;

  protected reponseSupplementaireEntrepriseForm: FormGroup;

  selectedTab: any;

  constructor(
    private conventionService : ConventionService,
    private reponseEvaluationService : ReponseEvaluationService,
    private ficheEvaluationService : FicheEvaluationService,
    private fb: FormBuilder,
  ) {
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
  }

  ngOnInit(): void {
    this.conventionService.getById(130186).subscribe(response => {
      this.convention = response;
      this.getFicheEvaluation();
      console.log(this.convention);
    });
  }

  getFicheEvaluation(){
    console.log(this.convention);
    this.ficheEvaluationService.getByCentreGestion(this.convention.centreGestion.id).subscribe((response: any) => {

      this.ficheEvaluation = response;


      for (let question of this.FicheEntrepriseIQuestions.concat(this.FicheEntrepriseIIQuestions).concat(this.FicheEntrepriseIIIQuestions)) {
        let key = 'reponse' + question.controlName;
        let questionKey = 'question' + question.controlName;
        if (!this.ficheEvaluation[questionKey]) {
          if (question.type == 'multiple-boolean') {
            for (var i = 0; i < question.texte.length; i++) {
              let key = "reponse" + question.controlName + this.controlsIndexToLetter[i];
              this.toggleValidators(this.reponseEntrepriseForm, [key], false);
            }
          } else {
            let key = 'reponse' + question.controlName;
            this.toggleValidators(this.reponseEntrepriseForm, [key], false);
          }
        }
      }

      if (this.ficheEvaluation) {
        this.reponseEvaluationService.getByConvention(this.convention.id).subscribe((response2: any) => {
          this.reponseEvaluation = response2;
          this.getQuestionSupplementaire();
          if (this.reponseEvaluation) {
            this.reponseEntrepriseForm.setValue({
              reponseEnt1: this.reponseEvaluation.reponseEnt1,
              reponseEnt1bis: this.reponseEvaluation.reponseEnt1bis,
              reponseEnt2: this.reponseEvaluation.reponseEnt2,
              reponseEnt2bis: this.reponseEvaluation.reponseEnt2bis,
              reponseEnt3: this.reponseEvaluation.reponseEnt3,
              reponseEnt4: this.reponseEvaluation.reponseEnt4,
              reponseEnt4bis: this.reponseEvaluation.reponseEnt4bis,
              reponseEnt5: this.reponseEvaluation.reponseEnt5,
              reponseEnt5bis: this.reponseEvaluation.reponseEnt5bis,
              reponseEnt6: this.reponseEvaluation.reponseEnt6,
              reponseEnt6bis: this.reponseEvaluation.reponseEnt6bis,
              reponseEnt7: this.reponseEvaluation.reponseEnt7,
              reponseEnt7bis: this.reponseEvaluation.reponseEnt7bis,
              reponseEnt8: this.reponseEvaluation.reponseEnt8,
              reponseEnt8bis: this.reponseEvaluation.reponseEnt8bis,
              reponseEnt9: this.reponseEvaluation.reponseEnt9,
              reponseEnt9bis: this.reponseEvaluation.reponseEnt9bis,
              reponseEnt10: this.reponseEvaluation.reponseEnt10,
              reponseEnt10bis: this.reponseEvaluation.reponseEnt10bis,
              reponseEnt11: this.reponseEvaluation.reponseEnt11,
              reponseEnt11bis: this.reponseEvaluation.reponseEnt11bis,
              reponseEnt12: this.reponseEvaluation.reponseEnt12,
              reponseEnt12bis: this.reponseEvaluation.reponseEnt12bis,
              reponseEnt13: this.reponseEvaluation.reponseEnt13,
              reponseEnt13bis: this.reponseEvaluation.reponseEnt13bis,
              reponseEnt14: this.reponseEvaluation.reponseEnt14,
              reponseEnt14bis: this.reponseEvaluation.reponseEnt14bis,
              reponseEnt15: this.reponseEvaluation.reponseEnt15,
              reponseEnt15bis: this.reponseEvaluation.reponseEnt15bis,
              reponseEnt16: this.reponseEvaluation.reponseEnt16,
              reponseEnt16bis: this.reponseEvaluation.reponseEnt16bis,
              reponseEnt17: this.reponseEvaluation.reponseEnt17,
              reponseEnt17bis: this.reponseEvaluation.reponseEnt17bis,
              reponseEnt18: this.reponseEvaluation.reponseEnt18,
              reponseEnt18bis: this.reponseEvaluation.reponseEnt18bis,
              reponseEnt19: this.reponseEvaluation.reponseEnt19,
            });
          }
        });
      }
    });
  }

  getQuestionSupplementaire(): void {

    this.ficheEvaluationService.getQuestionsSupplementaires(this.ficheEvaluation.id).subscribe((response: any) => {

      let questionsSupplementaires = response;

      for(let questionSupplementaire of questionsSupplementaires){

        let form = this.fb.group({});

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
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 5));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 6));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 7));
    });
  }

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
}
