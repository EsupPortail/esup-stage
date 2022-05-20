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

@Component({
  selector: 'app-eval-stage',
  templateUrl: './eval-stage.component.html',
  styleUrls: ['./eval-stage.component.scss']
})
export class EvalStageComponent implements OnInit {

  columns: string[] = [];
  sortColumn = 'id';
  sortDirection: SortDirection = 'desc';
  filters: any[] = [];

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
    private messageService: MessageService,
    private configService: ConfigService,
  ) {
  }

  ngOnInit(): void {
    this.isEtudiant = this.authService.isEtudiant();
    this.isEnseignant= this.authService.isEnseignant();
    this.isGestionnaireOrAdmin = this.authService.isGestionnaire() || this.authService.isAdmin() ;
    const login = this.authService.getUserConnectedLogin();


    this.filters = [];

    if(this.isGestionnaireOrAdmin){
      this.columns = ['id', 'etudiant.prenom', 'structure.raisonSociale', 'dateDebutStage', 'dateFinStage', 'ufr.libelle',
     'etape.libelle', 'annee','reponseEvaluationEtudiant','reponseEvaluationEnseignant','reponseEvaluationEntreprise', 'action'];

      this.filters.push({ id: 'annee', libelle: 'Année', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'libelle', value: [] });
      this.filters.push({ id: 'centreGestion.nomCentre', libelle: 'Centre de gestion'},);
      this.filters.push({ id: 'stageTermine', libelle: 'N\'afficher que les stages terminés ?', type: 'boolean', specific: true });

      this.conventionService.getListAnnee().subscribe(response => {
        this.annees = response;
        this.anneeEnCours = this.annees.find((a: any) => { return a.anneeEnCours === true });
        this.appTable?.setFilterOption('annee', this.annees);
        this.appTable?.setFilterValue('annee', [this.anneeEnCours.libelle]);
        this.appTable?.update();
      });
    } else if (this.isEtudiant){
      this.columns = ['id', 'structure.raisonSociale', 'dateDebutStage', 'dateFinStage', 'ufr.libelle',
     'etape.libelle', 'annee','reponseEvaluationEtudiant', 'action'];

      this.filters.push({ id: 'etudiant.identEtudiant', type: 'string', value: login, hidden: true, permanent: true });
    } else if (this.isEnseignant){
      this.columns = ['id', 'etudiant.prenom', 'structure.raisonSociale', 'dateDebutStage', 'dateFinStage', 'ufr.libelle',
     'etape.libelle', 'annee','reponseEvaluationEnseignant', 'action'];

      this.filters.push({ id: 'enseignant.uidEnseignant', type: 'string', value: login, hidden: true, permanent: true });
    }
  }

  goToConvention(id: number): void {
    this.conventionService.setGoToOnglet(8)
    this.router.navigate([`/conventions/${id}`], )
  }

}
