import { Component, EventEmitter, Input, Output, OnChanges, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { UfrService } from "../../../services/ufr.service";
import { EtapeService } from "../../../services/etape.service";
import { ConventionService } from "../../../services/convention.service";
import { MatAutocompleteSelectedEvent } from "@angular/material/autocomplete";
import { EtudiantGroupeEtudiantService } from "../../../services/etudiant-groupe-etudiant.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import { LdapService } from "../../../services/ldap.service";
import { EtudiantService } from "../../../services/etudiant.service";
import { MessageService } from "../../../services/message.service";
import { forkJoin } from 'rxjs';
import { Subject } from "rxjs";
import { ConfigService } from "../../../services/config.service";
import { debounceTime } from "rxjs/operators";
import { SortDirection } from "@angular/material/sort";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";

@Component({
  selector: 'app-selection-groupe-etu',
  templateUrl: './selection-groupe-etu.component.html',
  styleUrls: ['./selection-groupe-etu.component.scss']
})
export class SelectionGroupeEtuComponent implements OnInit {

  groupeEtudiantColumns: string[] = [];
  groupeEtudiantFilters: string[] = [];
  columns: string[] = [];
  sortColumn = 'prenom';
  sortDirection: SortDirection = 'desc';
  selectedRemove: any[] = [];
  selectedAdd: any[] = [];
  etudiants: any[] = [];
  annees: any[] = [];
  ufrList: any[] = [];
  etapeList: any[] = [];
  etapeFilterValue: string = '';
  autocompleteData: any = [];
  autocompleteChanged: any = new Subject();
  filterChanged: any = new Subject();

  form: FormGroup;
  formAddEtudiants: FormGroup;

  @Input() sharedData: any;
  @Input() groupeEtudiant: any;
  @Output() validated = new EventEmitter<any>();

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public groupeEtudiantService: GroupeEtudiantService,
    public etudiantGroupeEtudiantService: EtudiantGroupeEtudiantService,
    public etudiantService: EtudiantService,
    private authService: AuthService,
    private ldapService: LdapService,
    private ufrService: UfrService,
    private etapeService: EtapeService,
    private conventionService: ConventionService,
    private router: Router,
    private messageService: MessageService,
    private configService: ConfigService,
    private fb: FormBuilder,
  ) {
    this.form = this.fb.group({
      codeGroupe: [null, [Validators.required, Validators.maxLength(100)]],
      nomGroupe: [null, [Validators.required, Validators.maxLength(100)]],
    });
    this.formAddEtudiants = this.fb.group({
      codEtu: [null, []],
      nom: [null, []],
      prenom: [null, []],
      etape: [null, []],
      composante: [null, []],
      annee: [null, []],
    });
  }

  ngOnInit(): void {
    this.groupeEtudiantColumns = [...this.sharedData.columns];
    this.groupeEtudiantFilters = [...this.sharedData.filters];
    this.columns =  [...this.sharedData.columns];

    forkJoin(
      this.ufrService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
      this.etapeService.getPaginated(1, 0, 'libelle', 'asc', '{}'),
      this.conventionService.getListAnnee(),
    ).subscribe(([ufrData, etapeData, listAnneeData]) => {
      // ufr
      this.ufrList = ufrData.data;
      // etape
      this.etapeList = etapeData.data;
      // annees
      this.annees = listAnneeData;
    });

    this.autocompleteChanged.pipe(debounceTime(1000)).subscribe(async (event: any) => {
      this.autocompleteData = await this.etapeService.getAutocompleteData(event).toPromise();
      this.autocompleteData = this.autocompleteData.data;
      if(event === ''){
        this.etapeFilterValue = '';
        this.search();
      }
    });

    this.filterChanged.pipe(debounceTime(1000)).subscribe((event: any) => {
      this.search();
    });
  }

  ngOnChanges(): void{
    this.form.setValue({
      codeGroupe: this.groupeEtudiant?this.groupeEtudiant.code:null,
      nomGroupe: this.groupeEtudiant?this.groupeEtudiant.nom:null,
    });
    this.appTable?.update();
    this.selectedAdd = [];
    this.selectedRemove = [];
  }

  search(): void {
    if (!this.formAddEtudiants.get('codEtu')?.value && !this.formAddEtudiants.get('nom')?.value && !this.formAddEtudiants.get('prenom')?.value
    && !this.formAddEtudiants.get('etape')?.value && !this.formAddEtudiants.get('composante')?.value && !this.formAddEtudiants.get('annee')?.value) {
      this.messageService.setError(`Veuillez renseigner au moins l'un des critères`);
      return;
    }
    const data = {...this.formAddEtudiants.value};
    data.supannEtuEtape = this.etapeFilterValue;
    data.supannEntiteAffectation = data.composante;
    data.supannEtuAnneeInscription = data.annee;

    this.ldapService.searchEtudiants(data).subscribe((response: any) => {
      this.etudiants = response;
      this.selectedAdd = [];
    });
  }

  isSelectedRemove(data: any): boolean {
    return this.selectedRemove.find((r: any) => {return r.id === data.id}) !== undefined;
  }
  toggleSelectedRemove(data: any): void {
    const index = this.selectedRemove.findIndex((r: any) => {return r.id === data.id});
    if (index > -1) {
      this.selectedRemove.splice(index, 1);
    } else {
      this.selectedRemove.push(data);
    }
  }
  masterToggleRemove(): void {
    if (this.isAllSelectedRemove()) {
      this.selectedRemove = [];
      return;
    }
    this.appTable?.data.forEach((d: any) => {
      const index = this.selectedRemove.findIndex((s: any) => s.id === d.id);
      if (index === -1) {
        this.selectedRemove.push(d);
      }
    });
  }
  isAllSelectedRemove(): boolean {
    let allSelectedRemove = true;
    if(this.appTable?.data){
      this.appTable?.data.forEach((data: any) => {
        const index = this.selectedRemove.findIndex((r: any) => {return r.id === data.id});
        if (index === -1) {
           allSelectedRemove = false;
        }
      });
    }
    return allSelectedRemove;
  }

  isSelectedAdd(data: any): boolean {
    return this.selectedAdd.find((r: any) => {return r.codEtu === data.codEtu}) !== undefined;
  }
  toggleSelectedAdd(data: any): void {
    const index = this.selectedAdd.findIndex((r: any) => {return r.codEtu === data.codEtu});
    if (index > -1) {
      this.selectedAdd.splice(index, 1);
    } else {
      this.selectedAdd.push(data);
    }
  }
  masterToggleAdd(): void {
    if (this.isAllSelectedAdd()) {
      this.selectedAdd = [];
      return;
    }
    this.etudiants.forEach((d: any) => {
      const index = this.selectedAdd.findIndex((s: any) => s.codEtu === d.codEtu);
      if (index === -1) {
        this.selectedAdd.push(d);
      }
    });
  }
  isAllSelectedAdd(): boolean {
    let allSelectedAdd = true;
    if(this.etudiants){
      this.etudiants.forEach((data: any) => {
        const index = this.selectedAdd.findIndex((r: any) => {return r.codEtu === data.codEtu});
        if (index === -1) {
           allSelectedAdd = false;
        }
      });
    }
    return allSelectedAdd;
  }

  validate(): void {
    if (this.form.valid) {
      const selectedRemove = this.selectedRemove.map((s: any) => s.id);

      let data = {...this.form.value};
      data.etudiantRemovedIds = selectedRemove;
      data.etudiantAdded = this.selectedAdd;

      if (!this.groupeEtudiant) {
        this.groupeEtudiantService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Groupe créé avec succès');
          this.validated.emit(response);
        });
      } else {
        this.groupeEtudiantService.update(this.groupeEtudiant.id, data).subscribe((response: any) => {
          this.messageService.setSuccess('Groupe modifié avec succès');
          this.validated.emit(response);
        });
      }
    }
  }

  filterOnChange(value: string): void {
    this.filterChanged.next(value);
  }

  searchAutocomplete(value: string): void {
    this.autocompleteChanged.next(value);
  }

  autocompleteSelected(event: MatAutocompleteSelectedEvent): void {
    this.formAddEtudiants.get('etape')?.setValue(event.option.value.libelle, { emitEvent: false});
    this.etapeFilterValue = '{UAI:' + event.option.value.id.codeUniversite + '}' + event.option.value.id.code + '-' + event.option.value.id.codeVersionEtape;
    this.search();
  }

}
