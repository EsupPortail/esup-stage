import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from "@angular/router";
import { ConventionService } from "../../services/convention.service";

@Component({
  selector: 'app-convention',
  templateUrl: './convention.component.html',
  styleUrls: ['./convention.component.scss']
})
export class ConventionComponent implements OnInit {

  convention: any;

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

  allValid = false;

  constructor(private activatedRoute: ActivatedRoute, private conventionService: ConventionService) { }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((param: any) => {
      const pathId = param.id;
      // TODO création/récupération de la convention
      if (pathId === 'create') {
        // Récupération de la convention au mode brouillon
        this.conventionService.getBrouillon().subscribe((response: any) => {
          this.convention = response;
        });
      } else {
        // Récupération de la convention correspondant à l'id
        // this.conventionService.getById(pathId).subscribe((response: any) => {
        //   this.convention = response;
        // });
      }
    });
    // TODO maj des statuts
  }

  setStatus(statut: string, value: number): void {
    this.statuts[statut] = value;
  }

}
