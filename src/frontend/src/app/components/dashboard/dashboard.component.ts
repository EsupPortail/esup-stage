import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { TableComponent } from "../table/table.component";
import { ConventionService } from "../../services/convention.service";
import { AuthService } from "../../services/auth.service";
import { Router } from "@angular/router";
import { UfrService } from "../../services/ufr.service";
import { EtapeService } from "../../services/etape.service";
import { MessageService } from "../../services/message.service";
import { ConfigService } from "../../services/config.service";
import { forkJoin } from 'rxjs';
import { SortDirection } from "@angular/material/sort";
import { ContenuPipe } from "../../pipes/contenu.pipe";
import { TypeConventionService } from "../../services/type-convention.service";
import { LangueConventionService } from "../../services/langue-convention.service";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {

  columns: string[] = [];
  sortColumn = 'etudiant.nom';
  sortDirection: SortDirection = 'asc';
  filters: any[] = [];
  validationsOptions: any[] = [
    { id: 'validationPedagogique', libelle: 'Validée pédagogiquement' },
    { id: 'validationConvention', libelle: 'Validée administrativement' },
    { id: 'nonValidationPedagogique', libelle: 'Non validée pédagogiquement' },
    { id: 'nonValidationConvention', libelle: 'Non validée administrativement' },
    { id: 'signe', libelle: 'Signé' },
    { id: 'enCours', libelle: 'En cours de signature' },
    { id: 'nonSigne', libelle: 'Non signé' },
  ];
  exportColumns = {};
  tableCanLoad = false;
  savedFilters: any[] = [];

  nbConventionsEnAttente: number | undefined;
  anneeEnCours: any|undefined;
  annees: any[] = [];
  ufrList: any[] = [];
  etapeList: any[] = [];
  typeDashboard: number = 1; // Type de tableau de bord à afficher : 1=gestionnaire/responsable/admin/profil non défini ; 2=enseignant ; 3=etudiant
  langueConventionList: any[] = [];
  typeConventionList: any[] = [];

  selected: any[] = [];
  validationLibelles: any = {};

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public conventionService: ConventionService,
    private authService: AuthService,
    private router: Router,
    private ufrService: UfrService,
    private etapeService: EtapeService,
    private messageService: MessageService,
    private configService: ConfigService,
    private contenuPipe: ContenuPipe,
    private langueConventionService: LangueConventionService,
    private typeConventionService: TypeConventionService,
  ) {
    this.columns = [];
    this.filters = [];
    this.selected = [];
    this.annees = [];
    this.ufrList = [];
    this.etapeList = [];
    this.langueConventionList = [];
    this.typeConventionList = [];
    this.validationLibelles = {};
  }

  ngOnInit(): void {
    this.configService.getConfigGenerale().subscribe({
      next: (response: any) => {
        this.initializeValidationLibelles(response);
        this.initializeDashboardType();
        this.loadSavedFilters();
        this.addPermanentFilters();
        this.tableCanLoad = true;
        this.loadInitialData();
      },
      error: (error) => {
        console.error('Error loading config:', error);
        this.messageService.setError('Error loading configuration');
      }
    });
  }

  private initializeValidationLibelles(response: any): void {
    this.validationLibelles = {
      validationPedagogique: response.validationPedagogiqueLibelle || 'Validation pédagogique',
      verificationAdministrative: 'Vérification administrative',
      validationConvention : 'Validation convention'
    };
  }

  private initializeDashboardType(): void {
    if (this.authService.isGestionnaire()) {
      this.typeDashboard = 1;
      this.setDataGestionnaire();
    } else if (this.authService.isEnseignant()) {
      this.typeDashboard = 2;
      this.setDataEnseignant();
    } else if (this.authService.isEtudiant()) {
      this.typeDashboard = 3;
      this.setDataEtudiant();
    } else {
      this.typeDashboard = 1;
      this.setDataGestionnaire();
    }
  }

  private loadSavedFilters(): void {
    const filtersString = sessionStorage.getItem('dashboard-filters');
    if (filtersString) {
      try {
        this.savedFilters = JSON.parse(filtersString);
      } catch (error) {
        console.error('Error parsing saved filters:', error);
        this.savedFilters = [];
      }
    }
  }

  private addPermanentFilters(): void {
    this.filters.push({
      id: 'validationCreation',
      type: 'boolean',
      value: true,
      hidden: true,
      permanent: true
    });
  }

  loadInitialData(): void {
    this.conventionService.getListAnnee().subscribe({
      next: (listAnneeData) => {
        this.setupAnnees(listAnneeData);
        this.loadComplementaryData();
      },
      error: (error) => {
        console.error('Error loading années:', error);
        this.messageService.setError('Error loading années');
      }
    });
  }

  private setupAnnees(listAnneeData: any[]): void {
    this.annees = listAnneeData;
    this.anneeEnCours = this.annees.find((a: any) => a.anneeEnCours === true);

    if (!this.authService.isEtudiant()) {
      this.annees.push({
        annee: "any",
        libelle: "Toutes les années",
        anneeEnCours: false,
        any: true
      });
    }
  }

  private loadComplementaryData(): void {
    forkJoin({
      ufr: this.ufrService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
      etape: this.etapeService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
      langueConvention: this.langueConventionService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
      typeConvention: this.typeConventionService.getPaginated(1, 0, 'libelle', 'asc', '{}')
    }).subscribe({
      next: (results) => {
        this.ufrList = results.ufr.data || [];
        this.etapeList = results.etape.data || [];
        this.langueConventionList = results.langueConvention.data || [];
        this.typeConventionList = results.typeConvention.data || [];

        if (this.appTable) {
          this.appTable.update();
          this.changeAnnee();
        }

        this.updateFilterOptions();

        if (this.savedFilters) {
          this.restoreFilters();
        }
      },
      error: (error) => {
        console.error('Error loading complementary data:', error);
        this.messageService.setError('Error loading data');
      }
    });
  }

  private updateFilterOptions(): void {
    if (this.appTable) {
      this.appTable.setFilterOption('ufr.id', this.ufrList);
      this.appTable.setFilterOption('etape.id', this.etapeList);
      this.appTable.setFilterOption('langueConvention.code', this.langueConventionList);
      this.appTable.setFilterOption('typeConvention.id', this.typeConventionList);

      if (this.authService.isEtudiant()) {
        this.appTable.setFilterOption('annee', this.annees);
      }
    }
  }

  setDataGestionnaire(): void {
    this.columns = ['select', 'id', 'etudiant.nom', 'etudiant.prenom', 'structure.raisonSociale', 'dateDebutStage', 'dateFinStage', 'ufr.libelle', 'etape.libelle', 'enseignant.prenom', 'avenant', 'etatValidation', 'action'];
    this.filters = [
      { id: 'id', libelle: 'N° de la convention', type: 'int' },
      { id: 'etudiant', libelle: 'Étudiant', specific: true },
      { id: 'structure', libelle: 'Établissement d\'accueil', specific: true },
      { id: 'dateDebutStage', libelle: 'Date début du stage', type: 'date' },
      { id: 'dateFinStage', libelle: 'Date fin du stage', type: 'date' },
      { id: 'enseignant', libelle: 'Enseignant', specific: true },
      { id: 'avenant', libelle: 'Avenant', type: 'boolean', specific: true },
      { id: 'etatValidation', libelle: 'État de validation de la convention', type: 'list', options: this.validationsOptions, keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
      { id: 'ufr.id', libelle: 'Composante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
      { id: 'etape.id', libelle: 'Étape', type: 'autocomplete', autocompleteService: this.etapeService, options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true, colSpan: 9 },
      { id: 'langueConvention.code', libelle: 'Langue de convention', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'code', value: [] },
      { id: 'typeConvention.id', libelle: 'Type de convention', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
    ];

    this.exportColumns = {
      multipleExcelSheets: [
        {
          title: 'Données stage',
          columns: {
            id: { title: 'N° de la convention' },
            numEtudiant: { title: 'N° étudiant' },
            etudiant: { title: 'Étudiant (NOM Prénom)' },
            courrielPersoEtudiant: { title: 'Mail perso étudiant' },
            mailUniEtudiant: { title: 'Mail universitaire étudiant' },
            telEtudiant: { title: 'Téléphone perso étudiant' },
            telPortableEtudiant: { title: 'Téléphone portable étudiant' },
            codeUFR: { title: 'Code UFR' },
            ufr: { title: 'Libellé UFR' },
            codeDepartement: { title: 'Code département' },
            codeEtape: { title: 'Code étape' },
            etape: { title: 'Libellé étape' },
            dateDebutStage: { title: 'Date début du stage' },
            dateFinStage: { title: 'Date fin du stage' },
            interruptionStage: { title: 'Interruption' },
            dateDebutInterruption: { title: 'Date début interruption' },
            dateFinInterruption: { title: 'Date fin interruption' },
            theme: { title: 'Thématique' },
            sujetStage: { title: 'Sujet' },
            fonctionsEtTaches: { title: 'Fonctions et tâches' },
            details: { title: 'Détail du projet' },
            dureeExceptionnelle: { title: 'Durée du stage' },
            nbJoursHebdo: { title: 'Nbre de jours de travail' },
            nbHeuresHebdo: { title: 'Nbre d’heures hebdomadaire' },
            gratification: { title: 'Gratification' },
            uniteDuree: { title: 'Unité Durée gratification' },
            validationConvention: { title: this.validationLibelles.validationConvention },
            enseignant: { title: 'Enseignant référent (NOM Prénom)' },
            mailEnseignant: { title: 'Mail enseignant référent' },
            signataire: { title: 'Signataire (NOM Prénom)' },
            mailSignataire: { title: 'Mail signataire' },
            fonctionSignataire: { title: 'Fonction signataire' },
            annee: { title: 'Année universitaire' },
            typeConvention: { title: 'Type convention' },
            commentaireStage: { title: 'Commentaire stage' },
            commentaireDureeTravail: { title: 'Commentaire durée travail' },
            codeElp: { title: 'Code ELP' },
            libelleELP: { title: 'Elément pédagogique' },
            codeSexeEtu: { title: 'Code sexe étudiant' },
            avantageNature: { title: 'Avantage nature' },
            adresseEtudiant: { title: 'Adresse étudiant' },
            codePostalEtudiant: { title: 'Code postal étudiant' },
            paysEtudiant: { title: 'Pays étudiant' },
            villeEtudiant: { title: 'Ville étudiant' },
            validationPedagogique: { title: this.validationLibelles.validationPedagogique },
            avenant: { title: 'Avenant(s) à la convention' },
            dateCreation: { title: 'Date création convention' },
            dateModif: { title: 'Date modification convention' },
          }
        },
        {
          title: 'Données structure d\’accueil',
          columns: {
            id: { title: 'N° de la convention' },
            numEtudiant: { title: 'N° étudiant' },
            etudiant: { title: 'Étudiant (NOM Prénom)' },
            structure: { title: 'Nom structure d\’accueil' },
            structureSiret: { title: 'SIRET' },
            structureAdresse: { title: 'Adresse' },
            structureCP: { title: 'Code postal' },
            structureCommune: { title: 'Commune' },
            structurePays: { title: 'Pays' },
            structureStatutJuridique: { title: 'Statut juridique' },
            structureType: { title: 'Type de structure' },
            structureEffectif: { title: 'Effectif' },
            structureNAF: { title: 'Code NAF' },
            structurePhone: { title: 'Téléphone' },
            structureMail: { title: 'Mail' },
            structureSiteWeb: { title: 'Site web' },
            service: { title: 'Service d\’accueil – Nom' },
            serviceAdresse: { title: 'Service d\’accueil – Adresse' },
            serviceCP: { title: 'Service d\’accueil – Code postal' },
            serviceCommune: { title: 'Service d\’accueil - Commune' },
            servicePays: { title: 'Service d\’accueil – Pays' },
            tuteur: { title: 'Tuteur professionnel (Nom Prénom)' },
            tuteurMail: { title: 'Mail tuteur professionnel' },
            tuteurPhone: { title: 'Téléphone tuteur professionnel' },
            tuteurFonction: { title: 'Fonction tuteur professionnel' },
          }
        }
      ]
    };
  }

  setDataEnseignant():void{
  this.columns = ['id', 'etudiant.nom', 'etudiant.prenom', 'ufr.libelle', 'etape.libelle', 'dateDebutStage', 'dateFinStage', 'structure.raisonSociale', 'sujetStage', 'lieuStage', 'avenant', 'etatValidation', 'action'];
  this.filters = [
    { id: 'id', libelle: 'N° de la convention', type: 'int' },
    { id: 'etudiant', libelle: 'Étudiant', specific: true },
    { id: 'ufr.id', libelle: 'Composante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
    { id: 'etape.id', libelle: 'Étape', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
    { id: 'dateDebutStage', libelle: 'Date début du stage', type: 'date-min' },
    { id: 'dateFinStage', libelle: 'Date fin du stage', type: 'date-max' },
    { id: 'structure', libelle: 'Établissement d\'accueil', specific: true },
    { id: 'sujetStage', libelle: 'Sujet du stage' },
    { id: 'lieuStage', libelle: 'Lieu du stage' },
    { id: 'avenant', libelle: 'Avenant', type: 'boolean', specific: true },
    { id: 'etatValidation', libelle: 'État de validation de la convention', type: 'list', options: this.validationsOptions, keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
    { id: 'langueConvention.code', libelle: 'Langue de convention', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'code', value: [] },
    { id: 'typeConvention.id', libelle: 'Type de convention', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
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
    avenant: { title: 'Avenant' },
    validationPedagogique: { title: this.validationLibelles.validationPedagogique },
    validationConvention: { title: this.validationLibelles.validationConvention },
    langueConvention: { title: 'Langue de convention' },
    typeConvention: { title: 'Type de convention' },
  };
}
  setDataEtudiant():void{
    this.columns = ['id', 'structure.raisonSociale', 'dateDebutStage', 'dateFinStage', 'ufr.libelle', 'etape.libelle', 'enseignant.prenom', 'avenant', 'etatValidation', 'annee', 'action'];
    this.filters = [
      { id: 'id', libelle: 'N° de la convention', type: 'int' },
      { id: 'structure', libelle: 'Établissement d\'accueil', specific: true },
      { id: 'dateDebutStage', libelle: 'Date début du stage', type: 'date' },
      { id: 'dateFinStage', libelle: 'Date fin du stage', type: 'date' },
      { id: 'ufr.id', libelle: 'Composante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
      { id: 'etape.id', libelle: 'Étape', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
      { id: 'enseignant', libelle: 'Enseignant', specific: true },
      { id: 'avenant', libelle: 'Avenant', type: 'boolean', specific: true },
      { id: 'etatValidation', libelle: 'État de validation de la convention', type: 'list', options: this.validationsOptions, keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
      { id: 'annee', libelle: 'Année', type: 'annee', options: [], keyLibelle: 'libelle', keyId: 'libelle', value: [] },
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
      avenant: { title: 'Avenant', specific: true },
      validationPedagogique: { title: this.validationLibelles.validationPedagogique },
      validationConvention: { title: this.validationLibelles.validationConvention },
      annee: { title: 'Année' },
    };
  }

  changeAnnee(): void {
    if (!this.appTable || !this.anneeEnCours){
      console.error("erreur lors du chargement du tableau")
    }else if(this.appTable?.getFilters().annee?.value === "Toutes les années"){
      delete this.appTable.filterValues['annee'];
      this.appTable?.update();
      this.countConvention();
    } else {

      // Mettre à jour le filtre
      this.appTable?.setFilter({
        id: 'annee',
        type: 'text',
        value: this.anneeEnCours.any ? "" : this.anneeEnCours.libelle,
        specific: false,
        permanent: true
      });

      // mise à jour du tableau
      this.appTable.update();
      this.countConvention();
    }
  }


  isSelected(data: any): boolean {
    return this.selected.find((r: any) => { return r.id === data.id }) !== undefined;
  }

  toggleSelected(data: any): void {
    const index = this.selected.findIndex((r: any) => { return r.id === data.id });
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
      const index = this.selected.findIndex((r: any) => { return r.id === data.id });
      if (index === -1) {
        allSelected = false;
      }
    });
    return allSelected;
  }

  countConvention(): void {
    if (this.typeDashboard !== 3 && this.appTable) {
      this.nbConventionsEnAttente = this.appTable.total;
    }
    if (this.appTable) {
      const filters = this.appTable?.getFilterValues();
      sessionStorage.setItem('dashboard-filters', JSON.stringify(filters));
    }
  }

  goToConvention(id: number): void {
    this.router.navigate([`/conventions/${id}`],)
  }

  validationAdministrative(): void {
    // Message d'erreur si au moins une convention est validée administrativement
    if (this.selected.filter((c: any) => c.validationConvention).length > 0) {
      this.messageService.setError(this.contenuPipe.transform('CONVENTION_DEJA_VALIDEE_ADMIN'));
      return;
    }
    const ids = this.selected.map((s: any) => s.id);
    this.conventionService.validationAdministrative(ids).subscribe((response: any) => {
      this.messageService.setSuccess(`${response} convention(s) validée(s)`);
      this.selected = [];
      this.countConvention();
      this.appTable?.update();
    });
  }

  envoiSignatureElectronique(): void {
    const ids = this.selected.map((s: any) => s.id);
    this.conventionService.envoiSignatureElectronique(ids).subscribe((response: any) => {
      this.messageService.setSuccess(`${response} convention(s) envoyée(s)`);
      this.selected = [];
      this.appTable?.update();
    });
  }

  private restoreFilters(): void {
    if (!this.appTable || !this.savedFilters) {
      console.error("Required data not yet loaded");
      return;
    }

    Object.entries(this.savedFilters).forEach(([key, filterValue]: [string, any]) => {
      if (key === 'validationCreation') return;
      try {
        switch (key) {
          case 'annee':
            if (filterValue.value) {
              this.anneeEnCours = this.annees.find((a: any) => a.libelle === filterValue.value);
              if (this.anneeEnCours) {
                this.changeAnnee();
              }
            }
            break;

          case 'ufr.id':
            if (Array.isArray(filterValue.value) && filterValue.value.length > 0) {
              const ufrSelectedList = filterValue.value
                .map((value: any) => {

                  // Vérifie que la donnée filtrée contient un champ 'code'
                  const ufrCode = value.code;
                  if (!ufrCode) {
                    return null;
                  }

                  // Recherche dans la liste ufrList en comparant les codes
                  const found = this.ufrList.find((ufr: any) => ufr.id.code === ufrCode);
                  return found;
                })
                .filter(Boolean);  // Supprime les éléments null ou undefined
              if (ufrSelectedList.length) {
                this.appTable?.setFilter({
                  id: 'ufr.id',
                  type:'list',
                  value: ufrSelectedList.map((ufr: { id: any; }) => ufr.id),
                  specific:true
                });
              }
            }
            break;

          case 'etape.id':
            if (Array.isArray(filterValue.value) && filterValue.value.length > 0) {
              const etapeSelectedList = filterValue.value
                .map((etape: any) => {
                  const etapeId = etape.id || etape;
                  const found = this.etapeList.find((e: any) =>
                    JSON.stringify(e.id) === JSON.stringify(etapeId));
                  return found;
                })
                .filter(Boolean);

              if (etapeSelectedList.length) {
                this.appTable?.setFilterValue(key, etapeSelectedList);
              }
            }
            break;

          case 'dateDebutStage':
          case 'dateFinStage':
            if (filterValue.value) {
              const date = new Date(filterValue.value);
              if (!isNaN(date.getTime())) {
                this.appTable?.setFilterValue(key, date);
              }
            }
            break;

          default:
            if (filterValue.value !== undefined && filterValue.value !== null) {
              this.appTable?.setFilterValue(key, filterValue.value);
            }
        }
      } catch (error) {
        console.error(`Error restoring filter ${key}:`, error);
      }
    });

    if (this.appTable) {
      this.appTable.update();
    }
    this.restorePagingConfig()
  }

  private restorePagingConfig(): void {
    const pagingString = sessionStorage.getItem('dashboard-paging');
    if (pagingString && this.appTable) {
      try {
        const pagingConfig = JSON.parse(pagingString);
        this.sortColumn = pagingConfig.sortColumn;
        this.sortDirection = pagingConfig.sortOrder;
        this.appTable.setBackConfig(pagingConfig);
      } catch (error) {
        console.error('Error parsing paging config:', error);
      }
    }
  }

  getValidationIconStatus(row: any): string {
    // Vérifie si toutes les signatures sont présentes
    const allSignaturesPresent =
      row.dateSignatureEnseignant &&
      row.dateSignatureEtudiant &&
      row.dateSignatureSignataire &&
      row.dateSignatureTuteur &&
      row.dateSignatureViseur;

    // Vérifie si au moins une signature est présente mais pas toutes
    const someSignaturesPresent =
      row.dateSignatureEnseignant ||
      row.dateSignatureEtudiant ||
      row.dateSignatureSignataire ||
      row.dateSignatureTuteur ||
      row.dateSignatureViseur;

    // Retourne la classe CSS appropriée selon l'état des signatures
    if (allSignaturesPresent) {
      return 'done';        // Vert - Toutes les signatures sont présentes
    } else if (someSignaturesPresent) {
      return 'inProgress';  // Orange - Certaines signatures sont présentes
    } else {
      return 'empty';       // Rouge - Aucune signature n'est présente
    }
  }

// Message du Tooltip avec les signatures manquantes
  getValidationTooltip(row: any): string {
    const missingSignatures = [];

    if (!row.dateSignatureEnseignant) missingSignatures.push('enseignant');
    if (!row.dateSignatureEtudiant) missingSignatures.push('étudiant');
    if (!row.dateSignatureSignataire) missingSignatures.push('signataire');
    if (!row.dateSignatureTuteur) missingSignatures.push('tuteur');
    if (!row.dateSignatureViseur) missingSignatures.push('viseur');

    if (missingSignatures.length === 0) {
      return 'Toutes les signatures sont présentes';
    } else if (missingSignatures.length === 5) {
      return 'Aucune signature';
    } else {
      return `Signatures manquantes : ${missingSignatures.join(', ')}`;
    }
  }

  ngOnDestroy(): void {
    sessionStorage.setItem('dashboard-paging', JSON.stringify({ page: this.appTable?.page, pageSize: this.appTable?.pageSize, sortColumn: this.appTable?.sortColumn, sortOrder: this.appTable?.sortOrder }));
    sessionStorage.setItem('dashboard-filters', JSON.stringify(this.appTable?.getFilterValues()))
  }
}
