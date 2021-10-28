import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from "@angular/router";
import { CentreGestionService } from "../../services/centre-gestion.service";
import { MatTabChangeEvent } from "@angular/material/tabs";

@Component({
  selector: 'app-centre-gestion',
  templateUrl: './centre-gestion.component.html',
  styleUrls: ['./centre-gestion.component.scss']
})
export class CentreGestionComponent implements OnInit {

  statuts: any = {
    statutCoordCentre: 0,
    statutParamCentre: 0,
    statutPersoCentre: 0,
    statutRattachGest: 0,
    statutAlerteGest: 0,
  };

  initedTabs = [0];
  centreGestion: any;
  centreGestionInited = false;

  constructor(private activatedRoute: ActivatedRoute, private centreGestionService: CentreGestionService) { }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((param: any) => {
      const pathId = param.id;
      if (pathId === 'create') {
        this.centreGestionService.getBrouillonByLogin().subscribe((response: any) => {
          this.centreGestion = response;
          this.centreGestionInited = true;
        });
      } else {
        // todo edit
      }
    });
  }

  tabChanged(event: MatTabChangeEvent): void {
    if (this.initedTabs.indexOf(event.index) === -1) {
      this.initedTabs.push(event.index);
    }
  }

  hasInit(origin: number|null): boolean {
    if (origin) {
      return this.initedTabs.indexOf(origin) > -1;
    }
    return false;
  }

  getProgressValue(key: string): number {
    if (this.statuts[key] === 1) return 66;
    if (this.statuts[key] === 2) return 100;
    return 33;
  }

}
