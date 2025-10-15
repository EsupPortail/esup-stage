import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../table/table.component";
import { ConventionService } from "../../services/convention.service";
import { AuthService } from "../../services/auth.service";
import { Router } from "@angular/router";
import { SortDirection } from "@angular/material/sort";
import { TitleService } from "../../services/title.service";
import { CentreGestionService } from "../../services/centre-gestion.service";
import { UfrService } from "../../services/ufr.service";
import {EnvoiMailEnMasseEvalComponent} from "./envoi-mail-en-masse-eval/envoi-mail-en-masse-eval.component";
import {MatDialog} from "@angular/material/dialog";
import {ExportEvaluationComponent} from "./export-evaluation/export-evaluation.component";
import {EvaluationService} from "../../services/evaluation.service";
import { QuestionsEvaluationService } from "../../services/questions-evaluation.service";
import { DbQuestion } from "../../models/question-evaluation.model";
import {forkJoin} from "rxjs";


@Component({
  selector: 'app-eval-stage',
  templateUrl: './eval-stage.component.html',
  styleUrls: ['./eval-stage.component.scss']
})
export class EvalStageComponent implements OnInit, OnDestroy {

  columns: string[] = [];
  sortColumn = 'id';
  sortDirection: SortDirection = 'desc';
  filters: any[] = [];
  savedFilters: any[] = [];
  selected: any[] = [];
  exportColumns: any[] = [];

  anneeEnCours: any|undefined;
  annees: any[] = [];

