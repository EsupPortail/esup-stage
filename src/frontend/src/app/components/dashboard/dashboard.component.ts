import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../table/table.component";
import { ConventionService } from "../../services/convention.service";
import { AuthService } from "../../services/auth.service";
import { Router } from "@angular/router";
import { StructureService } from "../../services/structure.service";
import { UfrService } from "../../services/ufr.service";
import { EtapeService } from "../../services/etape.service";
import { MessageService } from "../../services/message.service";
import { ConfigService } from "../../services/config.service";
import { MatSnackBar } from "@angular/material/snack-bar";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  columns: string[] = [];
  sortColumn = 'id';
  filters: any[] = [];
  validationsOptions: any[] = [
    { id: 'validationPedagogique', libelle: 'Validée pédagogiquement' },
    { id: 'validationConvention', libelle: 'Validée administrativement' },
    { id: 'nonValidationPedagogique', libelle: 'Non validée pédagogiquement' },
    { id: 'nonValidationConvention', libelle: 'Non validée administrativement' },
  ];
  exportColumns = {};
  tableCanLoad = false;
  savedFilters: any[] = [];

  nbConventionsEnAttente: number|undefined;
  anneeEnCours: any|undefined;
  annees: any[] = [];
  typeDashboard: number = 1; // Type de tableau de bord à afficher : 1=gestionnaire/responsable/admin/profil non défini ; 2=enseignant ; 3=etudiant

  selected: any[] = [];
  validationLibelles: any = {};

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public conventionService: ConventionService,
    private authService: AuthService,
    private router: Router,
    public structureService: StructureService,
    private ufrService: UfrService,
    private etapeService: EtapeService,
    private messageService: MessageService,
    private configService: ConfigService,
    private snackBar: MatSnackBar,
  ) {
  }

  ngOnInit(): void {
    this.configService.getConfigGenerale().subscribe((response: any) => {
      let filtersString: any = sessionStorage.getItem('dashboard-filters');
      this.savedFilters = JSON.parse(filtersString);
      this.validationLibelles.validationPedagogique = response.validationPedagogiqueLibelle;
      this.validationLibelles.validationConvention = response.validationAdministrativeLibelle;

      if (this.authService.isGestionnaire()) {
        this.typeDashboard = 1;
        this.setDataGestionnaire();
      } else if (this.authService.isEnseignant()) {
        this.typeDashboard = 2;
        this.columns = ['id', 'etudiant', 'ufr', 'etape',  'dateDebutStage', 'dateFinStage', 'structure', 'sujetStage', 'lieuStage', 'etatValidation', 'avenant', 'action'];
        this.filters = [
          { id: 'id', libelle: 'N° de la convention', type: 'int' },
          { id: 'etudiant', libelle: 'Étudiant', specific: true },
          { id: 'ufr.id', libelle: 'Composante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
          { id: 'etape.id', libelle: 'Étape', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
          { id: 'dateDebutStage', libelle: 'Date début du stage', type: 'date-min' },
          { id: 'dateFinStage', libelle: 'Date fin du stage', type: 'date-max' },
          { id: 'structure.id', libelle: 'Établissement d\'accueil', type: 'autocomplete', autocompleteService: this.structureService, keyLibelle: 'raisonSociale', keyId: 'id', value: [] },
          { id: 'sujetStage', libelle: 'Sujet du stage' },
          { id: 'lieuStage', libelle: 'Lieu du stage' },
          { id: 'etatValidation', libelle: 'État de validation de la convention', type: 'list', options: this.validationsOptions, keyLibelle: 'libelle', keyId: 'id', value: ['nonValidationPedagogique', 'nonValidationConvention'], specific: true },
          { id: 'avenant', libelle: 'Avenant', specific: true },
        ];

        this.exportColumns = {
          id: { title: 'N° de la convention' },
          etudiant: { title: 'Étudiant' },
          ufr: { title: 'Composante' },
          etape: { title: 'Étape' },
          dateDebutStage: { title: 'Date début du stage' },
          dateFinStage: { title: 'Date fin du stage' },
          structure: { title: 'Établissement d\'accueil' },
          sujetStage: { title: 'Sujet du stage' },
          lieuStage: { title: 'Sujet du stage' },
          validationPedagogique: { title: this.validationLibelles.validationPedagogique },
          validationConvention: { title: this.validationLibelles.validationConvention },
          avenant: { title: 'Avenant' },
        };
      } else if (this.authService.isEtudiant()) {
        this.typeDashboard = 3;
        this.columns = ['id', 'structure', 'dateDebutStage', 'dateFinStage', 'ufr', 'etape', 'enseignant', 'validationPedagogique', 'validationConvention', 'avenant', 'annee', 'action'];
        this.filters = [
          { id: 'id', libelle: 'N° de la convention', type: 'int' },
          { id: 'structure.id', libelle: 'Établissement d\'accueil', type: 'autocomplete', autocompleteService: this.structureService, keyLibelle: 'raisonSociale', keyId: 'id', value: [] },
          { id: 'dateDebutStage', libelle: 'Date début du stage', type: 'date' },
          { id: 'dateFinStage', libelle: 'Date fin du stage', type: 'date' },
          { id: 'ufr.id', libelle: 'Composante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
          { id: 'etape.id', libelle: 'Étape', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
          { id: 'enseignant', libelle: 'Enseignant', specific: true },
          { id: 'validationPedagogique', libelle: this.validationLibelles.validationPedagogique, type: 'boolean' },
          { id: 'validationConvention', libelle: this.validationLibelles.validationConvention, type: 'boolean' },
          { id: 'avenant', libelle: 'Avenant', specific: true },
          { id: 'annee', libelle: 'Année', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'libelle', value: [] },
        ];

        this.exportColumns = {
          id: { title: 'N° de la convention' },
          etudiant: { title: 'Étudiant', specific: true },
          structure: { title: 'Établissement d\'accueil' },
          dateDebutStage: { title: 'Date début du stage' },
          dateFinStage: { title: 'Date fin du stage' },
          ufr: { title: 'Composante', specific: true },
          etape: { title: 'Étape', specific: true },
          enseignant: { title: 'Enseignant', specific: true },
          validationPedagogique: { title: this.validationLibelles.validationPedagogique },
          validationConvention: { title: this.validationLibelles.validationConvention },
          avenant: { title: 'Avenant', specific: true },
          annee: { title: 'Année' },
        };
      } else {
        this.typeDashboard = 1;
        this.setDataGestionnaire();
      }
      this.filters.push({ id: 'validationCreation', type: 'boolean', value: true, hidden: true });

      this.ufrService.getPaginated(1, 0, 'libelle', 'asc', '{}').subscribe((response: any) => {
        this.appTable?.setFilterOption('ufr.id', response.data);
      });

      this.etapeService.getPaginated(1, 0, 'libelle', 'asc', '{}').subscribe((response: any) => {
        this.appTable?.setFilterOption('etape.id', response.data);
      });

      this.conventionService.getListAnnee().subscribe((response: any) => {
        this.annees = response;
        this.anneeEnCours = this.annees.find((a: any) => { return a.anneeEnCours === true });
        if (!this.authService.isEtudiant()) {
          this.changeAnnee();
        } else {
          this.appTable?.setFilterOption('annee', this.annees);
          this.appTable?.setFilterValue('annee', [this.anneeEnCours.libelle]);
        }
        if (this.savedFilters) {
          this.restoreFilters();
        }
      });

      this.tableCanLoad = true;
    });
  }

  setDataGestionnaire(): void {
    this.columns = ['select', 'id', 'etudiant', 'structure', 'dateDebutStage', 'dateFinStage', 'ufr', 'etape', 'enseignant', 'etatValidation', 'action'];
    this.filters = [
      { id: 'id', libelle: 'N° de la convention', type: 'int' },
      { id: 'etudiant', libelle: 'Étudiant', specific: true },
      { id: 'structure.id', libelle: 'Établissement d\'accueil', type: 'autocomplete', autocompleteService: this.structureService, keyLibelle: 'raisonSociale', keyId: 'id', value: [] },
      { id: 'dateDebutStage', libelle: 'Date début du stage', type: 'date' },
      { id: 'dateFinStage', libelle: 'Date fin du stage', type: 'date' },
      { id: 'ufr.id', libelle: 'Composante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
      { id: 'etape.id', libelle: 'Étape', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
      { id: 'enseignant', libelle: 'Enseignant', specific: true },
      { id: 'etatValidation', libelle: 'État de validation de la convention', type: 'list', options: this.validationsOptions, keyLibelle: 'libelle', keyId: 'id', value: ['validationPedagogique', 'nonValidationConvention'], specific: true },
    ];

    this.exportColumns = {
      id: { title: 'N° de la convention' },
      etudiant: { title: 'Étudiant' },
      structure: { title: 'Établissement d\'accueil' },
      dateDebutStage: { title: 'Date début du stage' },
      dateFinStage: { title: 'Date fin du stage' },
      ufr: { title: 'Composante' },
      etape: { title: 'Étape' },
      enseignant: { title: 'Enseignant' },
      validationPedagogique: { title: this.validationLibelles.validationPedagogique },
      validationConvention: { title: this.validationLibelles.validationConvention },
    };
  }

  changeAnnee(): void {
    this.appTable?.setFilter({id: 'annee', type: 'text', value: this.anneeEnCours.libelle, specific: false});
    this.appTable?.update();
    this.countConvention();
    // Compte le nombre de conventions dont la date de validation se rapproche ou dépasse la date de début du stage
    this.conventionService.countConventionEnAttenteAlerte(this.anneeEnCours.annee).subscribe((response: number) => {
      if (response > 0) {
        this.snackBar.open(`${response} convention(s) à valider dont la date de validation se rapproche ou dépasse la date de début du stage`, 'Fermer', {
          horizontalPosition: 'center',
          verticalPosition: 'top',
        });
      }
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

  countConvention(): void {
    if (this.typeDashboard !== 3 && this.appTable) {
      const filters = this.appTable.getFilters();
      this.conventionService.getPaginated(1, 1, '', '', JSON.stringify(filters)).subscribe((response: any) => {
        this.nbConventionsEnAttente = response.total;
      });
    }
    if (this.appTable) {
      const filters = this.appTable?.getFilterValues();
      sessionStorage.setItem('dashboard-filters', JSON.stringify(filters));
    }
  }

  goToConvention(id: number): void {
    this.router.navigate([`/conventions/${id}`], )
  }

  validationAdministrative(): void {
    const ids = this.selected.map((s: any) => s.id);
    this.conventionService.validationAdministrative(ids).subscribe((response: any) => {
      this.messageService.setSuccess(`${response} convention(s) validée(s)`);
      this.selected = [];
      this.countConvention();
      this.appTable?.update();
    });
  }

  restoreFilters() {
    if (this.savedFilters) {
      Object.keys(this.savedFilters).forEach((key: any) => {
        if (this.savedFilters[key].type === 'date' && this.savedFilters[key].value) {
          this.appTable?.setFilterValue(key, new Date(this.savedFilters[key].value));
        }
        else if (this.savedFilters[key].value)
          this.appTable?.setFilterValue(key, this.savedFilters[key].value);
      });
    }
  }
}


