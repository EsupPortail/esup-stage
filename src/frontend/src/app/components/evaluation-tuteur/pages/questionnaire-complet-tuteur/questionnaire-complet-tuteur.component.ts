import {Component, OnInit} from '@angular/core';
import {ContenuService} from "../../../../services/contenu.service";
import {EvaluationTuteurService} from "../../services/evaluation-tuteur.service";
import {EvaluationTuteurContextService} from "../../services/evaluation-tuteur-context.service";
import {ConventionEvaluationTuteur} from "../../models/convention-evaluation-tuteur.model";

@Component({
  selector: 'app-questionnaire-complet-tuteur',
  templateUrl: './questionnaire-complet-tuteur.component.html',
  styleUrl: './questionnaire-complet-tuteur.component.scss'
})
export class QuestionnaireCompletTuteurComponent implements OnInit{

  texteQuestionnaireCompletTuteur !: string ;
  texteDemanderRenouvellementTuteur !: string;
  convention!: ConventionEvaluationTuteur;
  token!: string;


  constructor(
    private contenuService: ContenuService,
    private evaluationTuteurService: EvaluationTuteurService,
    private ctx : EvaluationTuteurContextService
  ) {
  }

  ngOnInit(): void {
    this.contenuService.get('TEXTE_QUESTIONNAIRE_COMPLET_TUTEUR').subscribe((response: any) => {
      this.texteQuestionnaireCompletTuteur = response.texte;
    })
    this.contenuService.get('TEXTE_DEMANDER_RENOUVELLEMENT_TUTEUR').subscribe((response: any) => {
      this.texteDemanderRenouvellementTuteur = response.texte;
    })
    this.ctx.convention$.subscribe(c=> {
      if(c != null){
        this.convention = c;
      }
    });
    this.ctx.token$.subscribe(t=>{
      if(t != null){
        this.token = t
      }
    })
  }

  imprimer(){
    this.evaluationTuteurService.getEvaluationPDF(this.token,this.convention.id).subscribe()
  }

  renouvellement() {

  }
}
