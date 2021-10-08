import { Component, OnInit } from '@angular/core';

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

  constructor() { }

  ngOnInit(): void {
  }

}
