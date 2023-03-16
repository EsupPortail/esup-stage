import { Component, EventEmitter, Input, OnChanges, OnInit, Output, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { EtudiantGroupeEtudiantService } from "../../../services/etudiant-groupe-etudiant.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import { MessageService } from "../../../services/message.service";
import { SortDirection } from "@angular/material/sort";
import { MatLegacyDialog as MatDialog, MatLegacyDialogConfig as MatDialogConfig } from '@angular/material/legacy-dialog';
import { CadreStageModalComponent } from './cadre-stage-modal/cadre-stage-modal.component';

@Component({
  selector: 'app-cadre-stage',
  templateUrl: './cadre-stage.component.html',
  styleUrls: ['./cadre-stage.component.scss']
})
export class CadreStageComponent implements OnInit, OnChanges {

  columns: string[] = [];
  sortColumn = 'prenom';
  sortDirection: SortDirection = 'desc';
  filters: any[] = [];

  @Input() sharedData: any;
  @Input() groupeEtudiant: any;
  @Output() validated = new EventEmitter<any>();

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public groupeEtudiantService: GroupeEtudiantService,
    public etudiantGroupeEtudiantService: EtudiantGroupeEtudiantService,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
    public matDialog: MatDialog,
  ) {
  }

  ngOnInit(): void {
    this.columns = ['action','numEtudiant','nom', 'mail', 'mailPerso', 'ufr.libelle', 'etape.libelle', 'annee', 'adresse', 'codePostal', 'commune', 'pays', 'tel', 'telPortable'];
    this.filters = [...this.sharedData.filters];
  }

  ngOnChanges(): void{
    this.appTable?.update();
  }

  edit(row: any): void{
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.height = '1000px';
    dialogConfig.data = {convention: row.convention};
    const modalDialog = this.matDialog.open(CadreStageModalComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        this.messageService.setSuccess('Cadre du stage édité avec succès');
        this.groupeEtudiantService.getById(this.groupeEtudiant.id).subscribe((response: any) => {
          this.validated.emit(response);
        });
      }
    });
  }
}
