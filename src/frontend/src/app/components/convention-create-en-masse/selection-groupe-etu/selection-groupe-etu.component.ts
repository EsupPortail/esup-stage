import {Component, EventEmitter, Input, OnChanges, OnInit, Output, ViewChild} from '@angular/core';
import { TableComponent } from "../../table/table.component";
import {GroupeEtudiant, GroupeEtudiantDto, GroupeEtudiantService} from "../../../services/groupe-etudiant.service";
import { UfrService } from "../../../services/ufr.service";
import {DiplomeEtape, EtapeService, EtapeV2Apogee} from "../../../services/etape.service";
import { ConventionService } from "../../../services/convention.service";
import { EtudiantGroupeEtudiantService } from "../../../services/etudiant-groupe-etudiant.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import { LdapService } from "../../../services/ldap.service";
import { MessageService } from "../../../services/message.service";
import {Observable, ReplaySubject, Subject} from 'rxjs';
import { ConfigService } from "../../../services/config.service";
import { SortDirection } from "@angular/material/sort";
import {FormBuilder, FormControl, FormGroup, ValidationErrors, Validators} from "@angular/forms";
import {
  EtudiantDiplomeEtapeResponse,
  EtudiantDiplomeEtapeSearch,
  EtudiantService
} from '../../../services/etudiant.service';
import {filters} from "css-select";
import {map,startWith} from "rxjs/operators";

@Component({
  selector: 'app-selection-groupe-etu',
  templateUrl: './selection-groupe-etu.component.html',
  styleUrls: ['./selection-groupe-etu.component.scss']
})
export class SelectionGroupeEtuComponent implements OnInit, OnChanges {

  groupeEtudiantColumns: string[] = [];
  groupeEtudiantFilters: string[] = [];
  columns: string[] = [];
  sortColumn = 'prenom';
  sortDirection: SortDirection = 'desc';
  selectedRemove: any[] = [];
  selectedAdd: EtudiantDiplomeEtapeResponse[] = [];
  etudiants: EtudiantDiplomeEtapeResponse[] = [];

  // Années N-1 / N / N+1
  currentYear = (new Date()).getFullYear();
  annees = [
    { label: (this.currentYear - 1) + '/' + (this.currentYear), value: this.currentYear - 1 },
    { label: this.currentYear + '/' + (this.currentYear + 1), value: this.currentYear },
    { label: (this.currentYear + 1) + '/' + (this.currentYear + 2), value: this.currentYear + 1 },
  ];
  ufrList: any[] = [];
  etapeList: DiplomeEtape[] = [];

  etapeFilterCtrl: FormControl = new FormControl();
  filteredEtapes$: Observable<DiplomeEtape[]>;

  _onDestroy = new Subject<void>();

  form: FormGroup;
  formAddEtudiants: FormGroup<{
    codEtu: FormControl<string|null>,
    nom: FormControl<string|null>,
    prenom: FormControl<string|null>,
    etape: FormControl<string|DiplomeEtEtape|null>,
    composante: FormControl<string|null>,
    annee: FormControl<number|null>,
  }>;

  @Input() sharedData: any;
  @Input() groupeEtudiant?: GroupeEtudiant;
  @Output() validated = new EventEmitter<any>();

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(
    public groupeEtudiantService: GroupeEtudiantService,
    public etudiantGroupeEtudiantService: EtudiantGroupeEtudiantService,
    private authService: AuthService,
    private ldapService: LdapService,
    private ufrService: UfrService,
    private etapeService: EtapeService,
    private conventionService: ConventionService,
    private router: Router,
    private messageService: MessageService,
    private configService: ConfigService,
    private fb: FormBuilder,
    private etudiantService: EtudiantService,
  ) {
    this.form = this.fb.group({
      codeGroupe: [null, [Validators.required, Validators.maxLength(100)]],
      nomGroupe: [null, [Validators.required, Validators.maxLength(100)]],
    });
    this.formAddEtudiants = this.fb.group({
      codEtu: new FormControl<string|null>(null),
      nom: new FormControl<string|null>(null),
      prenom: new FormControl<string|null>(null),
      etape: new FormControl<string|DiplomeEtEtape|null>(null, {
        validators: [control => !control?.value || typeof control?.value === 'string' ? { required: true } : null],
      }),
      composante: new FormControl<string|null>(null),
      annee: new FormControl<number|null>(this.currentYear),
    });

    this.formAddEtudiants.get('etape')?.disable({ emitEvent: false });
    this.formAddEtudiants.get('annee')?.valueChanges.subscribe((changes) => {
      this.getEtapes(changes, this.formAddEtudiants.get('composante')!.value);
    });
    this.formAddEtudiants.get('composante')?.valueChanges.subscribe((changes) => {
      this.getEtapes(this.formAddEtudiants.get('annee')!.value, changes);
    });
    this.filteredEtapes$ = this.formAddEtudiants.get('etape')!.valueChanges.pipe(
      map((value:string|DiplomeEtEtape|null) => this.filterEtapes(value)),
    );
  }

