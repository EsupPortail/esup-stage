import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { UfrService } from "../../../services/ufr.service";
import { EtapeService } from "../../../services/etape.service";
import { forkJoin } from 'rxjs';
import { ConventionService } from "../../../services/convention.service";
import { EtudiantGroupeEtudiantService } from "../../../services/etudiant-groupe-etudiant.service";
import { TemplateMailGroupeService } from "../../../services/template-mail-groupe.service";
import { MessageService } from "../../../services/message.service";
import { SortDirection } from "@angular/material/sort";
import { Router } from "@angular/router";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatLegacyTabChangeEvent as MatTabChangeEvent, MatLegacyTabGroup as MatTabGroup } from "@angular/material/legacy-tabs";
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
  ufrList: any[] = [];
  etapeList: any[] = [];
  annees: any[] = [];

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
              private ufrService: UfrService,
              private etapeService: EtapeService,
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
    this.groupeColumns = ['code','nom','annee','loginCreation','dateCreation','periodStage','actions'];
    this.groupeFilters = [
      { id: 'code', libelle: 'Code'},
      { id: 'nom', libelle: 'Nom'},
      { id: 'convention.annee', libelle: 'Année', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'libelle', value: [] },
      { id: 'loginCreation', libelle: 'loginCreation'},
    ];
    this.groupeFilters.push({ id: 'validationCreation', type: 'boolean', value: true, hidden: true, permanent: true });

    this.mailColumns = ['select','numEtudiant','nom', 'prenom', 'mail', 'ufr.libelle', 'etape.libelle', 'annee', 'etab', 'service', 'contact', 'mailTuteur'];
    this.mailFilters = [
        { id: 'ufr.id', libelle: 'Composante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
        { id: 'etape.id', libelle: 'Étape', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
        { id: 'convention.annee', libelle: 'Année', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'libelle', value: [] },
        { id: 'etudiant.nom', libelle: 'Nom'},
        { id: 'etudiant.prenom', libelle: 'Prénom'},
        { id: 'etudiant.numEtudiant', libelle: 'N° étudiant'},
    ];
    this.mailFilters.push({ id: 'groupeEtudiant.id', type: 'int', value: 0, hidden: true, permanent: true });

    this.exportColumns = ['select','numEtudiant','nom', 'prenom', 'mail', 'ufr.libelle', 'etape.libelle', 'annee'];
    this.exportFilters = [
        { id: 'ufr.id', libelle: 'Composante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
        { id: 'etape.id', libelle: 'Étape', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
        { id: 'convention.annee', libelle: 'Année', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'libelle', value: [] },
        { id: 'etudiant.nom', libelle: 'Nom'},
        { id: 'etudiant.prenom', libelle: 'Prénom'},
        { id: 'etudiant.numEtudiant', libelle: 'N° étudiant'},
    ];
    this.exportFilters.push({ id: 'groupeEtudiant.id', type: 'int', value: 0, hidden: true, permanent: true });

    this.templateMailGroupeService.getPaginated(1, 0, 'lib', 'asc', "").subscribe((response: any) => {
      this.templates = response.data;
    });

    forkJoin(
      this.ufrService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
      this.etapeService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
      this.conventionService.getListAnnee(),
    ).subscribe(([ufrData, etapeData, listAnneeData]) => {
      this.ufrList = ufrData.data;
      this.etapeList = etapeData.data;
      this.annees = listAnneeData;
      this.tableList?.setFilterOption('convention.annee', this.annees);
    });

  }

  refreshHistorique(): void{
    this.groupeEtudiantService.getHistoriqueGroupeMail(this.groupeEtudiant.id).subscribe((response: any) => {
      this.historiques = response;
    });
  }

  isConventionGenerated(row: any): boolean{
    if (row.etudiantGroupeEtudiants[0].mergedConvention) {
      return true
    }
    return false
  }

  refreshFilters(): void{
   this.tableMail?.setFilterValue('groupeEtudiant.id', this.groupeEtudiant.id);
   this.tableExport?.setFilterValue('groupeEtudiant.id', this.groupeEtudiant.id);
   this.tableMail?.setFilterOption('ufr.id', this.ufrList);
   this.tableMail?.setFilterOption('etape.id', this.etapeList);
   this.tableMail?.setFilterOption('convention.annee', this.annees);
   this.tableExport?.setFilterOption('ufr.id', this.ufrList);
   this.tableExport?.setFilterOption('etape.id', this.etapeList);
   this.tableExport?.setFilterOption('convention.annee', this.annees);
  }

  duplicate(row: any): void{
    this.groupeEtudiantService.duplicate(row.id).subscribe((response: any) => {
      this.messageService.setSuccess('Groupe dupliqué avec succès');
      this.router.navigate([`/convention-create-en-masse/create`])
    });
  }

  edit(row: any): void{
    this.router.navigate([`/convention-create-en-masse/` + row.id])
  }

  tabChanged(event: MatTabChangeEvent): void {
    this.selected = [];
    if (event.index == 0) {
      this.groupeEtudiant = {};
    }else{
      this.refreshFilters();
      this.refreshHistorique();
    }
  }

  save(): void {
  }

  printTab(row: any): void{
    this.groupeEtudiant = row;
    if (this.tabs) {
      this.tabs.selectedIndex = this.printTabIndex;
    }
  }

  sendMailTab(row: any): void{
    this.groupeEtudiant = row;
    if (this.tabs) {
      this.tabs.selectedIndex = this.mailTabIndex;
    }
  }

  sendMailForGroup(): void{
    const data = {ids: this.groupeEtudiant.etudiantGroupeEtudiants.map((ege: any) => ege.id)};
    this.sendMail(data);
  }

  sendMailForSelected(): void{
    const data = {ids: this.selected.map((ege: any) => ege.id)};
    this.sendMail(data);
  }

  sendMail(data: any){
    if (this.form.valid) {
      this.groupeEtudiantService.sendMail(this.groupeEtudiant.id,this.form.get('template')?.value, data).subscribe((response: any) => {
        this.messageService.setSuccess('Mails envoyés avec succès');
        this.refreshHistorique();
      });
    }
  }

  printForGroup(): void {
    const data = {ids: this.groupeEtudiant.etudiantGroupeEtudiants.map((ege: any) => ege.mergedConvention.id)};
    this.print(data);
  }

  printForSelected(): void{
    const data = {ids: this.selected.map((ege: any) => ege.mergedConvention.id)};
    this.print(data);
  }

  print(data: any): void{
    this.groupeEtudiantService.getConventionPDF(data).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: "application/zip"});
      let filename = 'conventions.zip';
      FileSaver.saveAs(blob, filename);
    });
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

  delete(id: number) {
    this.groupeEtudiantService.delete(id).subscribe((response: any) => {
      this.messageService.setSuccess("groupe supprimé");
      this.tableList?.update();
    });
  }
}
