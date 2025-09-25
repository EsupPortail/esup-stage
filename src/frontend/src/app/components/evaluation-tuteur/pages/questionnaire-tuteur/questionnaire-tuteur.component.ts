import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {ViewportScroller} from '@angular/common';
import {ConventionEvaluationTuteur } from "../../models/convention-evaluation-tuteur.model";
import {EvaluationTuteurContextService} from "../../services/evaluation-tuteur-context.service";
import {MessageService} from "../../../../services/message.service";
import {EvaluationTuteurService} from "../../services/evaluation-tuteur.service";


@Component({
  selector: 'app-questionnaire-tuteur',
  templateUrl: './questionnaire-tuteur.component.html',
  styleUrl: './questionnaire-tuteur.component.scss'
})
export class QuestionnaireTuteurComponent implements OnInit{

  controlsIndexToLetter:any = ['a','b','c','d','e','f','g','h']
  convention !: ConventionEvaluationTuteur;
  token !: string;
  ficheEvaluation: any;
  reponseEvaluation: any;
  questionsSupplementaires: any;

  reponseEntrepriseForm: FormGroup;

  protected reponseSupplementaireEntrepriseForm !: FormGroup;

  selectedTab = 0;
  totalTabs = 3;

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

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private viewportScroller: ViewportScroller,
    private ctx: EvaluationTuteurContextService,
    private messageService: MessageService,
    private evaluationTuteurService:EvaluationTuteurService,
  ) {
    this.reponseSupplementaireEntrepriseForm = this.fb.group({});

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
    this.ctx.convention$.subscribe(c=> {
      if(c != null){
        this.convention = c;
        if(c) {
          this.getFicheEvaluation();
          this.getQuestionSupplementaire();
        }
      }
    });
    this.ctx.token$.subscribe(t=>{
      if(t != null){
        this.token = t
      }
    })
  }

  getFicheEvaluation(){

    this.ficheEvaluation = this.convention.ficheEvaluation;

    for (let question of this.FicheEntrepriseIQuestions.concat(this.FicheEntrepriseIIQuestions).concat(this.FicheEntrepriseIIIQuestions)) {
      let key = 'reponse' + question.controlName;
      let questionKey = 'question' + question.controlName;
      if (!this.convention.ficheEvaluation[questionKey]) {
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

    if (this.convention.reponseEvaluation) {
      this.reponseEntrepriseForm.setValue({
        reponseEnt1:this.convention.reponseEvaluation.reponseEnt1,
        reponseEnt1bis:this.convention.reponseEvaluation.reponseEnt1bis,
        reponseEnt2:this.convention.reponseEvaluation.reponseEnt2,
        reponseEnt2bis:this.convention.reponseEvaluation.reponseEnt2bis,
        reponseEnt3:this.convention.reponseEvaluation.reponseEnt3,
        reponseEnt4:this.convention.reponseEvaluation.reponseEnt4,
        reponseEnt4bis:this.convention.reponseEvaluation.reponseEnt4bis,
        reponseEnt5:this.convention.reponseEvaluation.reponseEnt5,
        reponseEnt5bis:this.convention.reponseEvaluation.reponseEnt5bis,
        reponseEnt6:this.convention.reponseEvaluation.reponseEnt6,
        reponseEnt6bis:this.convention.reponseEvaluation.reponseEnt6bis,
        reponseEnt7:this.convention.reponseEvaluation.reponseEnt7,
        reponseEnt7bis:this.convention.reponseEvaluation.reponseEnt7bis,
        reponseEnt8:this.convention.reponseEvaluation.reponseEnt8,
        reponseEnt8bis:this.convention.reponseEvaluation.reponseEnt8bis,
        reponseEnt9:this.convention.reponseEvaluation.reponseEnt9,
        reponseEnt9bis:this.convention.reponseEvaluation.reponseEnt9bis,
        reponseEnt10:this.convention.reponseEvaluation.reponseEnt10,
        reponseEnt10bis:this.convention.reponseEvaluation.reponseEnt10bis,
        reponseEnt11:this.convention.reponseEvaluation.reponseEnt11,
        reponseEnt11bis:this.convention.reponseEvaluation.reponseEnt11bis,
        reponseEnt12:this.convention.reponseEvaluation.reponseEnt12,
        reponseEnt12bis:this.convention.reponseEvaluation.reponseEnt12bis,
        reponseEnt13:this.convention.reponseEvaluation.reponseEnt13,
        reponseEnt13bis:this.convention.reponseEvaluation.reponseEnt13bis,
        reponseEnt14:this.convention.reponseEvaluation.reponseEnt14,
        reponseEnt14bis:this.convention.reponseEvaluation.reponseEnt14bis,
        reponseEnt15:this.convention.reponseEvaluation.reponseEnt15,
        reponseEnt15bis:this.convention.reponseEvaluation.reponseEnt15bis,
        reponseEnt16:this.convention.reponseEvaluation.reponseEnt16,
        reponseEnt16bis:this.convention.reponseEvaluation.reponseEnt16bis,
        reponseEnt17:this.convention.reponseEvaluation.reponseEnt17,
        reponseEnt17bis:this.convention.reponseEvaluation.reponseEnt17bis,
        reponseEnt18:this.convention.reponseEvaluation.reponseEnt18,
        reponseEnt18bis:this.convention.reponseEvaluation.reponseEnt18bis,
        reponseEnt19:this.convention.reponseEvaluation.reponseEnt19,
      });
    }
  }


  getQuestionSupplementaire(): void {
    if(this.convention?.questionsSupplementaires == undefined) return;

    let questionsSupplementaires = this.convention.questionsSupplementaires;

    for(let questionSupplementaire of questionsSupplementaires){
      if(questionSupplementaire.idPlacement == 5 || questionSupplementaire.idPlacement == 6 || questionSupplementaire.idPlacement == 7){
        const questionSupplementaireFormControlName = 'questionSupplementaire' + questionSupplementaire.id
        this.reponseSupplementaireEntrepriseForm.addControl(questionSupplementaireFormControlName, new FormControl(null, Validators.required));
        questionSupplementaire.formControlName = questionSupplementaireFormControlName

        if(this.reponseEvaluation){
          questionSupplementaire.reponse = false;
          if (this.convention.reponseSupplementaires){
            questionSupplementaire.reponse = true;
            if(questionSupplementaire.typeQuestion == 'txt'){
              this.reponseSupplementaireEntrepriseForm.get(questionSupplementaireFormControlName)!.setValue(this.convention.reponseSupplementaires.reponseTxt);
            }
            if(questionSupplementaire.typeQuestion == 'not'){
              this.reponseSupplementaireEntrepriseForm.get(questionSupplementaireFormControlName)!.setValue(this.convention.reponseSupplementaires.reponseInt);
            }
            if(questionSupplementaire.typeQuestion == 'yn'){
              this.reponseSupplementaireEntrepriseForm.get(questionSupplementaireFormControlName)!.setValue(this.convention.reponseSupplementaires.reponseBool);
            }
          }
        }
      }
    }

    this.questionsSupplementaires = [];
    this.questionsSupplementaires[5] = this.convention.questionsSupplementaires.filter((q: any) => q.idPlacement == 5);
    this.questionsSupplementaires[6] = this.convention.questionsSupplementaires.filter((q: any) => q.idPlacement == 6);
    this.questionsSupplementaires[7] = this.convention.questionsSupplementaires.filter((q: any) => q.idPlacement == 7);
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

  get isLastTab(): boolean {
    return this.selectedTab >= this.totalTabs - 1;
  }

  prevTab(): void {
    if (this.selectedTab > 0) {
      this.selectedTab--;
      this.scrollTop()
    }
  }

  nextOrFinish(): void {
    if (this.isLastTab) {
      this.terminer();
    } else {
      this.selectedTab++;
      this.scrollTop()
    }
  }

  saveReponse(): void {
    const source = Array.isArray(this.questionsSupplementaires) ? this.questionsSupplementaires : [];
    const questionsSupplementaires = [5, 6, 7].reduce<any[]>((acc, i) => {
      const arr = Array.isArray(source[i]) ? source[i] : [];
      return acc.concat(arr);
    }, []);

    const data = {...this.reponseEntrepriseForm.value};

    for(let questionSupplementaire of questionsSupplementaires){
      let reponseSupplementaireData = {'reponseTxt':null,'reponseInt':null,'reponseBool':null,};
      if(questionSupplementaire.typeQuestion == 'txt'){
        reponseSupplementaireData.reponseTxt = this.reponseSupplementaireEntrepriseForm.get(questionSupplementaire.formControlName)!.value;
      }
      if(questionSupplementaire.typeQuestion == 'not'){
        reponseSupplementaireData.reponseInt = this.reponseSupplementaireEntrepriseForm.get(questionSupplementaire.formControlName)!.value;
      }
      if(questionSupplementaire.typeQuestion == 'yn'){
        reponseSupplementaireData.reponseBool = this.reponseSupplementaireEntrepriseForm.get(questionSupplementaire.formControlName)!.value;
      }
      if(questionSupplementaire.reponse){
        this.evaluationTuteurService.updateReponseSupplementaire(this.token, this.convention.id, questionSupplementaire.id, reponseSupplementaireData).subscribe((response: any) => {
        });
      }else{
        this.evaluationTuteurService.createReponseSupplementaire(this.token,this.convention.id, questionSupplementaire.id, reponseSupplementaireData).subscribe((response: any) => {
        });
      }
    }
    if(this.reponseEvaluation){
      this.evaluationTuteurService.updateReponse(this.token,this.convention.id, data).subscribe((response: any) => {
        this.reponseEvaluation = response;
      });
    }else{
      this.evaluationTuteurService.createReponse(this.token,this.convention.id, data).subscribe((response: any) => {
        this.reponseEvaluation = response;
      });
    }
  }

  terminer(): void {
    const valid = this.reponseEntrepriseForm.valid && this.reponseSupplementaireEntrepriseForm.valid
    this.saveReponse()
    this.evaluationTuteurService.validate(this.token,this.convention.id,valid).subscribe()
    console.log("terminer")
    this.router.navigate(['/evaluation-tuteur', this.convention.id, 'terminee'], {
      relativeTo: this.route.parent,
      replaceUrl: true
    });
  }

  enregistrer(): void {
    this.saveReponse();
    this.messageService.setSuccess('Evaluation enregistrée avec succès');
  }

  private scrollTop(): void {
    requestAnimationFrame(() => this.viewportScroller.scrollToAnchor('page-top'));
  }
}
