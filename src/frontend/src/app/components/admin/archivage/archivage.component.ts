import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router } from "@angular/router";
import * as FileSaver from 'file-saver';
import { TableComponent } from "../../table/table.component";
import { ArchivageService } from "../../../services/archivage.service";
import { NettoyageService, NettoyageContactsService, NettoyageServicesService } from "../../../services/nettoyage.service";
import { MessageService } from "../../../services/message.service";
import { AuthService } from "../../../services/auth.service";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";

@Component({
    selector: 'app-archivage',
    templateUrl: './archivage.component.html',
    styleUrls: ['./archivage.component.scss'],
    standalone: false
})
export class ArchivageComponent implements OnInit, OnDestroy {

  statistiques: any;

  // --- Onglet Conventions (archivage / purge) ---
  resume: any;
  typeSimulation: string = 'archivage';
  progression: any;
  purgeVerifiee: boolean = false;
  annulationDemandee: boolean = false;
  private pollHandle: any;
  columns: string[] = [];
  filters: any[] = [];
  exportColumns: any = {};

  // --- Onglets Contacts / Services (nettoyage) ---
  resumeNettoyage: any;
  progressionNettoyage: any;
  // Compteurs d'inutilisés : dénombrement coûteux, chargé à l'ouverture de l'onglet concerné
  // (undefined = pas encore chargé, d'où l'affichage « … » côté template)
  nbContactsInutilises: number | undefined;
  nbServicesInutilises: number | undefined;
  private ongletsCharges = new Set<string>();
  // Confirmation de vérification propre à chaque onglet (une liste vérifiée ne vaut pas pour l'autre)
  nettoyageVerifieContacts: boolean = false;
  nettoyageVerifieServices: boolean = false;
  annulationNettoyageDemandee: boolean = false;
  private pollHandleNettoyage: any;
  contactsColumns: string[] = [];
  contactsFilters: any[] = [];
  contactsExportColumns: any = {};
  servicesColumns: string[] = [];
  servicesFilters: any[] = [];
  servicesExportColumns: any = {};

  @ViewChild('tableConventions') tableConventions: TableComponent | undefined;
  @ViewChild('tableContacts') tableContacts: TableComponent | undefined;
  @ViewChild('tableServices') tableServices: TableComponent | undefined;

