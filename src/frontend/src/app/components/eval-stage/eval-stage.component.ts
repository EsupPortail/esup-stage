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
    private dialog: MatDialog
  ) {
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

  openExportExcelModal() {
    this.dialog.open(ExportEvaluationComponent, {
      width: '800px',
      data: { rows:this.appTable?.data }
    });
  }
}
