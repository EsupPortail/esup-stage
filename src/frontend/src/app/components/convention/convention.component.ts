import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from "@angular/router";
import { ConventionService } from "../../services/convention.service";
import { MatTabChangeEvent } from "@angular/material/tabs";

@Component({
  selector: 'app-convention',
  templateUrl: './convention.component.html',
  styleUrls: ['./convention.component.scss']
})
export class ConventionComponent implements OnInit {

  convention: any;

  etab: any;
  service: any;

  tabs: any = {
    0: { statut: 0, init: true },
    1: { statut: 0, init: false },
    2: { statut: 0, init: false },
    3: { statut: 0, init: false },
    4: { statut: 0, init: false },
    5: { statut: 0, init: false },
    6: { statut: 0, init: false },
    7: { statut: 0, init: false },
  }

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
          this.majStatus();
        });
      } else {
        // Récupération de la convention correspondant à l'id
        // this.conventionService.getById(pathId).subscribe((response: any) => {
        //   this.convention = response;
        // });
      }
    });
  }

  majStatus(): void {
    if (this.convention.etudiant){
      this.setStatus(0,2);
    }
    if (this.convention.structure){
      this.setStatus(1,2);
    }
    if (this.convention.service){
      this.setStatus(2,2);
    }
    if (this.convention.contact){
      this.setStatus(3,2);
    }

    //TODO setStatus(4,2)

    if (this.convention.enseignant){
      this.setStatus(5,2);
    }
    if (this.convention.signataire){
      this.setStatus(6,2);
    }
  }

  setStatus(key: number, value: number): void {
    this.tabs[key].statut = value;
  }

  setEtab(value: any): void {
    this.etab = value;
  }

  setService(value: any): void {
    this.service = value;
  }

  isCreated(): boolean {
    return this.convention.id && this.convention.id !== 0;
  }

  tabChanged(event: MatTabChangeEvent): void {
    this.tabs[event.index].init = true;
  }

  getProgressValue(key: number): number {
    if (this.tabs[key].statut === 1) return 66;
    if (this.tabs[key].statut === 2) return 100;
    return 33;
  }

  updateConvention(data: any): void {
    this.convention = data;
    this.setStatus(0,2);
  }

}