  constructor(
    public archivageService: ArchivageService,
    public nettoyageService: NettoyageService,
    public nettoyageContactsService: NettoyageContactsService,
    public nettoyageServicesService: NettoyageServicesService,
    private messageService: MessageService,
    private authService: AuthService,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.columns = ['id', 'etudiant.nom', 'etudiant.prenom', 'structure.raisonSociale', 'annee', 'dateFinStage', 'gratification', 'dateArchivage'];
    this.filters = [
      { id: 'simulation', type: 'text', value: this.typeSimulation, specific: true, hidden: true, permanent: true },
      { id: 'id', libelle: 'N° de la convention', type: 'int' },
      { id: 'etudiant', libelle: 'Étudiant', specific: true },
      { id: 'structure', libelle: 'Établissement d\'accueil', specific: true },
      { id: 'annee', libelle: 'Année universitaire' },
      { id: 'gratification', libelle: 'Gratification', type: 'boolean', specific: true },
    ];
    this.exportColumns = {
      singleExcelSheet: [{
        title: 'Simulation archivage',
        columns: {
          id: { title: 'N° de la convention' },
          etudiantNom: { title: 'Nom de l\'étudiant' },
          etudiantPrenom: { title: 'Prénom de l\'étudiant' },
          structure: { title: 'Établissement d\'accueil' },
          annee: { title: 'Année universitaire' },
          dateDebutStage: { title: 'Date de début du stage' },
          dateFinStage: { title: 'Date de fin du stage' },
          gratificationStage: { title: 'Gratification' },
          dateArchivage: { title: 'Date d\'archivage' },
        }
      }]
    };

    this.contactsColumns = ['id', 'nom', 'prenom', 'mail', 'fonction', 'service', 'structure'];
    this.contactsFilters = [
      { id: 'nom', libelle: 'Nom' },
      { id: 'prenom', libelle: 'Prénom' },
      { id: 'mail', libelle: 'Mail' },
      { id: 'service.structure.raisonSociale', libelle: 'Établissement d\'accueil' },
    ];
    this.contactsExportColumns = {
      nom: { title: 'Nom' }, prenom: { title: 'Prénom' }, mail: { title: 'Mail' }, tel: { title: 'Téléphone' },
      fonction: { title: 'Fonction' }, service: { title: 'Service' }, structure: { title: 'Établissement d\'accueil' },
      loginCreation: { title: 'Login création' }, dateCreation: { title: 'Date de création' },
    };

    this.servicesColumns = ['id', 'nom', 'voie', 'codePostal', 'commune', 'structure'];
    this.servicesFilters = [
      { id: 'nom', libelle: 'Nom' },
      { id: 'commune', libelle: 'Commune' },
      { id: 'structure.raisonSociale', libelle: 'Établissement d\'accueil' },
    ];
    this.servicesExportColumns = {
      nom: { title: 'Nom' }, voie: { title: 'Voie' }, codePostal: { title: 'Code postal' }, commune: { title: 'Commune' },
      structure: { title: 'Établissement d\'accueil' }, loginCreation: { title: 'Login création' }, dateCreation: { title: 'Date de création' },
    };

    this.refresh();
    this.refreshNettoyage();
    // Raccroche le suivi d'un traitement déjà en cours (lancé par un autre admin ou le cron manuel)
    this.archivageService.getProgression().subscribe((p: any) => {
      if (p?.enCours) { this.progression = p; this.startPolling(); }
    });
    this.nettoyageService.getProgression().subscribe((p: any) => {
      if (p?.enCours) { this.progressionNettoyage = p; this.startPollingNettoyage(); }
    });
  }

  ngOnDestroy(): void {
    this.stopPolling();
    this.stopPollingNettoyage();
  }

