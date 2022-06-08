import { Component, EventEmitter, Input, Output, OnChanges, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { ConventionService } from "../../../services/convention.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import { StructureService } from "../../../services/structure.service";
import { UfrService } from "../../../services/ufr.service";
import { EtapeService } from "../../../services/etape.service";
import { EtudiantGroupeEtudiantService } from "../../../services/etudiant-groupe-etudiant.service";
import { MessageService } from "../../../services/message.service";
import { ConfigService } from "../../../services/config.service";
import { SortDirection } from "@angular/material/sort";
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { EtabAccueilGroupeModalComponent } from './etab-accueil-groupe-modal/etab-accueil-groupe-modal.component';

@Component({
  selector: 'app-etab-accueil-groupe',
  templateUrl: './etab-accueil-groupe.component.html',
  styleUrls: ['./etab-accueil-groupe.component.scss']
})
export class EtabAccueilGroupeComponent implements OnInit {

  columns: string[] = [];
  sortColumn = 'prenom';
  sortDirection: SortDirection = 'desc';
  filters: any[] = [];
  selected: any[] = [];

  anneeEnCours: any|undefined;
  annees: any[] = [];


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
    this.columns = ['select','numEtudiant','nom', 'prenom', 'mail', 'etab'];
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
    dialogConfig.width = '1200px';
    dialogConfig.height = '1000px';
    dialogConfig.data = {};
    const modalDialog = this.matDialog.open(EtabAccueilGroupeModalComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        this.updateEtab(this.groupeEtudiant.convention.id,dialogResponse)
      }
    });
  }

  selectForSelected(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1200px';
    dialogConfig.height = '1000px';
    dialogConfig.data = {};
    const modalDialog = this.matDialog.open(EtabAccueilGroupeModalComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        for(const etu of this.selected){
          this.updateEtab(etu.convention.id,dialogResponse);
        }
      }
    });
  }

  importCsv(): void {
  }

  updateEtab(conventionId: number, etabId: number): void {
    const data = {
      "field":'idStructure',
      "value":etabId,
    };
    this.conventionService.patch(conventionId, data).subscribe((response: any) => {
        this.messageService.setSuccess('Structure d\'accueil affectée au groupe avec succès');
        this.groupeEtudiantService.getById(this.groupeEtudiant.id).subscribe((response: any) => {
          this.validated.emit(response);
        });
    });
  }
}
