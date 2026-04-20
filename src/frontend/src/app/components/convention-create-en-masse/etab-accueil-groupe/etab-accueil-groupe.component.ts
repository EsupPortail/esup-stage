import { Component, EventEmitter, Input, OnChanges, OnInit, Output, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { ConventionService } from "../../../services/convention.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import { EtudiantGroupeEtudiantService } from "../../../services/etudiant-groupe-etudiant.service";
import { ServiceService } from "../../../services/service.service";
import { MessageService } from "../../../services/message.service";
import { SortDirection } from "@angular/material/sort";
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { EtabAccueilGroupeModalComponent } from './etab-accueil-groupe-modal/etab-accueil-groupe-modal.component';
import {
  defaultIfEmpty, defer,
  filter,
  from,
  map,
  mergeMap,
  Observable,
  shareReplay,
  tap, toArray,
} from "rxjs";

@Component({
  selector: 'app-etab-accueil-groupe',
  templateUrl: './etab-accueil-groupe.component.html',
  styleUrls: ['./etab-accueil-groupe.component.scss']
})
export class EtabAccueilGroupeComponent implements OnInit, OnChanges {

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
    private serviceService: ServiceService,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
    public matDialog: MatDialog,
  ) {
  }

  ngOnInit(): void {
    this.columns = [...this.sharedData.columns];
    this.columns.push('etab');
    this.filters = [...this.sharedData.filters];
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
    dialogConfig.width = '95vw';
    dialogConfig.maxWidth = '1100px';
    dialogConfig.maxHeight = '90vh';
    dialogConfig.panelClass = 'custom-dialog-container';
    dialogConfig.data = {etab: this.groupeEtudiant.convention.structure};
    const modalDialog = this.matDialog.open(EtabAccueilGroupeModalComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        this.updateEtab(this.groupeEtudiant.convention.id,dialogResponse)
          .pipe(
            mergeMap(convention => this.getServiceIfSingle(dialogResponse, this.groupeEtudiant.convention.centreGestion.id).pipe(
              mergeMap(service => this.updateService(convention,service)),
              defaultIfEmpty(convention),
            ))
          ).subscribe(convention => this.emitGroupe())
      }
    });
  }

  selectForSelected(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '95vw';
    dialogConfig.maxWidth = '1100px';
    dialogConfig.maxHeight = '90vh';
    dialogConfig.panelClass = 'custom-dialog-container';
    dialogConfig.data = {etab: null};
    const modalDialog = this.matDialog.open(EtabAccueilGroupeModalComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        const service$ = defer(
            () => this.getServiceIfSingle(dialogResponse, this.groupeEtudiant.convention.centreGestion.id)
          ).pipe(shareReplay(1));
        from(this.selected).pipe(
          mergeMap((etu) =>
            this.updateEtab(etu.convention.id, dialogResponse)
          ),
          mergeMap(convention => service$.pipe(
            mergeMap(service => this.updateService(convention,service)),
            defaultIfEmpty(convention),
          )),
          toArray(),
        ).subscribe((conventions:any)=> this.emitGroupe());
      }
    });
  }

  importCsv(event: any): void {
    this.groupeEtudiantService.import(event.target.files[0], this.groupeEtudiant.id).subscribe((response: any) => {
      this.messageService.setSuccess('Structures d\'accueil importées avec succès');
    });
  }

  updateEtab(conventionId: number, etabId: number): Observable<any> {
    const data = {
      "field":'idStructure',
      "value":etabId,
    };
    return this.conventionService.patch(conventionId, data).pipe(tap((response: any) => {
        this.messageService.setSuccess('Structure d\'accueil affectée avec succès');
    }));
  }

  emitGroupe() {
        this.groupeEtudiantService.getById(this.groupeEtudiant.id).subscribe((response: any) => {
          this.validated.emit(response);
        });
  }

  getServiceIfSingle(etabId: number, centreGestionId: number): Observable<any> {
    return this.serviceService.getByStructure(etabId, centreGestionId).pipe(
      filter((response: any) => response.length === 1),
      map((response: any) => response[0])
    );
  }

  updateService(convention: any, service: any): Observable<any> {
    return this.conventionService.patch(convention.id, {
      "field":'idService',
      "value":service.id,
    }).pipe(tap(convention =>
        this.messageService.setSuccess('Service d\'accueil par défaut affectée avec succès')
    ));
  }
}
