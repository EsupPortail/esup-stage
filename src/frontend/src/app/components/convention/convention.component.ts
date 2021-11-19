import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from "@angular/router";
import { ConventionService } from "../../services/convention.service";
import { MatTabChangeEvent } from "@angular/material/tabs";
import { TitleService } from "../../services/title.service";

@Component({
  selector: 'app-convention',
  templateUrl: './convention.component.html',
  styleUrls: ['./convention.component.scss']
})
export class ConventionComponent implements OnInit {

  convention: any;

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

  constructor(private activatedRoute: ActivatedRoute, private conventionService: ConventionService, private titleService: TitleService) { }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((param: any) => {
      const pathId = param.id;
      if (pathId === 'create') {
        this.titleService.title = 'Création d\'une convention';
        // Récupération de la convention au mode brouillon
        this.conventionService.getBrouillon().subscribe((response: any) => {
          this.convention = response;
          this.majStatus();
        });
      } else {
        this.titleService.title = 'Gestion d\'une convention';
        // Récupération de la convention correspondant à l'id
        this.conventionService.getById(pathId).subscribe((response: any) => {
          this.convention = response;
        });
      }
    });
  }

  majStatus(): void {
    if (this.convention.etudiant){
      this.setStatus(0,2);
    }else{
      this.setStatus(0,0);
    }
    if (this.convention.structure){
      this.setStatus(1,2);
    }else{
      this.setStatus(1,0);
    }
    if (this.convention.service){
      this.setStatus(2,2);
    }else{
      this.setStatus(2,0);
    }
    if (this.convention.contact){
      this.setStatus(3,2);
    }else{
      this.setStatus(3,0);
    }

    //TODO setStatus(4,2)

    if (this.convention.enseignant){
      this.setStatus(5,2);
    }else{
      this.setStatus(5,0);
    }
    if (this.convention.signataire){
      this.setStatus(6,2);
    }else{
      this.setStatus(6,0);
    }
  }

  setStatus(key: number, value: number): void {
    this.tabs[key].statut = value;
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

  updateEtab(data: any): void {
    this.updateSingleField('idStructure',data.id);
  }

  updateService(data: any): void {
    this.updateSingleField('idService',data.id);
  }

  updateTuteurPro(data: any): void {
    this.updateSingleField('idContact',data.id);
  }

  updateStage(data: any): void {
    //TODO
  }

  updateEnseignant(data: any): void {
    this.updateSingleField('idEnseignant',data.id);
  }

  updateSignataire(data: any): void {
    this.updateSingleField('idSignataire',data.id);
  }

  updateSingleField(key: string, value: any): void {
    const data = {
      "field":key,
      "value":value,
    };
    console.log('data: ' + JSON.stringify(data, null, 2));
    this.conventionService.patch(this.convention.id, data).subscribe((response: any) => {
      this.convention = response;
      this.majStatus();
    });
  }

  isValide(): boolean {
    return this.convention && (this.convention.validationPedagogique || this.convention.validationConvention); // TODO valider avec Claude
  }

}
