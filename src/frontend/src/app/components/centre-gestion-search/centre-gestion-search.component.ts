import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../table/table.component";
import { CentreGestionService } from "../../services/centre-gestion.service";
import { AuthService } from "../../services/auth.service";
import { Router } from "@angular/router";
import { SortDirection } from "@angular/material/sort";

@Component({
  selector: 'app-centre-gestion-search',
  templateUrl: './centre-gestion-search.component.html',
  styleUrls: ['./centre-gestion-search.component.scss']
})
export class CentreGestionSearchComponent implements OnInit, OnDestroy, AfterViewInit {

  columns = ['personnels', 'id', 'nomCentre', 'niveauCentre.libelle', 'validationPedagogique', 'codeConfidentialite', 'action'];
  exportColumns = {
    id: { title: 'Id' },
    nomCentre: { title: 'Nom du centre' },
    niveauCentre: { title: 'Type' },
    validationPedagogique: { title: 'Validation pédagogique' },
    codeConfidentialite: { title: 'Confidentialité' },
  };
  sortColumn = 'id';
  sortDirection: SortDirection = 'asc';
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

  ngAfterViewInit(): void {
    setTimeout(() => {
      const filtersString: string|null = sessionStorage.getItem('centregestion-filters');
      if (filtersString) {
        const savedFilters: any = JSON.parse(filtersString);
        Object.keys(savedFilters).forEach((key: any) => {
          this.appTable?.setFilterValue(key, savedFilters[key].value);
        });
      }
      const pagingString: string|null = sessionStorage.getItem('centregestion-paging');
      if (pagingString) {
        const pagingConfig = JSON.parse(pagingString);
        this.sortColumn = pagingConfig.sortColumn;
        this.sortDirection = pagingConfig.sortOrder;
        this.appTable?.setBackConfig(pagingConfig);
      }
    });
  }

  isPersonnel(data: any) {
    return data.personnels.some((p: any) => p.uidPersonnel == this.currentUser.login);
  }

  editCentre(id: number): void {
    this.router.navigate([`/centre-gestion/${id}`], )
  }

  ngOnDestroy(): void {
    sessionStorage.setItem('centregestion-paging', JSON.stringify({page: this.appTable?.page, pageSize: this.appTable?.pageSize, sortColumn: this.appTable?.sortColumn, sortOrder: this.appTable?.sortOrder}));
    sessionStorage.setItem('centregestion-filters', JSON.stringify(this.appTable?.getFilterValues()))
  }

}
