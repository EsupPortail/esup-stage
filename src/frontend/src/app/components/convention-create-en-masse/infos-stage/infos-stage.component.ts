import { Component, EventEmitter, Input, Output, OnChanges, OnDestroy, OnInit, AfterViewInit,  ViewChild } from '@angular/core';
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
import { InfosStageModalComponent } from './infos-stage-modal/infos-stage-modal.component';

@Component({
  selector: 'app-infos-stage',
  templateUrl: './infos-stage.component.html',
  styleUrls: ['./infos-stage.component.scss']
})
export class InfosStageComponent implements OnInit {

  columns: string[] = [];
  sortColumn = 'prenom';
  sortDirection: SortDirection = 'desc';
  filters: any[] = [];
  selected: any[] = [];

  @Input() sharedData: any;
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
    this.columns = ['select','numEtudiant','nom', 'prenom', 'mail'];
    this.filters = this.sharedData.filters;
  }

  ngOnChanges(): void{
    this.appTable?.update();
    this.selected = [];
  }

  ngAfterViewInit(): void {
      this.appTable?.setFilterValue('groupeEtudiant.id', this.groupeEtudiant.id);
      this.appTable?.setFilterOption('ufr.id', this.sharedData.ufrList);
      this.appTable?.setFilterOption('etape.id', this.sharedData.etapeList);
      this.appTable?.setFilterOption('convention.annee', this.sharedData.annees);
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
    dialogConfig.height = '1000px';
    dialogConfig.data = {convention:this.groupeEtudiant.convention,groupeEtudiant:this.groupeEtudiant};
    const modalDialog = this.matDialog.open(InfosStageModalComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      this.refreshGroupeEtudiant(dialogResponse);
    });
  }

  selectForSelected(): void {
    if(this.selected.length == 1){
      const dialogConfig = new MatDialogConfig();
      dialogConfig.width = '1000px';
    dialogConfig.height = '1000px';
      const etu = this.selected[0];
      dialogConfig.data = {convention:etu.convention,groupeEtudiant:null};
      const modalDialog = this.matDialog.open(InfosStageModalComponent, dialogConfig);
      modalDialog.afterClosed().subscribe(dialogResponse => {
        this.refreshGroupeEtudiant(dialogResponse);
      });
    }else{
        this.messageService.setError('Veuillez selectionner un unique étudiant.');
    }
  }

  refreshGroupeEtudiant(dialogResponse: any): void {
    this.groupeEtudiantService.getById(this.groupeEtudiant.id).subscribe((response: any) => {
      this.validated.emit(response);
    });
  }
}
