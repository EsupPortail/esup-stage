import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { ConventionService } from "../../../services/convention.service";
import { EtudiantGroupeEtudiantService } from "../../../services/etudiant-groupe-etudiant.service";
import { TemplateMailGroupeService } from "../../../services/template-mail-groupe.service";
import { MessageService } from "../../../services/message.service";
import { SortDirection } from "@angular/material/sort";
import { Router } from "@angular/router";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import * as FileSaver from 'file-saver';

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

  groupeEtudiant: any = {};

  templates: any[] = [];

  historiques: any[] = [];
  columnsHisto = ['modifiePar', 'date', 'destinataire'];

  form: FormGroup;

  mailTabIndex = 1;
  printTabIndex = 2;

  @ViewChild('tableList') tableList: TableComponent | undefined;
  @ViewChild('tableMail') tableMail: TableComponent | undefined;
  @ViewChild('tableExport') tableExport: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(
    public groupeEtudiantService: GroupeEtudiantService,
    public etudiantGroupeEtudiantService: EtudiantGroupeEtudiantService,
    public conventionService: ConventionService,
    public templateMailGroupeService: TemplateMailGroupeService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private router: Router
  ) {
    this.form = this.fb.group({
      template: [null, [Validators.required]],
    });

  }

  ngOnInit(): void {

    this.groupeColumns = ['code','nom','loginCreation','dateCreation','periodStage','actions'];
    this.groupeFilters = [
      { id: 'nom', libelle: 'Nom'},
    ];
    this.groupeFilters.push({ id: 'validationCreation', type: 'boolean', value: true, hidden: true, permanent: true });

    this.mailColumns = ['select','numEtudiant','nom', 'prenom', 'mail', 'etab', 'service', 'contact', 'mailTuteur'];
    this.mailFilters = [
        { id: 'etudiant.nom', libelle: 'Nom'},
        { id: 'etudiant.prenom', libelle: 'Prénom'},
        { id: 'etudiant.numEtudiant', libelle: 'N° étudiant'},
    ];
    this.mailFilters.push({ id: 'groupeEtudiant.id', type: 'int', value: 0, hidden: true, permanent: true });

    this.exportColumns = ['select','numEtudiant','nom', 'prenom', 'mail'];
    this.exportFilters = [
        { id: 'etudiant.nom', libelle: 'Nom'},
        { id: 'etudiant.prenom', libelle: 'Prénom'},
        { id: 'etudiant.numEtudiant', libelle: 'N° étudiant'},
    ];
    this.exportFilters.push({ id: 'groupeEtudiant.id', type: 'int', value: 0, hidden: true, permanent: true });

    this.templateMailGroupeService.getPaginated(1, 0, 'lib', 'asc', "").subscribe((response: any) => {
      this.templates = response.data;
    });
  }

  refreshHistorique(): void{
    this.groupeEtudiantService.getHistoriqueGroupeMail(this.groupeEtudiant.id).subscribe((response: any) => {
      this.historiques = response;
    });
  }

  refreshFilters(): void{
   this.tableMail?.setFilterValue('groupeEtudiant.id', this.groupeEtudiant.id);
   this.tableExport?.setFilterValue('groupeEtudiant.id', this.groupeEtudiant.id);
  }

  duplicate(row: any): void{
    this.groupeEtudiantService.duplicate(row.id).subscribe((response: any) => {
      this.messageService.setSuccess('Groupe dupliqué avec succès');
      this.router.navigate([`/convention-create-en-masse/create`], )
    });
  }

  tabChanged(event: MatTabChangeEvent): void {
    this.selected = [];
    if (event.index == 0) {
      this.groupeEtudiant = {};
    }
    this.refreshFilters();
    this.refreshHistorique();
  }


  save(): void {
  }

  print(row: any): void{
    this.groupeEtudiant = row;
    if (this.tabs) {
      this.tabs.selectedIndex = this.printTabIndex;
    }
  }

  sendMail(row: any): void{
    this.groupeEtudiant = row;
    if (this.tabs) {
      this.tabs.selectedIndex = this.mailTabIndex;
    }
  }

  sendMailForGroup(): void{
    if (this.form.valid) {
      const size = this.tableMail?.data.length;
      let i = 0;
      for (const etu of this.tableMail?.data){
        const data = {
          templateMail: this.form.get('template')?.value,
          conventionId: etu.convention.id,
          to: etu.convention.contact.mail,
        }
        this.groupeEtudiantService.sendMail(data,this.groupeEtudiant.id).subscribe((response: any) => {
          i++;
          if(i === size){
            this.messageService.setSuccess('Mails envoyés avec succès');
            this.refreshHistorique();
          }
        });
      }
    }
  }

  sendMailForSelected(): void{
    if (this.form.valid) {
      const size = this.selected.length;
      let i = 0;
      for (const etu of this.selected){
        const data = {
          templateMail: this.form.get('template')?.value,
          conventionId: etu.convention.id,
          to: etu.convention.contact.mail,
        }
        this.groupeEtudiantService.sendMail(data,this.groupeEtudiant.id).subscribe((response: any) => {
          i++;
          if(i === size){
            this.messageService.setSuccess('Mails envoyés avec succès');
            this.refreshHistorique();
          }
        });
      }
    }
  }

  printForGroup(): void{
  const convention = this.groupeEtudiant.convention;
    this.conventionService.getConventionPDF(convention.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: "application/pdf"});
      let filename = 'convention_' + convention.id + '_' + convention.prenom + '_' + convention.nom + '.pdf';
      FileSaver.saveAs(blob, filename);
    });
  }

  printForSelected(): void{
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

    if(this.tabs){
      let data: any;
      if(this.tabs.selectedIndex === this.mailTabIndex){
        data = this.tableMail?.data;
      }else{
        data = this.tableExport?.data;
      }

      data.forEach((d: any) => {
        const index = this.selected.findIndex((s: any) => s.id === d.id);
        if (index === -1) {
          this.selected.push(d);
        }
      });
    }
  }

  isAllSelected(): boolean {
    let allSelected = true;

    if(this.tabs){
      let data: any;
      if(this.tabs.selectedIndex === this.mailTabIndex){
        data = this.tableMail?.data;
      }else{
        data = this.tableExport?.data;
      }

      if(data){
        data.forEach((data: any) => {
          const index = this.selected.findIndex((r: any) => {return r.id === data.id});
          if (index === -1) {
             allSelected = false;
          }
        });
      }
    }
    return allSelected;
  }
}
