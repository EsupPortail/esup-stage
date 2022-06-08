import { Component, EventEmitter, Input, Output, OnChanges, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { ConventionService } from "../../../services/convention.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import { EtudiantGroupeEtudiantService } from "../../../services/etudiant-groupe-etudiant.service";
import { MessageService } from "../../../services/message.service";
import { ConfigService } from "../../../services/config.service";
import { SortDirection } from "@angular/material/sort";
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { EnseignantGroupeModalComponent } from './enseignant-groupe-modal/enseignant-groupe-modal.component';

@Component({
  selector: 'app-enseignant-groupe',
  templateUrl: './enseignant-groupe.component.html',
  styleUrls: ['./enseignant-groupe.component.scss']
})
export class EnseignantGroupeComponent implements OnInit {

  columns: string[] = [];
  sortColumn = 'prenom';
  sortDirection: SortDirection = 'desc';
  filters: any[] = [];
  selected: any[] = [];

  @Input() groupeEtudiant: any;
  @Output() validated = new EventEmitter<any>();

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public groupeEtudiantService: GroupeEtudiantService,
    public etudiantGroupeEtudiantService: EtudiantGroupeEtudiantService,
    private conventionService: ConventionService,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
    private configService: ConfigService,
    public matDialog: MatDialog,
  ) {
  }

  ngOnInit(): void {
    this.columns = ['select','numEtudiant','nom', 'prenom', 'mail', 'enseignant'];
    this.filters = [
        { id: 'etudiant.nom', libelle: 'Nom'},
        { id: 'etudiant.prenom', libelle: 'Prénom'},
        { id: 'etudiant.numEtudiant', libelle: 'N° étudiant'},
    ];
  }

  ngOnChanges(): void{
      this.appTable?.update();
      this.selected = [];
  }

  isSelected(data: any): boolean {
    return this.selected.find((r: any) => {return r.id === data.id}) !== undefined;
  }

  toggleSelected(data: any): void {
    const index = this.selected.findIndex((r: any) => {return r.id === data.id});
    if (index > -1) {
      this.selected.splice(index, 1);
    } else {
      this.selected.push(data);
    }
  }

  masterToggle(): void {
    if (this.isAllSelected()) {
      this.selected = [];
      return;
    }
    this.appTable?.data.forEach((d: any) => {
      const index = this.selected.findIndex((s: any) => s.id === d.id);
      if (index === -1) {
        this.selected.push(d);
      }
    });
  }

  isAllSelected(): boolean {
    let allSelected = true;
    if(this.appTable?.data){
      this.appTable?.data.forEach((data: any) => {
        const index = this.selected.findIndex((r: any) => {return r.id === data.id});
        if (index === -1) {
           allSelected = false;
        }
      });
    }
    return allSelected;
  }

  selectForGroup(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {};
    const modalDialog = this.matDialog.open(EnseignantGroupeModalComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        this.updateService(this.groupeEtudiant.convention.id,dialogResponse)
      }
    });
  }

  selectForSelected(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {};
    const modalDialog = this.matDialog.open(EnseignantGroupeModalComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        for(const etu of this.selected){
          this.updateService(etu.convention.id,dialogResponse);
        }
      }
    });
  }

  updateService(conventionId: number, serviceId: number): void {
    const data = {
      "field":'idService',
      "value":serviceId,
    };
    this.conventionService.patch(conventionId, data).subscribe((response: any) => {
        this.messageService.setSuccess('Service d\'accueil affectée au groupe avec succès');
        this.groupeEtudiantService.getById(this.groupeEtudiant.id).subscribe((response: any) => {
          this.validated.emit(response);
        });
    });
  }
}