  getEtapes(annee: number|null, composante: string|null): void {
    this.etudiants = [];
    this.selectedAdd = [];
    if (annee && composante) {
      // recherche de la liste des étapes en fonction de l'année et de la composante sélectionnée
      this.etapeService.getApogeeEtapes(annee.toString(), composante).subscribe(response => {
        this.etapeList = response;
        this.formAddEtudiants.get('etape')?.enable({ emitEvent: true });
        this.formAddEtudiants.get('etape')!.setValue(null, { emitEvent: true });
      }, () => {
        this.emptyEtape();
      });
    } else {
      this.emptyEtape();
    }
  }

  emptyEtape(): void {
    this.formAddEtudiants.get('etape')?.setValue(null, { emitEvent: false });
    this.formAddEtudiants.get('etape')?.disable({ emitEvent: false });
  }

  filterEtapes(value?: string|DiplomeEtEtape|null): DiplomeEtape[] {
    if (!value) return this.etapeList;
    return typeof value === 'string' ? this.filterEtapesBySearchString(value) : this.etapeList;
  }
  private filterEtapesBySearchString(value: string): DiplomeEtape[] {
    value = value.toLowerCase();
    return this.etapeList.map(
      diplome => { return { ...diplome, listeEtapes: diplome.listeEtapes.filter(
          etape => etape.codeEtp.toLowerCase().includes(value)
            || etape.libWebVet.toLowerCase().includes(value),
        ) }; }
    ).filter(diplome => diplome.listeEtapes.length > 0)
      || this.etapeList;
  }

  displayEtape(dpe?: DiplomeEtEtape) {
    return dpe ? dpe.etape.codeEtp + '-' + dpe.etape.codVrsVet + ' ' + dpe.etape.libWebVet : null;
  }

  ngOnInit(): void {
    this.groupeEtudiantColumns = [...this.sharedData.columns];
    this.groupeEtudiantFilters = [...this.sharedData.filters];
    this.columns =  [...this.sharedData.columns];

    this.ufrService.getApogeeComposantes().subscribe((response: any) => {
      this.ufrList = response;
    })
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
    if (!this.formAddEtudiants.valid) {
      this.etudiants = [];
      this.selectedAdd = [];
      return;
    }
    const value = this.formAddEtudiants.value;
    const etape: DiplomeEtEtape = value.etape as DiplomeEtEtape;
    const data: EtudiantDiplomeEtapeSearch = {
      annee: value.annee!.toString(),
      //codeComposante: value.composante,
      codeEtape: etape.etape.codeEtp,
      versionEtape: etape.etape.codVrsVet,
      codeDiplome: etape.diplome.codeDiplome,
      versionDiplome: etape.diplome.versionDiplome,
    };

    this.etudiantService.searchEtudiantsDiplomeEtape(data).subscribe(response => {
      this.etudiants = response.map(etudiant => {
        return {
          ...etudiant,
          // complète les données absentes de la réponse
          annee: value.annee!.toString(),
          codeComposante: value.composante,
          codeEtape: etape.etape.codeEtp,
          versionEtape: etape.etape.codVrsVet,
          libelleEtape: etape.etape.libWebVet,
          codeDiplome: etape.diplome.codeDiplome,
          versionDiplome: etape.diplome.versionDiplome,
        } as EtudiantDiplomeEtapeResponse;
      });
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

  isSelectedAdd(data: EtudiantDiplomeEtapeResponse): boolean {
    return this.selectedAdd.find((r: any) => {return r.codEtu === data.codEtu}) !== undefined;
  }
  toggleSelectedAdd(data: EtudiantDiplomeEtapeResponse): void {
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
      let data: GroupeEtudiantDto = {
        ...this.form.value,
        etudiantRemovedIds: this.selectedRemove.map((s: any) => s.id),
        etudiantAdded: this.selectedAdd,
      };

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

}
type DiplomeEtEtape = {diplome:DiplomeEtape,etape:EtapeV2Apogee};
