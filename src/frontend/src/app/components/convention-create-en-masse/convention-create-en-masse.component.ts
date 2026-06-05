import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { GroupeEtudiantService } from "../../services/groupe-etudiant.service";
import { UfrService } from "../../services/ufr.service";
import { EtapeService } from "../../services/etape.service";
import { forkJoin } from 'rxjs';
import { ConventionService } from "../../services/convention.service";
import { MatTabChangeEvent } from "@angular/material/tabs";
import { TitleService } from "../../services/title.service";
import { AuthService } from "../../services/auth.service";

@Component({
  selector: 'app-convention-create-en-masse',
  templateUrl: './convention-create-en-masse.component.html',
  styleUrls: ['./convention-create-en-masse.component.scss']
})
export class ConventionCreateEnMasseComponent implements OnInit {

  conventionTabIndex: number = 0;

  sharedData: any = {};
  groupeEtudiant: any;
  allValid = false;
  isModif = false;

  tabs: {[tab:number]:{statut:number,init:boolean}} = {
    0: { statut: 0, init: false },
    1: { statut: 0, init: false },
    2: { statut: 2, init: false },
    3: { statut: 0, init: false },
    4: { statut: 0, init: false },
    5: { statut: 0, init: false },
    6: { statut: 0, init: false },
    7: { statut: 0, init: false },
  }

  constructor(private activatedRoute: ActivatedRoute,
              public groupeEtudiantService: GroupeEtudiantService,
              private conventionService: ConventionService,
              private ufrService: UfrService,
              private etapeService: EtapeService,
              private titleService: TitleService,
              private authService: AuthService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.sharedData.columns = ['select','numEtudiant','nom', 'prenom', 'mail', 'ufr.libelle', 'etape.libelle', 'annee'];
    this.sharedData.filters = [
      { id: 'etudiant.nom', libelle: 'Nom'},
      { id: 'etudiant.prenom', libelle: 'Prénom'},
      { id: 'etudiant.numEtudiant', libelle: 'N° étudiant'},
      { id: 'convention.annee', libelle: 'Année', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'libelle', value: [] },
      { id: 'ufr.id', libelle: 'Composante', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true },
      { id: 'etape.id', libelle: 'Étape', type: 'autocomplete', autocompleteService: this.etapeService, options: [], keyLibelle: 'libelle', keyId: 'id', value: [], specific: true, colSpan: 9 },
    ];
    this.sharedData.filters.push({ id: 'groupeEtudiant.id', type: 'int', value: 0, hidden: true, permanent: true });

    this.activatedRoute.params.subscribe((param: any) => {
      const pathId = param.id;
      if (pathId === 'create') {
        this.titleService.title = 'Création de conventions en masse';
        // Récupération du groupeEtudiant au mode brouillon
        this.groupeEtudiantService.getBrouillon().subscribe((response: any) => {
          this.groupeEtudiant = response;
          this.majFilter();
          this.majStatus();
        });
      } else {
        this.titleService.title = 'Modification de conventions en masse';
        // Récupération du groupeEtudiant correspondant à l'id
        this.groupeEtudiantService.getById(pathId).subscribe((response: any) => {
          this.groupeEtudiant = response;
          this.majFilter();
          this.majStatus();
        });
      }
    });

    forkJoin(
      this.ufrService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
      this.etapeService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
      this.conventionService.getListAnnee(),
    ).subscribe(([ufrData, etapeData, listAnneeData]) => {
      let filter = this.sharedData.filters.find((f: any) => f.id === 'ufr.id');
      if (filter) filter.options = ufrData.data;
      filter = this.sharedData.filters.find((f: any) => f.id === 'etape.id');
      if (filter) filter.options = etapeData.data;
      filter = this.sharedData.filters.find((f: any) => f.id === 'convention.annee');
      if (filter) filter.options = listAnneeData;
    });

    this.activatedRoute.params.subscribe((param: any) => {
      const pathId = param.id;
      if (pathId != 'create') {
        this.isModif = true;
      }
    });
  }

