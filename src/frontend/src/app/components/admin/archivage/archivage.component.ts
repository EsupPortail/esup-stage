import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router } from "@angular/router";
import * as FileSaver from 'file-saver';
import { TableComponent } from "../../table/table.component";
import { ArchivageService } from "../../../services/archivage.service";
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
  resume: any;
  typeSimulation: string = 'archivage';
  progression: any;
  // Confirmation explicite que la liste de la simulation de purge a été vérifiée
  purgeVerifiee: boolean = false;
  // Demande d'annulation envoyée, en attente de l'arrêt effectif du traitement
  annulationDemandee: boolean = false;
  private pollHandle: any;

  columns: string[] = [];
  filters: any[] = [];
  exportColumns: any = {};

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public archivageService: ArchivageService,
    private messageService: MessageService,
    private authService: AuthService,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.columns = ['id', 'etudiant.nom', 'etudiant.prenom', 'structure.raisonSociale', 'annee', 'dateFinStage', 'gratification', 'dateArchivage'];
    this.filters = [
      // Type de simulation, piloté par le sélecteur de la page (voir changeTypeSimulation)
      { id: 'simulation', type: 'text', value: this.typeSimulation, specific: true, hidden: true, permanent: true },
      { id: 'id', libelle: 'N° de la convention', type: 'int' },
      { id: 'etudiant', libelle: 'Étudiant', specific: true },
      { id: 'structure', libelle: 'Établissement d\'accueil', specific: true },
      { id: 'annee', libelle: 'Année universitaire' },
      { id: 'gratification', libelle: 'Gratification', type: 'boolean', specific: true },
    ];
    this.exportColumns = {
      singleExcelSheet: [
        {
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
        }
      ]
    };

    this.refresh();
    // Si un traitement est déjà en cours (lancé par un autre admin ou par le cron manuel), on raccroche le suivi
    this.archivageService.getProgression().subscribe((progression: any) => {
      if (progression?.enCours) {
        this.progression = progression;
        this.startPolling();
      }
    });
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  refresh(): void {
    this.archivageService.getStatistiques().subscribe((response: any) => {
      this.statistiques = response;
    });
    this.archivageService.getSimulationResume().subscribe((response: any) => {
      this.resume = response;
    });
  }

  changeTypeSimulation(type: string): void {
    if (this.typeSimulation === type) {
      return;
    }
    this.typeSimulation = type;
    // La confirmation de vérification ne vaut que pour la liste consultée
    this.purgeVerifiee = false;
    // Garde la config du filtre alignée pour que la réinitialisation des filtres conserve le type affiché
    const filtreSimulation = this.filters.find((f: any) => f.id === 'simulation');
    if (filtreSimulation) {
      filtreSimulation.value = type;
    }
    this.appTable?.setFilter({ id: 'simulation', type: 'text', value: type, specific: true });
    this.appTable?.update();
  }

  canEdit(): boolean {
    return this.authService.checkRights({ fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.MODIFICATION] });
  }

  /**
   * La carte de lancement n'apparaît que s'il y a au moins une convention à traiter pour la
   * simulation affichée — ou si un traitement est en cours / vient de se terminer (bilan).
   */
  afficherLancement(): boolean {
    if (this.progression?.enCours || this.progression?.message) {
      return true;
    }
    if (!this.resume) {
      return false;
    }
    return this.typeSimulation === 'purge' ? this.resume.conventionsAPurger > 0 : this.resume.conventionsAArchiver > 0;
  }

  lancer(type: string): void {
    if (this.progression?.enCours) {
      return;
    }
    // La purge exige la confirmation explicite de vérification de la simulation
    if (type === 'purge' && !this.purgeVerifiee) {
      return;
    }
    this.purgeVerifiee = false;
    this.annulationDemandee = false;
    // Affichage immédiat de la barre, même si le traitement se termine très vite
    const demarrage = Date.now();
    this.progression = { enCours: true, tache: type === 'purge' ? 'Purge' : 'Archivage', etape: 'Démarrage', traitees: 0, total: 0 };
    this.archivageService.executer(type).subscribe({
      next: (progression: any) => {
        // On laisse la barre visible au moins un instant avant d'afficher un éventuel bilan immédiat
        const attente = Math.max(0, 800 - (Date.now() - demarrage));
        setTimeout(() => this.appliquerProgression(progression), attente);
      },
      error: (err: any) => {
        this.progression = null;
        this.messageService.setError(`Impossible de lancer le traitement : ${err.error?.message || err.message}`);
      }
    });
  }

  private appliquerProgression(progression: any): void {
    this.progression = progression;
    if (progression?.enCours) {
      this.startPolling();
    } else {
      this.finDeTraitement(progression);
    }
  }

  private finDeTraitement(progression: any): void {
    this.stopPolling();
    this.annulationDemandee = false;
    if (progression?.erreur) {
      this.messageService.setError(progression.message || 'Le traitement a échoué');
    } else if (progression?.annule) {
      this.messageService.setWarning(progression.message || 'Traitement interrompu');
    } else if (progression?.message) {
      this.messageService.setSuccess(progression.message);
    }
    this.refresh();
    this.appTable?.update();
  }

  annuler(): void {
    if (!this.progression?.enCours || this.annulationDemandee) {
      return;
    }
    this.annulationDemandee = true;
    this.archivageService.annuler().subscribe({
      next: (progression: any) => {
        this.progression = progression;
      },
      error: (err: any) => {
        this.annulationDemandee = false;
        this.messageService.setError(`Impossible d'annuler : ${err.error?.message || err.message}`);
      }
    });
  }

  private startPolling(): void {
    this.stopPolling();
    this.pollHandle = setInterval(() => {
      this.archivageService.getProgression().subscribe((progression: any) => {
        this.progression = progression;
        if (!progression?.enCours) {
          this.finDeTraitement(progression);
        }
      });
    }, 2000);
  }

  private stopPolling(): void {
    if (this.pollHandle) {
      clearInterval(this.pollHandle);
      this.pollHandle = null;
    }
  }

  pourcentage(): number {
    if (!this.progression || !this.progression.total) {
      return 0;
    }
    return Math.min(100, Math.round(this.progression.traitees * 100 / this.progression.total));
  }

  goToConvention(row: any): void {
    if (row?.id) {
      this.router.navigate([`/conventions/${row.id}`]);
    }
  }

  exporterRapport(): void {
    const type = this.progression?.tache === 'Purge' ? 'purge' : 'archivage';
    this.archivageService.exportRapport().subscribe({
      next: (blob: Blob) => {
        FileSaver.saveAs(blob, `${type}_conventions_traitees_${Date.now()}.xlsx`);
      },
      error: (err: any) => {
        this.messageService.setError(`Impossible d'exporter le rapport : ${err.error?.message || err.message}`);
      }
    });
  }
}
