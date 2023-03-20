import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../table/table.component";
import { ConventionService } from "../../services/convention.service";
import { AuthService } from "../../services/auth.service";
import { Router } from "@angular/router";
import { SortDirection } from "@angular/material/sort";
import { TitleService } from "../../services/title.service";

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
  ) {
  }

  ngOnDestroy(): void {
    sessionStorage.setItem('evalstages-paging', JSON.stringify({page: this.appTable?.page, pageSize: this.appTable?.pageSize, sortColumn: this.appTable?.sortColumn, sortOrder: this.appTable?.sortOrder}));
    sessionStorage.setItem('evalstages-filters', JSON.stringify(this.appTable?.getFilterValues()))
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
      this.columns = ['id', 'etudiant.nom_etudiant.prenom', 'structure.raisonSociale', 'dateDebutStage', 'dateFinStage', 'ufr.libelle',
     'etape.libelle', 'annee','reponseEvaluationEtudiant','reponseEvaluationEnseignant','reponseEvaluationEntreprise', 'action'];

      this.filters.push({ id: 'annee', libelle: 'Année', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'libelle', value: [] });
      this.filters.push({ id: 'centreGestion.nomCentre', libelle: 'Centre de gestion'},);
      this.filters.push({ id: 'stageTermine', libelle: 'N\'afficher que les stages terminés ?', type: 'boolean', specific: true });

      this.conventionService.getListAnnee().subscribe(response => {
        this.annees = response;
        this.anneeEnCours = this.annees.find((a: any) => { return a.anneeEnCours === true });
        this.appTable?.setFilterOption('annee', this.annees);
        this.appTable?.setFilterValue('annee', [this.anneeEnCours.libelle]);

        if (this.savedFilters) {
          this.restoreFilters();
        }
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
  }

  goToConvention(id: number): void {
    this.conventionService.setGoToOnglet(8)
    this.router.navigate([`/conventions/${id}`], {queryParams: {back: 'eval-stages'} });
  }

  restoreFilters() {
    Object.keys(this.savedFilters).forEach((key: any) => {
      if (this.savedFilters[key].value)
        this.appTable?.setFilterValue(key, this.savedFilters[key].value);
    });
    const pagingString: string|null = sessionStorage.getItem('evalstages-paging');
    if (pagingString) {
      const pagingConfig = JSON.parse(pagingString);
      this.sortColumn = pagingConfig.sortColumn;
      this.sortDirection = pagingConfig.sortOrder;
      this.appTable?.setBackConfig(pagingConfig);
    }
  }

}
