import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { ConventionService } from "../../../services/convention.service";

@Component({
  selector: 'app-convention-search',
  templateUrl: './convention-search.component.html',
  styleUrls: ['./convention-search.component.scss']
})
export class ConventionSearchComponent implements OnInit {

  columns = ['id', 'etudiant', 'etablissement', 'periode', 'ufr', 'etape', 'validationPedagogique', 'validationConvention', 'avenant', 'annee', 'action']
  sortColumn = 'id';
  filters = [];

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(public conventionService: ConventionService) {
  }

  ngOnInit(): void {
  }

}
