import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../table/table.component";
import { ConventionService } from "../../services/convention.service";
import { AuthService } from "../../services/auth.service";
import { Router } from "@angular/router";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  columns: string[] = [];
  sortColumn = 'id';
  filters: any[] = [];

  nbConventionsEnAttente: number|undefined;
  anneeEnCours: string|undefined;
  annees: any[] = [];

  selected: any[] = [];

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public conventionService: ConventionService,
    private authService: AuthService,
    private router: Router,
  ) {
  }

  ngOnInit(): void {
    if (this.isGestionnaire()) {
      this.setDataGestionnaire();
    } else if (this.isEnseignant()) {
      this.columns = ['id', 'etudiant', 'ufr', 'etape',  'dateDebutStage', 'dateFinStage', 'structure', 'sujetStage', 'adresseEtabRef', 'etatValidation', 'avenant', 'action'];
      this.filters = [
        { id: 'id', libelle: 'N° de la convention', type: 'int' },
        { id: 'etudiant', libelle: 'Étudiant', specific: true },
        { id: 'ufr', libelle: 'Composnante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
        { id: 'etape', libelle: 'Étape', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
        { id: 'dateDebutStage', libelle: 'Date début du stage', type: 'date-min' },
        { id: 'dateFinStage', libelle: 'Date fin du stage', type: 'date-max' },
        { id: 'structure', libelle: 'Établissement d\'accueil', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
        { id: 'sujetStage', libelle: 'Sujet du stage' },
        { id: 'adresseEtabRef', libelle: 'Lieu du stage' },
        { id: 'etatValidation', libelle: 'État de validation de la convention', specific: true },
        { id: 'avenant', libelle: 'Avenant', specific: true },
      ];
    } else if (this.isEtudiant()) {
      this.columns = ['id', 'structure', 'dateDebutStage', 'dateFinStage', 'ufr', 'etape', 'enseignant', 'validationPedagogique', 'validationConvention', 'avenant', 'annee', 'action'];
      this.filters = [
        { id: 'id', libelle: 'N° de la convention', type: 'int' },
        { id: 'structure', libelle: 'Établissement d\'accueil', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
        { id: 'dateDebutStage', libelle: 'Date début du stage', type: 'date' },
        { id: 'dateFinStage', libelle: 'Date fin du stage', type: 'date' },
        { id: 'ufr', libelle: 'Composnante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
        { id: 'etape', libelle: 'Étape', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
        { id: 'enseignant', libelle: 'Enseignant', specific: true },
        { id: 'validationPedagogique', libelle: 'Validation pédagogique', type: 'boolean' },
        { id: 'validationConvention', libelle: 'Validation pédagogique', type: 'boolean' },
        { id: 'avenant', libelle: 'Avenant', specific: true },
        { id: 'annee', libelle: 'Année', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
      ];
    } else {
      this.setDataGestionnaire();
    }
    this.filters.push({ id: 'validationCreation', type: 'boolean', value: true, hidden: true });

    this.conventionService.getListAnnee().subscribe((response: any) => {
      this.annees = response;
      this.anneeEnCours = this.annees.find((a: any) => { return a.anneeEnCours === true }).annee;
      this.changeAnnee();
    });
  }

  setDataGestionnaire(): void {
    this.columns = ['select', 'id', 'etudiant', 'structure', 'dateDebutStage', 'dateFinStage', 'ufr', 'etape', 'enseignant', 'action'];
    this.filters = [
      { id: 'id', libelle: 'N° de la convention', type: 'int' },
      { id: 'etudiant', libelle: 'Étudiant', specific: true },
      { id: 'structure', libelle: 'Établissement d\'accueil', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
      { id: 'dateDebutStage', libelle: 'Date début du stage', type: 'date' },
      { id: 'dateFinStage', libelle: 'Date fin du stage', type: 'date' },
      { id: 'ufr', libelle: 'Composnante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
      { id: 'etape', libelle: 'Étape', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [] },
      { id: 'enseignant', libelle: 'Enseignant', specific: true },
    ];
  }

  isEtudiant(): boolean {
    return this.authService.isEtudiant();
  }

  isGestionnaire(): boolean {
    return this.authService.isGestionnaire();
  }

  isEnseignant(): boolean {
    return this.authService.isEnseignant();
  }

  changeAnnee(): void {
    this.countConvention();
    this.appTable?.setFilter({id: 'annee', type: 'text', value: this.anneeEnCours, specific: false});
    this.appTable?.update();
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
    // TODO
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
    if ((this.isGestionnaire() || this.isEnseignant()) && this.anneeEnCours) {
      this.conventionService.countConventionEnAttente(this.anneeEnCours).subscribe((response: any) => {
        this.nbConventionsEnAttente = response.nbEnAttenteValidPedadogique + response.nbEnAttenteValidAdministratif;
      });
    }
  }

  goToConvention(id: number): void {
    this.router.navigate([`/conventions/${id}`], )
  }
}


