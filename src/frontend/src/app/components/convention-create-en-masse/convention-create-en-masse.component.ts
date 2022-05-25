import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../table/table.component";
import { ConventionService } from "../../services/convention.service";
import { AuthService } from "../../services/auth.service";
import { Router } from "@angular/router";
import { StructureService } from "../../services/structure.service";
import { UfrService } from "../../services/ufr.service";
import { EtapeService } from "../../services/etape.service";
import { EtudiantService } from "../../services/etudiant.service";
import { MessageService } from "../../services/message.service";
import { ConfigService } from "../../services/config.service";
import { SortDirection } from "@angular/material/sort";

@Component({
  selector: 'app-convention-create-en-masse',
  templateUrl: './convention-create-en-masse.component.html',
  styleUrls: ['./convention-create-en-masse.component.scss']
})
export class ConventionCreateEnMasseComponent implements OnInit {

  columns: string[] = [];
  sortColumn = 'prenom';
  sortDirection: SortDirection = 'desc';
  filters: any[] = [];
  selected: any[] = [];

  anneeEnCours: any|undefined;
  annees: any[] = [];

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public conventionService: ConventionService,
    public etudiantService: EtudiantService,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
    private configService: ConfigService,
  ) {
  }

  ngOnInit(): void {

    this.columns = ['select','numEtudiant','nom', 'prenom', 'mail'];
    this.filters = [
      { id: 'nom', libelle: 'Nom'},
      { id: 'prenom', libelle: 'Prénom'},
      { id: 'numEtudiant', libelle: 'N° étudiant'},
    ];
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
    this.appTable?.data.forEach((data: any) => {
      const index = this.selected.findIndex((r: any) => {return r.id === data.id});
      if (index === -1) {
         allSelected = false;
      }
    });
    return allSelected;
  }

}
