import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../table/table.component";
import { ConventionService } from "../../services/convention.service";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  columns = ['id', 'etudiant', 'etablissement', 'periode', 'ufr', 'etape', 'validationPedagogique', 'validationConvention', 'avenant', 'annee', 'action']
  sortColumn = 'id';
  filters = [];

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(public conventionService: ConventionService) {
  }

  ngOnInit(): void {
  }

}
