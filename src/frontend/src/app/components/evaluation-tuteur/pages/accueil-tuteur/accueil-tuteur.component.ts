import { Component, OnInit } from '@angular/core';
import { ContenuService } from "../../../../services/contenu.service";
import {ActivatedRoute, Router } from "@angular/router";

@Component({
  selector: 'app-accueil-tuteur',
  templateUrl: './accueil-tuteur.component.html',
  styleUrl: './accueil-tuteur.component.scss'
})
export class AccueilTuteurComponent implements OnInit {

  texteAccueilEvalTuteur: string = '';

  constructor(
    private contenuService: ContenuService,
    private router: Router,
    private route: ActivatedRoute,
  ) {
  }

  ngOnInit(): void {
    this.contenuService.get('TEXTE_ACCUEIL_TUTEUR').subscribe((response: any) => {
      this.texteAccueilEvalTuteur = response.texte;
    })
  }

  commencerEvaluation(){
    this.router.navigate(['questionnaire'],
      {
        relativeTo: this.route,
        queryParamsHandling: 'merge'
      });
  }
}
