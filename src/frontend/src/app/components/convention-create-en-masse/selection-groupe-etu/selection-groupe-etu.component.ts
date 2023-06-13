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
import { ReplaySubject, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ConfigService } from "../../../services/config.service";
import { debounceTime } from "rxjs/operators";
import { SortDirection } from "@angular/material/sort";
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";

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
  filterChanged: any = new Subject();

  etapeFilterCtrl: FormControl = new FormControl();
  filteredEtapes: ReplaySubject<any> = new ReplaySubject<any>(1);

  _onDestroy = new Subject<void>();

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
      this.ufrService.getApogeeComposantes(),
      this.etapeService.getApogeeEtapes(),
      this.conventionService.getListAnnee(),
    ).subscribe(([ufrData, etapeData, listAnneeData]) => {
      // ufr
      this.ufrList = ufrData;
      // etape
      this.etapeList = etapeData;

      this.filteredEtapes.next(this.etapeList.slice());
      this.etapeFilterCtrl.valueChanges
        .pipe(takeUntil(this._onDestroy))
        .subscribe(() => {
          this.filterEtapes();
        });
      // annees
      this.annees = listAnneeData;
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
      this.etudiants = [];
      this.selectedAdd = [];
      return;
    }
    const data = {...this.formAddEtudiants.value};
    data.supannEtuEtape = data.etape;
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

  resetFilters(): void {
    this.formAddEtudiants.reset();
  }

  filterOnChange(value: string): void {
    this.filterChanged.next(value);
  }

  getEtapeLibelle(ldapEtape: string): string {
    let result = this.etapeList.find((x: any) => ldapEtape.includes(x.code));
    if (result)
      return result.libelle;
    return ldapEtape;
  }

  getUfrLibelle(codeUfr: string): string {
    let result = this.ufrList.find((x: any) => x.code === codeUfr);
    if (result)
      return result.libelle;
    return codeUfr;
  }

  compareCode(option: any, value: any): boolean {
    if (option && value) {
      return option.code === value.code;
    }
    return false;
  }

  filterEtapes() {
    if (!this.etapeList) {
      return;
    }

    let search = this.etapeFilterCtrl.value;
    if (!search) {
      this.filteredEtapes.next(this.etapeList.slice());
      return;
    } else {
      search = search.toLowerCase();
    }

    this.filteredEtapes.next(
      this.etapeList.filter(etape => etape.code.toLowerCase().indexOf(search) > -1 || etape.libelle.toLowerCase().indexOf(search) > -1)
    );
  }

  etapesChange(etape: any, selected: any) {
    if (selected) {
    } else {
    }
  }
}
