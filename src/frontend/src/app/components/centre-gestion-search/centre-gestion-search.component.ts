import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../table/table.component";
import { CentreGestionService } from "../../services/centre-gestion.service";
import { AuthService } from "../../services/auth.service";
import { Router } from "@angular/router";

@Component({
  selector: 'app-centre-gestion-search',
  templateUrl: './centre-gestion-search.component.html',
  styleUrls: ['./centre-gestion-search.component.scss']
})
export class CentreGestionSearchComponent implements OnInit {

  columns = ['personnels', 'id', 'nomCentre', 'niveauCentre.libelle', 'validationPedagogique', 'codeConfidentialite', 'action'];
  exportColumns = {
    id: { title: 'Id' },
    nomCentre: { title: 'Nom du centre' },
    niveauCentre: { title: 'Type' },
    validationPedagogique: { title: 'Validation pédagogique' },
    codeConfidentialite: { title: 'Confidentialité' },
  };
  sortColumn = 'id';
  filters = [
    { id: 'nomCentre', libelle: 'Nom du centre de gestion' }
  ];

  data: any;
  currentUser: any;

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public centreGestionService: CentreGestionService,
    public authService: AuthService,
    private router: Router) { }

  ngOnInit(): void {
    this.authService.getCurrentUser().subscribe(response => {
      this.currentUser = response;
    });
  }

  isPersonnel(data: any) {
    return data.personnels.some((p: any) => p.uidPersonnel == this.currentUser.login);
  }

  editCentre(id: number): void {
    this.router.navigate([`/centre-gestion/${id}`], )
  }

}