  isEtudiant:boolean = false;
  isEnseignant:boolean = false;
  isGestionnaireOrAdmin:boolean = false;

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public conventionService: ConventionService,
    private authService: AuthService,
    private router: Router,
    private titleService: TitleService,
    private centreGestionService: CentreGestionService,
    private ufrService: UfrService,
    private dialog: MatDialog,
    private questionsEvaluationService: QuestionsEvaluationService) {
  }

  ngOnInit(): void {
    this.isEtudiant = this.authService.isEtudiant();
    this.isEnseignant = this.authService.isEnseignant();
    this.isGestionnaireOrAdmin = this.authService.isGestionnaire() || this.authService.isAdmin() ;
    const login = this.authService.getUserConnectedLogin();

    if (this.isGestionnaireOrAdmin) this.titleService.title = 'Rechercher une évaluation';
    else this.titleService.title = `Visualiser mes fiches d'évaluation`;

    let filtersString: any = sessionStorage.getItem('evalstages-filters');
    this.savedFilters = JSON.parse(filtersString);

    this.filters = [{id: 'isConventionValide', value: 'true', hidden: true, permanent: true, specific: true}];

    if(this.isGestionnaireOrAdmin){
      this.columns = ['select', 'id', 'etudiant.nom_etudiant.prenom', 'structure.raisonSociale', 'dateDebutStage', 'dateFinStage', 'ufr.libelle',
     'etape.libelle', 'annee','reponseEvaluationEtudiant','reponseEvaluationEnseignant','reponseEvaluationEntreprise', 'action'];

      this.filters.push(
        { id: 'annee', libelle: 'Année', type: 'annee', options: [], keyLibelle: 'libelle', keyId: 'libelle', value: [] },
        { id: 'id', libelle: 'N° de la convention', type: 'int' },
        { id: 'etudiant', libelle: 'Étudiant', specific: true },
        { id: 'ufr.id', libelle: 'Composante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
        { id: 'centreGestion.nomCentre', libelle: 'Centres de gestion', type: 'list', options: [], keyId: 'nomCentre', keyLibelle: 'nomCentre', colSpan: 6, infoBulleCentre: true },
        { id: 'stageTermine', libelle: 'N\'afficher que les stages terminés ?', type: 'boolean', specific: true }
      );

      this.conventionService.getListAnnee().subscribe(response => {
        this.annees = response;
        this.anneeEnCours = this.annees.find((a: any) => { return a.anneeEnCours === true });
        this.appTable?.setFilterOption('annee', this.annees);
        this.appTable?.setFilterValue('annee', this.anneeEnCours.libelle);

        if (this.savedFilters) {
          this.restoreFilters();
        }
        this.centreGestionService.getPaginated(1, 0, 'nomCentre', '', '').subscribe((response: any) => {
          this.appTable?.setFilterOption('centreGestion.nomCentre', response.data);
        });
        this.appTable?.update();
      });
    } else {
      if (this.isEtudiant) {
        this.columns = ['id', 'structure.raisonSociale', 'dateDebutStage', 'dateFinStage', 'ufr.libelle',
          'etape.libelle', 'annee', 'reponseEvaluationEtudiant', 'action'];

        this.filters.push({id: 'etudiant.identEtudiant', type: 'string', value: login, hidden: true, permanent: true});
      } else if (this.isEnseignant) {
        this.columns = ['id', 'etudiant.nom_etudiant.prenom', 'structure.raisonSociale', 'dateDebutStage', 'dateFinStage', 'ufr.libelle',
          'etape.libelle', 'annee', 'reponseEvaluationEnseignant', 'action'];

        this.filters.push({id: 'enseignant.uidEnseignant', type: 'string', value: login, hidden: true, permanent: true});
      }
      if (this.savedFilters) {
        this.restoreFilters();
      }
    }
    this.ufrService.getPaginated(1, 0, 'libelle', '', '').subscribe((response: any) => {
      this.appTable?.setFilterOption('ufr.id', response.data);
    });
    forkJoin({
      etu: this.questionsEvaluationService.getQuestionsEtu(),
      ens: this.questionsEvaluationService.getQuestionsEns(),
      ent: this.questionsEvaluationService.getQuestionsEnt(),
    }).subscribe(({ etu, ens, ent }) => {
      const qMap = this.buildQuestionMap([...etu, ...ens, ...ent]);
      this.setExportColumns(qMap);
    });
    window.addEventListener('beforeunload', this.saveSessionData.bind(this));
  }

  goToConvention(id: number): void {
    this.router.navigate([`/conventions/${id}`], {queryParams: {back: 'eval-stages', goToOnglet: '8'} });
  }

  restoreFilters() {
    Object.entries(this.savedFilters).forEach(([key, filterData]: [string, any]) => {
      if (filterData.value) {
        let value = filterData.value;

        if (filterData.type === 'list') {
          value = Array.isArray(value) ?
            value.map((v: string) => v.trim()) :
            value;
        }

        this.appTable?.setFilterValue(key, value);
      }
    });

    const pagingString: string|null = sessionStorage.getItem('evalstages-paging');
    if (pagingString) {
      const pagingConfig = JSON.parse(pagingString);
      this.sortColumn = pagingConfig.sortColumn;
      this.sortDirection = pagingConfig.sortOrder;
      this.appTable?.setBackConfig(pagingConfig);
    }
  }

  saveSessionData(): void {
    const pagingData = {
      page: this.appTable?.page,
      pageSize: this.appTable?.pageSize,
      sortColumn: this.appTable?.sortColumn,
      sortOrder: this.appTable?.sortOrder
    };
    const filterValues = this.appTable?.getFilters();

    sessionStorage.setItem('evalstages-paging', JSON.stringify(pagingData));
    sessionStorage.setItem('evalstages-filters', JSON.stringify(filterValues));
  }

  ngOnDestroy(): void {
    this.saveSessionData();
    window.removeEventListener('beforeunload', this.saveSessionData.bind(this));
  }

  openEnvoiMailModal() {
    this.dialog.open(EnvoiMailEnMasseEvalComponent, {
      width: '800px',
      data: {rows: this.selected}
    });
  }

  openExportExcelModal() {
    const normalized = this.normalizeExportColumns(this.exportColumns);
    this.dialog.open(ExportEvaluationComponent, {
      width: '800px',
      data: { sheets:normalized.sheets,rows:this.appTable?.data }
    });
  }

  isSelected(row: any): boolean {
    return this.selected.some((r: any) => r.id === row.id);
  }
  toggleSelected(row: any): void {
    const i = this.selected.findIndex((r: any) => r.id === row.id);
    if (i > -1) this.selected.splice(i, 1);
    else this.selected.push(row);
  }
  masterToggle(): void {
    if (this.isAllSelected()) { this.selected = []; return; }
    this.appTable?.data.forEach((r: any) => {
      if (!this.selected.some((s: any) => s.id === r.id)) this.selected.push(r);
    });
  }
  isAllSelected(): boolean {
    let all = true;
    this.appTable?.data.forEach((r: any) => {
      if (!this.selected.some((s: any) => s.id === r.id)) all = false;
    });
    return all;
  }

  private normalizeExportColumns(raw: any): { sheets: any[] } {
    return {
      sheets: raw.map((sheet: { title?: string; columns: Record<string, { title: string }> }) => ({
        title: sheet.title || 'Feuille',
        availableColumns: Object.entries(sheet.columns).map(([key, col]: [string, any]) => ({
          key,
          title: col.title
        }))
      }))
    };
  }

  private setExportColumns(qMap: Record<string, any>) {
    // Étudiant : sous-questions
    const etuI4Label = this.qText(qMap, 'ETUI4', 'I.4');
    const etuI4Items = this.qItems(qMap, 'ETUI4');
    const etuIII5Label = this.qText(qMap, 'ETUIII5', 'III.5');
    const etuIII5Items = this.qItems(qMap, 'ETUIII5');

    const etudiant: Record<string, { title: string }> = {
      // Communes
      ETU_ID_CONVENTION: { title: 'N° convention' },
      ETU_NOM_ETUDIANT: { title: 'Nom Étudiant' },
      ETU_PRENOM_ETUDIANT: { title: 'Prénom Étudiant' },
      ETU_STRUCTURE: { title: 'Structure' },
      ETU_DATE_DEBUT_STAGE: { title: 'Date Début Stage' },
      ETU_DATE_FIN_STAGE: { title: 'Date Fin Stage' },
      ETU_CENTRE_GESTION: { title: 'Centre de Gestion' },
      ETU_ETAPE: { title: 'Étape' },
      ETU_ANNEE_UNIVERSITAIRE: { title: 'Année Universitaire' },

      // I
      ETU_ETUI1:      { title: this.qText(qMap, 'ETUI1', 'I.1') },
      ETU_ETUI1bis:   { title: this.qText(qMap, 'ETUI1', 'I.1') + " - " + this.qBis(qMap,  'ETUI1', 'Commentaire') },
      ETU_ETUI2:      { title: this.qText(qMap, 'ETUI2', 'I.2') },
      ETU_ETUI3:      { title: this.qText(qMap, 'ETUI3', 'I.3') },

      ETU_ETUI4a:     { title: `${etuI4Label} - ${etuI4Items[0] ?? this.letterAt(0)}` },
      ETU_ETUI4b:     { title: `${etuI4Label} - ${etuI4Items[1] ?? this.letterAt(1)}` },
      ETU_ETUI4c:     { title: `${etuI4Label} - ${etuI4Items[2] ?? this.letterAt(2)}` },
      ETU_ETUI4d:     { title: `${etuI4Label} - ${etuI4Items[3] ?? this.letterAt(3)}` },

      ETU_ETUI5:      { title: this.qText(qMap, 'ETUI5', 'I.5') },
      ETU_ETUI6:      { title: this.qText(qMap, 'ETUI6', 'I.6') },
      ETU_ETUI7:      { title: this.qText(qMap, 'ETUI7', 'I.7') },
      ETU_ETUI7_bis1: { title: `${this.qText(qMap, 'ETUI7', 'I.7')} - Si oui, par qui ?` },
      ETU_ETUI7_bis2: { title: `${this.qText(qMap, 'ETUI7', 'I.7')} - Si non, pourquoi ?` },
      ETU_ETUI8:      { title: this.qText(qMap, 'ETUI8', 'I.8') },

      // II
      ETU_ETUII1:     { title: this.qText(qMap, 'ETUII1', 'II.1') },
      ETU_ETUII1bis:  { title: this.qText(qMap, 'ETUII1', 'II.1') + " - " + this.qBis(qMap,  'ETUII1', 'Commentaire') },
      ETU_ETUII2:     { title: this.qText(qMap, 'ETUII2', 'II.2') },
      ETU_ETUII2bis:  { title: this.qText(qMap, 'ETUII1', 'II.2') + " - " + this.qBis(qMap,  'ETUII2', 'Commentaire') },
      ETU_ETUII3:     { title: this.qText(qMap, 'ETUII3', 'II.3') },
      ETU_ETUII3bis:  { title: this.qText(qMap, 'ETUII3', 'II.3') + " - " + this.qBis(qMap,  'ETUII3', 'Commentaire') },
      ETU_ETUII4:     { title: this.qText(qMap, 'ETUII4', 'II.4') },
      ETU_ETUII5:     { title: this.qText(qMap, 'ETUII5', 'II.5') },
      ETU_ETUII5a:    { title: `${this.qText(qMap, 'ETUII5', 'II.5')} - De quel ordre ?` },
      ETU_ETUII5b:    { title: `${this.qText(qMap, 'ETUII5', 'II.5')} - Avec autonomie ?` },
      ETU_ETUII6:     { title: this.qText(qMap, 'ETUII6', 'II.6') },

      // III
      ETU_ETUIII1:    { title: this.qText(qMap, 'ETUIII1', 'III.1') },
      ETU_ETUIII1bis: { title: this.qText(qMap, 'ETUIII1', 'III.1') + " - " + this.qBis(qMap,  'ETUIII1', 'Commentaire') },
      ETU_ETUIII2:    { title: this.qText(qMap, 'ETUIII2', 'III.2') },
      ETU_ETUIII2bis: { title: this.qText(qMap, 'ETUIII2', 'III.2') + " - " + this.qBis(qMap,  'ETUIII2', 'Commentaire') },
      ETU_ETUIII4:    { title: this.qText(qMap, 'ETUIII4', 'III.4') },

      ETU_ETUIII5a:   { title: `${etuIII5Label} - ${etuIII5Items[0] ?? this.letterAt(0)}` },
      ETU_ETUIII5b:   { title: `${etuIII5Label} - ${etuIII5Items[1] ?? this.letterAt(1)}` },
      ETU_ETUIII5c:   { title: `${etuIII5Label} - ${etuIII5Items[2] ?? this.letterAt(2)}` },
      ETU_ETUIII5bis: { title: this.qText(qMap, 'ETUIII5', 'III.5') + " - " + this.qBis(qMap, 'ETUIII5', 'Commentaire') },

      ETU_ETUIII6:    { title: this.qText(qMap, 'ETUIII6', 'III.6') },
      ETU_ETUIII6bis: { title: this.qText(qMap, 'ETUIII6', 'III.6') + " - " + this.qBis(qMap,  'ETUIII6', 'Commentaire') },
      ETU_ETUIII7:    { title: this.qText(qMap, 'ETUIII7', 'III.7') },
      ETU_ETUIII7bis: { title: this.qText(qMap, 'ETUIII7', 'III.7') + " - " + this.qBis(qMap,  'ETUIII7', 'Commentaire') },
      ETU_ETUIII8:    { title: this.qText(qMap, 'ETUIII8', 'III.8') },
      ETU_ETUIII8bis: { title: this.qText(qMap, 'ETUIII8', 'III.8') + " - " + this.qBis(qMap,  'ETUIII8', 'Commentaire') },
      ETU_ETUIII9:    { title: this.qText(qMap, 'ETUIII9', 'III.9') },
      ETU_ETUIII9bis: { title: this.qText(qMap, 'ETUIII9', 'III.9') + " - " + this.qBis(qMap,  'ETUIII9', 'Commentaire') },
      ETU_ETUIII10:   { title: this.qText(qMap, 'ETUIII10', 'III.10') },
      ETU_ETUIII11:   { title: this.qText(qMap, 'ETUIII11', 'III.11') },
      ETU_ETUIII12:   { title: this.qText(qMap, 'ETUIII12', 'III.12') },
      ETU_ETUIII14:   { title: this.qText(qMap, 'ETUIII14', 'III.14') },
      ETU_ETUIII15:   { title: this.qText(qMap, 'ETUIII15', 'III.15') },
      ETU_ETUIII15bis:{ title: this.qText(qMap, 'ETUIII15', 'III.15') + " - " + this.qBis(qMap,  'ETUIII15', 'Commentaire') },
      ETU_ETUIII16:   { title: this.qText(qMap, 'ETUIII16', 'III.16') },
      ETU_ETUIII16bis:{ title: this.qText(qMap, 'ETUIII16', 'III.16') + " - " + this.qBis(qMap,  'ETUIII16', 'Commentaire') },
    };

    // Enseignant : sous-questions
    const ensI1 = this.qText(qMap, 'ENSI1', 'Suivi avec le stagiaire');
    const ensI1Items = this.qItems(qMap, 'ENSI1');
    const ensI2 = this.qText(qMap, 'ENSI2', 'Suivi avec le tuteur professionnel');
    const ensI2Items = this.qItems(qMap, 'ENSI2');

    const enseignant: Record<string, { title: string }> = {
      ENS_ID_CONVENTION: { title: 'N° convention' },
      ENS_NOM_ETUDIANT: { title: 'Nom Étudiant' },
      ENS_PRENOM_ETUDIANT: { title: 'Prénom Étudiant' },
      ENS_STRUCTURE: { title: 'Structure' },
      ENS_DATE_DEBUT_STAGE: { title: 'Date Début Stage' },
      ENS_DATE_FIN_STAGE: { title: 'Date Fin Stage' },
      ENS_CENTRE_GESTION: { title: 'Centre de Gestion' },
      ENS_ETAPE: { title: 'Étape' },
      ENS_ANNEE_UNIVERSITAIRE: { title: 'Année Universitaire' },

      ENS_ENSI1a: { title: `${ensI1} - ${ensI1Items[0] ?? 'a'}` },
      ENS_ENSI1b: { title: `${ensI1} - ${ensI1Items[1] ?? 'b'}` },
      ENS_ENSI1c: { title: `${ensI1} - ${ensI1Items[2] ?? 'c'}` },

      ENS_ENSI2a: { title: `${ensI2} - ${ensI2Items[0] ?? 'a'}` },
      ENS_ENSI2b: { title: `${ensI2} - ${ensI2Items[1] ?? 'b'}` },
      ENS_ENSI2c: { title: `${ensI2} - ${ensI2Items[2] ?? 'c'}` },

      ENS_ENSI3:   { title: this.qText(qMap, 'ENSI3', 'Commentaire(s)') },
      ENS_ENSII1:  { title: this.qText(qMap, 'ENSII1', 'II.1') },
      ENS_ENSII10: { title: this.qText(qMap, 'ENSII10', 'II.10') },
      ENS_ENSII11: { title: this.qText(qMap, 'ENSII11', 'II.11') },
      ENS_ENSII2:  { title: this.qText(qMap, 'ENSII2', 'II.2') },
      ENS_ENSII3:  { title: this.qText(qMap, 'ENSII3', 'II.3') },
      ENS_ENSII4:  { title: this.qText(qMap, 'ENSII4', 'II.4') },
      ENS_ENSII5:  { title: this.qText(qMap, 'ENSII5', 'II.5') },
      ENS_ENSII6:  { title: this.qText(qMap, 'ENSII6', 'II.6') },
      ENS_ENSII7:  { title: this.qText(qMap, 'ENSII7', 'II.7') },
      ENS_ENSII8:  { title: this.qText(qMap, 'ENSII8', 'II.8') },
      ENS_ENSII9:  { title: this.qText(qMap, 'ENSII9', 'II.9') },
    };

    // Entreprise
    // Entreprise (avec les bis => "<libellé principal> - <libellé bis>")
    const entreprise: Record<string, { title: string }> = {
      ENT_ID_CONVENTION: { title: 'N° convention' },
      ENT_NOM_ETUDIANT: { title: 'Nom Étudiant' },
      ENT_PRENOM_ETUDIANT: { title: 'Prénom Étudiant' },
      ENT_STRUCTURE: { title: 'Structure' },
      ENT_DATE_DEBUT_STAGE: { title: 'Date Début Stage' },
      ENT_DATE_FIN_STAGE: { title: 'Date Fin Stage' },
      ENT_CENTRE_GESTION: { title: 'Centre de Gestion' },
      ENT_ETAPE: { title: 'Étape' },
      ENT_ANNEE_UNIVERSITAIRE: { title: 'Année Universitaire' },

      ENT_ENT1:    { title: this.qText(qMap, 'ENT1',  'Ent 1') },
      ENT_ENT1bis: { title: `${this.qText(qMap, 'ENT1',  'Ent 1')} - ${this.qBis(qMap,  'ENT1',  'Commentaire')}` },

      ENT_ENT2:    { title: this.qText(qMap, 'ENT2',  'Ent 2') },
      ENT_ENT2bis: { title: `${this.qText(qMap, 'ENT2',  'Ent 2')} - ${this.qBis(qMap,  'ENT2',  'Commentaire')}` },

      ENT_ENT3:    { title: this.qText(qMap, 'ENT3',  'Ent 3') },

      ENT_ENT5:    { title: this.qText(qMap, 'ENT5',  'Ent 4') },
      ENT_ENT5bis: { title: `${this.qText(qMap, 'ENT5',  'Ent 4')} - ${this.qBis(qMap,  'ENT5',  'Commentaire')}` },

      ENT_ENT9:    { title: this.qText(qMap, 'ENT9',  'Ent 5') },
      ENT_ENT9bis: { title: `${this.qText(qMap, 'ENT9',  'Ent 5')} - ${this.qBis(qMap,  'ENT9',  'Commentaire')}` },

      ENT_ENT11:   { title: this.qText(qMap, 'ENT11', 'Ent 6') },
      ENT_ENT11bis:{ title: `${this.qText(qMap, 'ENT11', 'Ent 6')} - ${this.qBis(qMap,  'ENT11', 'Commentaire')}` },

      ENT_ENT12:   { title: this.qText(qMap, 'ENT12', 'Ent 7') },
      ENT_ENT12bis:{ title: `${this.qText(qMap, 'ENT12', 'Ent 7')} - ${this.qBis(qMap,  'ENT12', 'Commentaire')}` },

      ENT_ENT13:   { title: this.qText(qMap, 'ENT13', 'Ent 8') },
      ENT_ENT13bis:{ title: `${this.qText(qMap, 'ENT13', 'Ent 8')} - ${this.qBis(qMap,  'ENT13', 'Commentaire')}` },

      ENT_ENT14:   { title: this.qText(qMap, 'ENT14', 'Ent 9') },
      ENT_ENT14bis:{ title: `${this.qText(qMap, 'ENT14', 'Ent 9')} - ${this.qBis(qMap,  'ENT14', 'Commentaire')}` },

      ENT_ENT4:    { title: this.qText(qMap, 'ENT4',  'Ent 10') },
      ENT_ENT4bis: { title: `${this.qText(qMap, 'ENT4',  'Ent 10')} - ${this.qBis(qMap,  'ENT4',  'Commentaire')}` },

      ENT_ENT6:    { title: this.qText(qMap, 'ENT6',  'Ent 11') },
      ENT_ENT6bis: { title: `${this.qText(qMap, 'ENT6',  'Ent 11')} - ${this.qBis(qMap,  'ENT6',  'Commentaire')}` },

      ENT_ENT7:    { title: this.qText(qMap, 'ENT7',  'Ent 12') },
      ENT_ENT7bis: { title: `${this.qText(qMap, 'ENT7',  'Ent 12')} - ${this.qBis(qMap,  'ENT7',  'Commentaire')}` },

      ENT_ENT8:    { title: this.qText(qMap, 'ENT8',  'Ent 13') },
      ENT_ENT8bis: { title: `${this.qText(qMap, 'ENT8',  'Ent 13')} - ${this.qBis(qMap,  'ENT8',  'Commentaire')}` },

      ENT_ENT15:   { title: this.qText(qMap, 'ENT15', 'Ent 14') },
      ENT_ENT15bis:{ title: `${this.qText(qMap, 'ENT15', 'Ent 14')} - ${this.qBis(qMap,  'ENT15', 'Commentaire')}` },

      ENT_ENT16:   { title: this.qText(qMap, 'ENT16', 'Ent 15') },
      ENT_ENT16bis:{ title: `${this.qText(qMap, 'ENT16', 'Ent 15')} - ${this.qBis(qMap,  'ENT16', 'Commentaire')}` },

      ENT_ENT17:   { title: this.qText(qMap, 'ENT17', 'Ent 16') },
      ENT_ENT17bis:{ title: `${this.qText(qMap, 'ENT17', 'Ent 16')} - ${this.qBis(qMap,  'ENT17', 'Commentaire')}` },

      ENT_ENT19:   { title: this.qText(qMap, 'ENT19', 'Ent 19') },

      ENT_ENT10:   { title: this.qText(qMap, 'ENT10', 'Ent 10 (autre)') },

      ENT_ENT18:   { title: this.qText(qMap, 'ENT18', 'Ent 18') },
      ENT_ENT18bis:{ title: `${this.qText(qMap, 'ENT18', 'Ent 18')} - ${this.qBis(qMap,  'ENT18', 'Commentaire')}` },
    };

    this.exportColumns = [
      { title: 'Étudiant',  columns: etudiant },
      { title: 'Enseignant', columns: enseignant },
      { title: 'Entreprise', columns: entreprise },
    ];
  }


  private buildQuestionMap(all: any[]): Record<string, { texte?: string; bis?: string; items?: string[] }> {
    const map: Record<string, { texte?: string; bis?: string; items?: string[] }> = {};
    const safeParse = (s?: string | null) => { try { return s ? JSON.parse(s) : null; } catch { return null; } };

    for (const q of all || []) {
      const code: string = q?.code;
      if (!code) continue;
      const entry = map[code] ?? {};
      entry.texte = (q.texte ?? '').toString().trim() || entry.texte;
      entry.bis   = (q.bisQuestion ?? '').toString().trim() || entry.bis;

      // items (si paramsJson contient { items: [...] } ou structure ETUI7/…)
      const pj = safeParse((q as any).paramsJson);
      if (pj?.items && Array.isArray(pj.items)) {
        entry.items = pj.items.map((x: any) => String(x));
      } else if (pj?.oui?.items || pj?.non?.items) {
        // cas particuliers (ex: ETUI7 avec {oui:{items:[]}, non:{items:[]}})
        const oui = Array.isArray(pj?.oui?.items) ? pj.oui.items : [];
        const non = Array.isArray(pj?.non?.items) ? pj.non.items : [];
        entry.items = [...oui, ...non].map((x: any) => String(x)); // utile si tu veux les étiquettes
      }
      map[code] = entry;
    }
    return map;
  }

  private qText(qMap: Record<string, any>, code: string, fallback: string): string {
    return (qMap[code]?.texte || '').trim() || fallback;
  }
  private qBis(qMap: Record<string, any>, code: string, fallback: string): string {
    return (qMap[code]?.bis || '').trim() || fallback;
  }
  private qItems(qMap: Record<string, any>, code: string): string[] {
    return Array.isArray(qMap[code]?.items) ? qMap[code].items : [];
  }
  private letterAt(i: number): string {
    return ['a','b','c','d','e','f','g','h'][i] ?? String(i);
  }

}
