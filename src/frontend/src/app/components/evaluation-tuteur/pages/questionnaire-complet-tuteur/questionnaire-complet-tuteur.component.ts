import {Component, OnInit} from '@angular/core';
import {ContenuService} from "../../../../services/contenu.service";
import {EvaluationTuteurService} from "../../services/evaluation-tuteur.service";
import {EvaluationTuteurContextService} from "../../services/evaluation-tuteur-context.service";
import {ConventionEvaluationTuteur} from "../../models/convention-evaluation-tuteur.model";
import {MessageService} from "../../../../services/message.service";
import * as FileSaver from "file-saver";

@Component({
    selector: 'app-questionnaire-complet-tuteur',
    templateUrl: './questionnaire-complet-tuteur.component.html',
    styleUrl: './questionnaire-complet-tuteur.component.scss',
    standalone: false
})
export class QuestionnaireCompletTuteurComponent implements OnInit{

  texteQuestionnaireCompletTuteur !: string ;
  texteDemanderRenouvellementTuteur !: string;
  convention!: ConventionEvaluationTuteur;
  token!: string;


  constructor(
    private contenuService: ContenuService,
    private evaluationTuteurService: EvaluationTuteurService,
    private ctx : EvaluationTuteurContextService,
    private messageService: MessageService
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
    this.evaluationTuteurService.getEvaluationPDF(this.token,this.convention.id).subscribe(r=>{
      var blob = new Blob([r as BlobPart], {type: "application/pdf"});
      let filename = 'Questionnaire_'+this.convention.id + '.pdf';
      FileSaver.saveAs(blob, filename);
    });
  }

  renouvellement() {
    this.evaluationTuteurService.getReouvellement(this.token,this.convention.id).subscribe(
      r => {
        this.messageService.setSuccess('Renouvellement effectué avec succès.');
      }
    )
  }
}
