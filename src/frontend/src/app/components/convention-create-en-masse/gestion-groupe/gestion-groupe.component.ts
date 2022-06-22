import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { EtudiantGroupeEtudiantService } from "../../../services/etudiant-groupe-etudiant.service";
import { MessageService } from "../../../services/message.service";
import { SortDirection } from "@angular/material/sort";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";

@Component({
  selector: 'app-gestion-groupe',
  templateUrl: './gestion-groupe.component.html',
  styleUrls: ['./gestion-groupe.component.scss']
})
export class GestionGroupeComponent implements OnInit {

  groupeColumns: string[] = [];
  groupeSortColumn = 'nom';
  groupeSortDirection: SortDirection = 'desc';
  groupeFilters: any[] = [];

  mailColumns: string[] = [];
  mailSortColumn = 'nom';
  mailSortDirection: SortDirection = 'desc';
  mailFilters: any[] = [];

  exportColumns: string[] = [];
  exportSortColumn = 'nom';
  exportSortDirection: SortDirection = 'desc';
  exportFilters: any[] = [];

  selected: any[] = [];

  data: any = {};

  mailTabIndex = 1;
  printTabIndex = 2;

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(
    public groupeEtudiantService: GroupeEtudiantService,
    public etudiantGroupeEtudiantService: EtudiantGroupeEtudiantService,
    private messageService: MessageService,
  ) {
  }

  ngOnInit(): void {
    this.groupeColumns = ['code','nom','loginCreation','dateCreation','periodStage','actions'];
    this.groupeFilters = [
      { id: 'nom', libelle: 'Nom'},
    ];
    this.groupeFilters.push({ id: 'validationCreation', type: 'boolean', value: true, hidden: true, permanent: true });

    this.mailColumns = ['select','numEtudiant','nom', 'prenom', 'mail'];
    this.mailFilters = [
        { id: 'etudiant.nom', libelle: 'Nom'},
        { id: 'etudiant.prenom', libelle: 'Prénom'},
        { id: 'etudiant.numEtudiant', libelle: 'N° étudiant'},
    ];

    this.exportColumns = ['select','numEtudiant','nom', 'prenom', 'mail'];
    this.exportFilters = [
        { id: 'etudiant.nom', libelle: 'Nom'},
        { id: 'etudiant.prenom', libelle: 'Prénom'},
        { id: 'etudiant.numEtudiant', libelle: 'N° étudiant'},
    ];
  }

  duplicate(row: any): void{
    this.groupeEtudiantService.duplicate(row.id).subscribe((response: any) => {
      this.messageService.setSuccess('Groupe dupliqué avec succès');
    });
  }

  tabChanged(event: MatTabChangeEvent): void {
    this.selected = [];
    if (event.index == 0) {
      this.data = {};
    }
  }

  print(row: any): void{
    this.data = row;
    if (this.tabs) {
      this.tabs.selectedIndex = this.printTabIndex;
    }
  }

  sendMail(row: any): void{
    this.data = row;
    if (this.tabs) {
      this.tabs.selectedIndex = this.mailTabIndex;
    }
  }

  sendMailForGroup(): void{
  }

  sendMailForSelected(): void{
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
}