  majStatus(): void {
    if (this.groupeEtudiant){
      this.setStatus(0,2);
    }else{
      this.setStatus(0,0);
    }
    if (this.groupeEtudiant && this.groupeEtudiant.infosStageValid){
      this.setStatus(1,2);
    }else{
      this.setStatus(1,0);
    }
    if (this.groupeEtudiant){
      this.setStatus(3,2*this.percentValidStructure()/100);
    }else{
      this.setStatus(3,0);
    }
    if (this.groupeEtudiant){
      this.setStatus(4,2*this.percentValidService()/100);
    }else{
      this.setStatus(4,0);
    }
    if (this.groupeEtudiant){
      this.setStatus(5,2*this.percentValidContact()/100);
    }else{
      this.setStatus(5,0);
    }
    if (this.groupeEtudiant){
      this.setStatus(6,2*this.percentValidEnseignant()/100);
    }else{
      this.setStatus(6,0);
    }
    if (this.groupeEtudiant) {
      this.setStatus(7, 2*this.percentValidSignataire()/100);
    } else {
      this.setStatus(7, 0);
    }
  }

  setStatus(key: number, value: number): void {
    this.tabs[key].statut = value;
    this.majAllValid();
  }

  percentValidStructure(): number {
    return this.groupeEtudiant.convention.structure ? 100 : this.percentValidEtudiantsStructure();
  }
  percentValidService(): number {
    return this.groupeEtudiant.convention.service ? 100 : this.percentValidEtudiantsService();
  }
  percentValidContact(): number {
    return this.groupeEtudiant.convention.contact ? 100 : this.percentValidEtudiantsContact();
  }
  percentValidEnseignant(): number {
    return this.groupeEtudiant.convention.enseignant ? 100 : this.percentValidEtudiantsEnseignant();
  }
  percentValidEtudiantsStructure(): number {
    let count = 0;
    for (let etudiant of this.groupeEtudiant.etudiantGroupeEtudiants) {
      if (etudiant.convention.structure)
        count++;
    }
    return 100 * count / this.groupeEtudiant.etudiantGroupeEtudiants.length;
  }
  percentValidEtudiantsService(): number {
    let count = 0;
    for (let etudiant of this.groupeEtudiant.etudiantGroupeEtudiants)
      if (etudiant.convention.service)
        count++;
    return 100 * count / this.groupeEtudiant.etudiantGroupeEtudiants.length;
  }
  percentValidEtudiantsContact(): number {
    let count = 0;
    for (let etudiant of this.groupeEtudiant.etudiantGroupeEtudiants) {
      if (etudiant.convention.contact)
        count++;
    }
    return 100 * count / this.groupeEtudiant.etudiantGroupeEtudiants.length;
  }
  percentValidEtudiantsEnseignant(): number {
    let count = 0;
    for (let etudiant of this.groupeEtudiant.etudiantGroupeEtudiants) {
      if (etudiant.convention.enseignant)
        count++;
    }
    return 100 * count / this.groupeEtudiant.etudiantGroupeEtudiants.length;
  }

  majAllValid(): void {
    this.allValid = true;
    for (let key in this.tabs) {
        if (this.tabs[key].statut !== 2) {
          this.allValid = false;
        }
    }
  }

  percentValidSignataire(): number {
    return this.groupeEtudiant.convention.signataire ? 100 : this.percentValidEtudiantsSignataire();
  }

  percentValidEtudiantsSignataire(): number {
    let count = 0;
    for (let etudiant of this.groupeEtudiant.etudiantGroupeEtudiants) {
      if (etudiant.convention.signataire) count++;
    }
    return 100 * count / this.groupeEtudiant.etudiantGroupeEtudiants.length;
  }

  tabChanged(event: MatTabChangeEvent): void {
    if(this.tabs[event.index])
      this.tabs[event.index].init = true;
  }

  /** @param statut la progression entre 0 et 2
   * @return 33 si statut vide sinon un pourcentage équivalent */
  progressValue(statut: number|null): number {
    return statut ? 100 * statut / 2 : 33;
  }
  /** @param statut la progression entre 0 et 2 */
  progressClass(statut: number|null): {[cssClass:string]:boolean} {
    if (statut === 2) return {done: true};
    return statut ? {editing: true} : {empty: true};
  }

  majFilter(): void {
    if (this.groupeEtudiant) {
      let filter = this.sharedData.filters.find((f: any) => f.id === 'groupeEtudiant.id');
      if (filter) filter.value = this.groupeEtudiant.id;
    }
    this.tabs[0].init = true;
  }

  updateGroupeEtudiant(data: any): void {
    this.groupeEtudiant = data;
    this.majFilter();
    this.majStatus();
  }
}
