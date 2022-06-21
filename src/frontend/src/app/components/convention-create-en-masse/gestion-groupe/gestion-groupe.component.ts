import { Component, EventEmitter, Input, Output, OnChanges, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import { EtudiantService } from "../../../services/etudiant.service";
import { MessageService } from "../../../services/message.service";
import { ConfigService } from "../../../services/config.service";
import { SortDirection } from "@angular/material/sort";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";

@Component({
  selector: 'app-gestion-groupe',
  templateUrl: './gestion-groupe.component.html',
  styleUrls: ['./gestion-groupe.component.scss']
})
export class GestionGroupeComponent implements OnInit {

  columns: string[] = [];
  sortColumn = 'prenom';
  sortDirection: SortDirection = 'desc';
  filters: any[] = [];
  selected: any[] = [];

  anneeEnCours: any|undefined;
  annees: any[] = [];

  form: FormGroup;

  @Input() groupeEtudiant: any;
  @Output() validated = new EventEmitter<any>();

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public groupeEtudiantService: GroupeEtudiantService,
    public etudiantService: EtudiantService,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
    private configService: ConfigService,
    private fb: FormBuilder,
  ) {
    this.form = this.fb.group({
      nomGroupe: [null, [Validators.required, Validators.maxLength(100)]],
    });
  }

  ngOnInit(): void {
    this.columns = ['nom'];
    this.filters = [
      { id: 'nom', libelle: 'Nom'},
      { id: 'prenom', libelle: 'Prénom'},
      { id: 'numEtudiant', libelle: 'N° étudiant'},
    ];
  }

  ngOnChanges(): void{
    this.form.setValue({
      nomGroupe: this.groupeEtudiant?this.groupeEtudiant.nom:null,
    });
    if(this.groupeEtudiant){
      this.selected = this.groupeEtudiant.etudiantGroupeEtudiants.map((ege: any) => ege.etudiant);
    }
  }

  validate(): void {
      if (this.form.valid) {
        const selected = this.selected.map((s: any) => s.id);

        let data = {...this.form.value};
        data.etudiantIds = selected;

        if (!this.groupeEtudiant) {
          this.groupeEtudiantService.create(data).subscribe((response: any) => {
            this.messageService.setSuccess('Groupe créé avec succès');
            this.validated.emit(response);
          });
        } else {
          this.groupeEtudiantService.update(this.groupeEtudiant.id, data).subscribe((response: any) => {
            this.messageService.setSuccess('Groupe modifié avec succès');
            this.validated.emit(response);
          });
        }
      }
  }
}
