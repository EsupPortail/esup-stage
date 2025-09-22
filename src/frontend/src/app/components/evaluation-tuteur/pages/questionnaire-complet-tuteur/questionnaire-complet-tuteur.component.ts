import {Component, OnInit} from '@angular/core';
import {ContenuService} from "../../../../services/contenu.service";

@Component({
  selector: 'app-questionnaire-complet-tuteur',
  templateUrl: './questionnaire-complet-tuteur.component.html',
  styleUrl: './questionnaire-complet-tuteur.component.scss'
})
export class QuestionnaireCompletTuteurComponent implements OnInit{

  texteQuestionnaireCompletTuteur !: string ;
  texteDemanderRenouvellementTuteur !: string;

  constructor(
    private contenuService: ContenuService,
  ) {
  }

  ngOnInit(): void {
    this.contenuService.get('TEXTE_QUESTIONNAIRE_COMPLET_TUTEUR').subscribe((response: any) => {
      this.texteQuestionnaireCompletTuteur = response.texte;
    })
    this.contenuService.get('TEXTE_DEMANDER_RENOUVELLEMENT_TUTEUR').subscribe((response: any) => {
      this.texteDemanderRenouvellementTuteur = response.texte;
    })
  }

  imprimer(){
    console.log('imprimer')
  }

  renouvellement() {

  }
}
