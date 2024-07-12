import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { TableComponent } from "../table/table.component";
import { CentreGestionService } from "../../services/centre-gestion.service";
import { MessageService } from "../../services/message.service";
import { AuthService } from "../../services/auth.service";
import { Router } from "@angular/router";
import { SortDirection } from "@angular/material/sort";
import { ConfirmDeleteCentreComponent } from './confirm-delete-centre/confirm-delete-centre.component';

@Component({
  selector: 'app-centre-gestion-search',
  templateUrl: './centre-gestion-search.component.html',
  styleUrls: ['./centre-gestion-search.component.scss']
})
export class CentreGestionSearchComponent implements OnInit, OnDestroy, AfterViewInit {

  isAdmin:boolean = false;	
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
    private messageService: MessageService,
    public matDialog: MatDialog,
    private router: Router) {
  }
  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();	  
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
    return data.personnels.some((p: any) => p.uidPersonnel == this.currentUser.uid);
  }

  editCentre(id: number): void {
    this.router.navigate([`/centre-gestion/${id}`], )
  }


  openDeleteFormModal(id: number) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {centreGestionId: id};
    const modalDialog = this.matDialog.open(ConfirmDeleteCentreComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        this.delete(id);
      }
    });
  }

  delete(id: number): void {
    this.centreGestionService.delete(id).subscribe(response => {
        this.messageService.setSuccess('Le centre de gestion a été supprimé avec succès');
        this.appTable?.update();
    });
  }

  ngOnDestroy(): void {
    sessionStorage.setItem('centregestion-paging', JSON.stringify({page: this.appTable?.page, pageSize: this.appTable?.pageSize, sortColumn: this.appTable?.sortColumn, sortOrder: this.appTable?.sortOrder}));
    sessionStorage.setItem('centregestion-filters', JSON.stringify(this.appTable?.getFilterValues()))
  }

}
