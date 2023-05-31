import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
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
  sortColumn = 'etudiant.nom_etudiant.prenom';
  sortDirection: SortDirection = 'asc';
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

  nbConventionsEnAttente: number | undefined;
  anneeEnCours = {
    "annee": "any",
    "libelle": "Toutes les années",
    "anneeEnCours": false,
    "any": true
  };
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
    public structureService: StructureService,
    private ufrService: UfrService,
    private etapeService: EtapeService,
    private messageService: MessageService,
    private configService: ConfigService,
    private snackBar: MatSnackBar,
    private contenuPipe: ContenuPipe,
    private langueConventionService: LangueConventionService,
    private typeConventionService: TypeConventionService,
  ) {
  }

  ngOnInit(): void {
    this.configService.getConfigGenerale().subscribe((response: any) => {
      let filtersString: any = sessionStorage.getItem('dashboard-filters');
      this.savedFilters = JSON.parse(filtersString);
      this.validationLibelles.validationPedagogique = response.validationPedagogiqueLibelle;
      this.validationLibelles.verificationAdministrative = 'Vérification administrative';
      this.validationLibelles.validationConvention = response.validationAdministrativeLibelle;

      if (this.authService.isGestionnaire()) {
        this.typeDashboard = 1;
        this.setDataGestionnaire();
      } else if (this.authService.isEnseignant()) {
        this.typeDashboard = 2;
        this.columns = ['id', 'etudiant.nom_etudiant.prenom', 'ufr.libelle', 'etape.libelle', 'dateDebutStage', 'dateFinStage', 'structure.raisonSociale', 'sujetStage', 'lieuStage', 'avenant', 'etatValidation', 'action'];
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
      } else if (this.authService.isEtudiant()) {
        this.typeDashboard = 3;
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
          avenant: { title: 'Avenant', specific: true },
          validationPedagogique: { title: this.validationLibelles.validationPedagogique },
          validationConvention: { title: this.validationLibelles.validationConvention },
          annee: { title: 'Année' },
        };
      } else {
        this.typeDashboard = 1;
        this.setDataGestionnaire();
      }
      this.filters.push({ id: 'validationCreation', type: 'boolean', value: true, hidden: true, permanent: true });

      forkJoin(
        this.ufrService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
        this.etapeService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
        this.conventionService.getListAnnee(),
        this.langueConventionService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
        this.typeConventionService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
      ).subscribe(([ufrData, etapeData, listAnneeData, langueConventionList, typeConventionList]) => {
        // ufr
        this.ufrList = ufrData.data;
        this.appTable?.setFilterOption('ufr.id', this.ufrList);

        // etape
        this.etapeList = etapeData.data;
        this.appTable?.setFilterOption('etape.id', this.etapeList);

        // annees
        this.annees = listAnneeData;
        if (!this.authService.isEtudiant()) {
          this.annees.push(this.anneeEnCours);
        } else {
          this.appTable?.setFilterOption('annee', this.annees);
        }

        // langue convention
        this.langueConventionList = langueConventionList.data;
        this.appTable?.setFilterOption('langueConvention.code', this.langueConventionList);

        // type convention
        this.typeConventionList = typeConventionList.data;
        this.appTable?.setFilterOption('typeConvention.id', this.typeConventionList);

        if (this.savedFilters) {
          this.restoreFilters();
        }
      });

      this.tableCanLoad = true;
    });
  }

  setDataGestionnaire(): void {
    this.columns = ['select', 'id', 'etudiant.nom_etudiant.prenom', 'structure.raisonSociale', 'dateDebutStage', 'dateFinStage', 'ufr.libelle', 'etape.libelle', 'enseignant.prenom', 'avenant', 'etatValidation', 'action'];
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
            nbJoursHebdo: { title: 'Nbre de jours de travail' },//?
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

  changeAnnee(): void {
    this.appTable?.setFilter({ id: 'annee', type: 'text', value: this.anneeEnCours.any ? "" : this.anneeEnCours.libelle, specific: false });
    this.appTable?.update();
    this.countConvention();
    if (!this.anneeEnCours.any) {
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
      const filters = this.appTable.getFilters();
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

  restoreFilters() {
    if (this.savedFilters) {
      Object.keys(this.savedFilters).forEach((key: any) => {
        if (this.savedFilters[key].type === 'date' && this.savedFilters[key].value) {
          this.appTable?.setFilterValue(key, new Date(this.savedFilters[key].value));
        }
        else if (key === 'annee' && this.savedFilters[key].value) {
          this.anneeEnCours = this.annees.find((a: any) => { return a.libelle === this.savedFilters[key].value });
          this.changeAnnee();
        }
        else if (key === 'ufr.id' && this.savedFilters[key].value) {
          let ufrSelectedList: any[] = [];
          this.savedFilters[key].value.forEach((value: any) => {
            let ufrSelected = this.ufrList.find((ufr: any) => ufr.id.code === value.code);
            ufrSelectedList.push(ufrSelected.id);
          });
          this.appTable?.setFilterValue(key, ufrSelectedList);
        }
        else if (key === 'etape.id' && this.savedFilters[key].value) {
          let etapeSelectedList: any[] = [];
          this.savedFilters[key].value.forEach((value: any) => {
            let etapeSelected = this.etapeList.find((etape: any) => etape.id.code === value.id.code);
            etapeSelectedList.push(etapeSelected);
          });
          this.appTable?.setFilterValue(key, etapeSelectedList);
        }
        else if (this.savedFilters[key].value)
          this.appTable?.setFilterValue(key, this.savedFilters[key].value);
      });
    }
    const pagingString: string | null = sessionStorage.getItem('dashboard-paging');
    if (pagingString) {
      const pagingConfig = JSON.parse(pagingString);
      this.sortColumn = pagingConfig.sortColumn;
      this.sortDirection = pagingConfig.sortOrder;
      this.appTable?.setBackConfig(pagingConfig);
    }
  }

  ngOnDestroy(): void {
    sessionStorage.setItem('dashboard-paging', JSON.stringify({ page: this.appTable?.page, pageSize: this.appTable?.pageSize, sortColumn: this.appTable?.sortColumn, sortOrder: this.appTable?.sortOrder }));
    sessionStorage.setItem('dashboard-filters', JSON.stringify(this.appTable?.getFilterValues()))
  }
}


