import { Component, OnInit } from '@angular/core';
import { ContenuService } from "../../services/contenu.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  texteAccueil: string = '';

  constructor(private contenuService: ContenuService) { }

  ngOnInit(): void {
    this.contenuService.get('TEXTE_ACCUEIL').subscribe((response: any) => {
      this.texteAccueil = response.texte;
    })
  }

}
