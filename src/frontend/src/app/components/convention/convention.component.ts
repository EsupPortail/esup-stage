import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-convention',
  templateUrl: './convention.component.html',
  styleUrls: ['./convention.component.scss']
})
export class ConventionComponent implements OnInit {

  statuts: any = {
    statutEtudiant: 0,
    statutEtabAccueil: 0,
    statutServiceAccueil: 0,
    statutTuteurPro: 0,
    statutStage: 0,
    statutEnseignantRef: 0,
    statutSignataire: 0,
    statutRecap: 0,
  };

  constructor() { }

  ngOnInit(): void {
    // TODO maj des statuts
  }

  setStatus(statut: string, value: number): void {
    this.statuts[statut] = value;
  }

}
