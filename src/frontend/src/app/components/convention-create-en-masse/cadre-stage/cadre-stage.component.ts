import { Component, EventEmitter, Input, OnChanges, OnInit, Output, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { EtudiantGroupeEtudiantService } from "../../../services/etudiant-groupe-etudiant.service";
import { TypeConventionService } from "../../../services/type-convention.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import { MessageService } from "../../../services/message.service";
import { SortDirection } from "@angular/material/sort";
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { CadreStageModalComponent } from './cadre-stage-modal/cadre-stage-modal.component';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";

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
  typeConventions: any[] = [];

  form: FormGroup;

  @Input() sharedData: any;
  @Input() groupeEtudiant: any;
  @Output() validated = new EventEmitter<any>();

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public groupeEtudiantService: GroupeEtudiantService,
    public etudiantGroupeEtudiantService: EtudiantGroupeEtudiantService,
    private typeConventionService: TypeConventionService,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
    private fb: FormBuilder,
    public matDialog: MatDialog,
  ) {
    this.form = this.fb.group({
      typeConventionGroupe: [null],
    });
  }

  ngOnInit(): void {
    this.columns = ['action','numEtudiant','nom', 'mail', 'mailPerso', 'ufr.libelle', 'etape.libelle', 'annee', 'adresse', 'codePostal', 'commune', 'pays', 'tel', 'telPortable'];
    this.filters = [...this.sharedData.filters];

    this.typeConventionService.getListActiveWithTemplate().subscribe((response: any) => {
      this.typeConventions = response.data;
    });
  }

  ngOnChanges(): void{
    this.appTable?.update();
    if(this.groupeEtudiant){
      this.form.setValue({
        typeConventionGroupe: this.groupeEtudiant.convention.typeConvention.id,
      });
    }
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

  validate(): void {
    if (this.form.get('typeConventionGroupe')?.value) {
      this.groupeEtudiantService.setTypeConventionGroupe(this.groupeEtudiant.id, this.form.get('typeConventionGroupe')?.value).subscribe((response: any) => {
        this.messageService.setSuccess('Groupe modifié avec succès');
      });
    }
  }

}