  canEdit(): boolean {
    return this.authService.checkRights({ fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.MODIFICATION] });
  }

  pourcentageDe(progression: any): number {
    if (!progression || !progression.total) {
      return 0;
    }
    return Math.min(100, Math.round(progression.traitees * 100 / progression.total));
  }

  goToConvention(row: any): void {
    if (row?.id) {
      this.router.navigate([`/conventions/${row.id}`]);
    }
  }

  // ------------------------------------------------------------------
  // Onglet Conventions : archivage / purge
  // ------------------------------------------------------------------

  refresh(): void {
    this.archivageService.getStatistiques().subscribe((r: any) => this.statistiques = r);
    this.archivageService.getSimulationResume().subscribe((r: any) => this.resume = r);
  }

  changeTypeSimulation(type: string): void {
    if (this.typeSimulation === type) {
      return;
    }
    this.typeSimulation = type;
    this.purgeVerifiee = false;
    const filtreSimulation = this.filters.find((f: any) => f.id === 'simulation');
    if (filtreSimulation) { filtreSimulation.value = type; }
    this.tableConventions?.setFilter({ id: 'simulation', type: 'text', value: type, specific: true });
    this.tableConventions?.update();
  }

  /** Nombre d'éléments concernés par la simulation conventions affichée. */
  nbAConcerner(): number {
    if (!this.resume) {
      return 0;
    }
    return this.typeSimulation === 'purge' ? this.resume.conventionsAPurger : this.resume.conventionsAArchiver;
  }

  lancer(type: string): void {
    if (this.progression?.enCours) {
      return;
    }
    if (type === 'purge' && !this.purgeVerifiee) {
      return;
    }
    this.purgeVerifiee = false;
    this.annulationDemandee = false;
    const demarrage = Date.now();
    this.progression = { enCours: true, tache: type === 'purge' ? 'Purge' : 'Archivage', etape: 'Démarrage', traitees: 0, total: 0 };
    this.archivageService.executer(type).subscribe({
      next: (p: any) => {
        const attente = Math.max(0, 800 - (Date.now() - demarrage));
        setTimeout(() => {
          this.progression = p;
          if (p?.enCours) { this.startPolling(); } else { this.finDeTraitement(p); }
        }, attente);
      },
      error: (err: any) => {
        this.progression = null;
        this.messageService.setError(`Impossible de lancer le traitement : ${err.error?.message || err.message}`);
      }
    });
  }

  annuler(): void {
    if (!this.progression?.enCours || this.annulationDemandee) {
      return;
    }
    this.annulationDemandee = true;
    this.archivageService.annuler().subscribe({
      next: (p: any) => this.progression = p,
      error: (err: any) => {
        this.annulationDemandee = false;
        this.messageService.setError(`Impossible d'annuler : ${err.error?.message || err.message}`);
      }
    });
  }

  exporterRapport(): void {
    const type = this.progression?.tache === 'Purge' ? 'purge' : 'archivage';
    this.archivageService.exportRapport().subscribe({
      next: (blob: Blob) => FileSaver.saveAs(blob, `${type}_conventions_traitees_${Date.now()}.xlsx`),
      error: (err: any) => this.messageService.setError(`Impossible d'exporter le rapport : ${err.error?.message || err.message}`)
    });
  }

  pourcentage(): number {
    return this.pourcentageDe(this.progression);
  }

  private finDeTraitement(p: any): void {
    this.stopPolling();
    this.annulationDemandee = false;
    this.notifierFin(p);
    this.refresh();
    this.tableConventions?.update();
  }

  private startPolling(): void {
    this.stopPolling();
    this.pollHandle = setInterval(() => {
      this.archivageService.getProgression().subscribe((p: any) => {
        this.progression = p;
        if (!p?.enCours) { this.finDeTraitement(p); }
      });
    }, 2000);
  }

  private stopPolling(): void {
    if (this.pollHandle) { clearInterval(this.pollHandle); this.pollHandle = null; }
  }

  // ------------------------------------------------------------------
  // Onglets Contacts / Services : nettoyage des inutilisés
  // ------------------------------------------------------------------

  refreshNettoyage(): void {
    this.nettoyageService.getResume().subscribe((r: any) => this.resumeNettoyage = r);
    // Ne recharge que les compteurs déjà consultés (les autres le seront à l'ouverture de l'onglet)
    this.ongletsCharges.forEach(type => this.chargerCompteur(type));
  }

  /** Nombre de contacts (ou services) que le nettoyage supprimerait (0 tant que non chargé). */
  nbInutilises(type: string): number {
    const valeur = type === 'services' ? this.nbServicesInutilises : this.nbContactsInutilises;
    return valeur ?? 0;
  }

  /** Le compteur de cet onglet est-il déjà connu ? (sinon on affiche un indicateur de chargement) */
  compteurCharge(type: string): boolean {
    return (type === 'services' ? this.nbServicesInutilises : this.nbContactsInutilises) !== undefined;
  }

  /**
   * Chargement paresseux : les données lourdes d'un onglet (dénombrement des inutilisés) ne
   * sont demandées qu'à sa première ouverture, pour ne pas pénaliser l'affichage de la page.
   */
  onTabChange(index: number): void {
    const type = index === 1 ? 'contacts' : (index === 2 ? 'services' : null);
    if (!type || this.ongletsCharges.has(type)) {
      return;
    }
    this.ongletsCharges.add(type);
    this.chargerCompteur(type);
  }

  private chargerCompteur(type: string): void {
    this.nettoyageService.getNombreInutilises(type).subscribe({
      next: (nb: number) => {
        if (type === 'services') {
          this.nbServicesInutilises = nb;
        } else {
          this.nbContactsInutilises = nb;
        }
      },
      error: () => {
        // Compteur indisponible : on n'empêche pas la consultation du tableau pour autant
        if (type === 'services') {
          this.nbServicesInutilises = 0;
        } else {
          this.nbContactsInutilises = 0;
        }
      }
    });
  }

  /** Le nettoyage en cours porte-t-il sur ce type ? (un seul nettoyage à la fois) */
  nettoyageEnCoursPour(type: string): boolean {
    if (!this.progressionNettoyage?.enCours) {
      return false;
    }
    const attendu = type === 'services' ? 'Nettoyage des services' : 'Nettoyage des contacts';
    return this.progressionNettoyage.tache === attendu;
  }

  /** Le bilan affiché concerne-t-il ce type ? */
  bilanNettoyagePour(type: string): boolean {
    if (this.progressionNettoyage?.enCours || !this.progressionNettoyage?.message) {
      return false;
    }
    const attendu = type === 'services' ? 'Nettoyage des services' : 'Nettoyage des contacts';
    return this.progressionNettoyage.tache === attendu;
  }

  estVerifie(type: string): boolean {
    return type === 'services' ? this.nettoyageVerifieServices : this.nettoyageVerifieContacts;
  }

  lancerNettoyage(type: string): void {
    if (this.progressionNettoyage?.enCours || !this.estVerifie(type)) {
      return;
    }
    this.nettoyageVerifieContacts = false;
    this.nettoyageVerifieServices = false;
    this.annulationNettoyageDemandee = false;
    const demarrage = Date.now();
    this.progressionNettoyage = { enCours: true, tache: type === 'services' ? 'Nettoyage des services' : 'Nettoyage des contacts', etape: 'Démarrage', traitees: 0, total: 0 };
    this.nettoyageService.executer(type).subscribe({
      next: (p: any) => {
        const attente = Math.max(0, 800 - (Date.now() - demarrage));
        setTimeout(() => {
          this.progressionNettoyage = p;
          if (p?.enCours) { this.startPollingNettoyage(); } else { this.finDeTraitementNettoyage(p); }
        }, attente);
      },
      error: (err: any) => {
        this.progressionNettoyage = null;
        this.messageService.setError(`Impossible de lancer le nettoyage : ${err.error?.message || err.message}`);
      }
    });
  }

  annulerNettoyage(): void {
    if (!this.progressionNettoyage?.enCours || this.annulationNettoyageDemandee) {
      return;
    }
    this.annulationNettoyageDemandee = true;
    this.nettoyageService.annuler().subscribe({
      next: (p: any) => this.progressionNettoyage = p,
      error: (err: any) => {
        this.annulationNettoyageDemandee = false;
        this.messageService.setError(`Impossible d'annuler : ${err.error?.message || err.message}`);
      }
    });
  }

  exporterRapportNettoyage(): void {
    const type = this.progressionNettoyage?.tache === 'Nettoyage des services' ? 'services' : 'contacts';
    this.nettoyageService.exportRapport().subscribe({
      next: (blob: Blob) => FileSaver.saveAs(blob, `nettoyage_${type}_${Date.now()}.xlsx`),
      error: (err: any) => this.messageService.setError(`Impossible d'exporter le rapport : ${err.error?.message || err.message}`)
    });
  }

  pourcentageNettoyage(): number {
    return this.pourcentageDe(this.progressionNettoyage);
  }

  private finDeTraitementNettoyage(p: any): void {
    this.stopPollingNettoyage();
    this.annulationNettoyageDemandee = false;
    this.notifierFin(p);
    this.refreshNettoyage();
    this.tableContacts?.update();
    this.tableServices?.update();
  }

  private startPollingNettoyage(): void {
    this.stopPollingNettoyage();
    this.pollHandleNettoyage = setInterval(() => {
      this.nettoyageService.getProgression().subscribe((p: any) => {
        this.progressionNettoyage = p;
        if (!p?.enCours) { this.finDeTraitementNettoyage(p); }
      });
    }, 2000);
  }

  private stopPollingNettoyage(): void {
    if (this.pollHandleNettoyage) { clearInterval(this.pollHandleNettoyage); this.pollHandleNettoyage = null; }
  }

  private notifierFin(p: any): void {
    if (p?.erreur) {
      this.messageService.setError(p.message || 'Le traitement a échoué');
    } else if (p?.annule) {
      this.messageService.setWarning(p.message || 'Traitement interrompu');
    } else if (p?.message) {
      this.messageService.setSuccess(p.message);
    }
  }
}
