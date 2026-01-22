import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { GroupeEtudiantService } from "../../../services/groupe-etudiant.service";
import { UfrService } from "../../../services/ufr.service";
import { EtapeService } from "../../../services/etape.service";
import { ConventionService } from "../../../services/convention.service";
import { EtudiantGroupeEtudiantService } from "../../../services/etudiant-groupe-etudiant.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import { LdapService } from "../../../services/ldap.service";
import { MessageService } from "../../../services/message.service";
import { ReplaySubject, Subject } from 'rxjs';
import { ConfigService } from "../../../services/config.service";
import { SortDirection } from "@angular/material/sort";
import { FormBuilder, FormControl, FormGroup, Validators } from "@angular/forms";
import { EtudiantService } from '../../../services/etudiant.service';

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

  annees: { label:string, value:number }[] = [];
  ufrList: any[] = [];
  etapeList: any[] = [];

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
      codEtu: [null, []],
      nom: [null, []],
      prenom: [null, []],
      etape: [null, []],
      composante: [null, []],
      annee: [null, []],
    });

    this.formAddEtudiants.get('etape')?.disable({ emitEvent: false });
    this.formAddEtudiants.get('annee')?.valueChanges.subscribe((changes) => {
      this.getEtapes(changes, this.formAddEtudiants.get('composante')?.value);
    });
    this.formAddEtudiants.get('composante')?.valueChanges.subscribe((changes) => {
      this.getEtapes(this.formAddEtudiants.get('annee')?.value, changes);
    });
  }

  // Années N-1 / N / N+1
  initCurrentYear(anneeEnCours?: string): void {
    const currentYear:number = anneeEnCours ? Number.parseInt(anneeEnCours) : this.anneeUniversitaire(new Date());
    this.annees = [
      { label: (currentYear - 1) + '/' + (currentYear), value: currentYear - 1 },
      { label: currentYear + '/' + (currentYear + 1), value: currentYear },
      { label: (currentYear + 1) + '/' + (currentYear + 2), value: currentYear + 1 },
    ];
    this.formAddEtudiants.get('annee')?.setValue(currentYear);
  }

  anneeUniversitaire(now:Date): number {
    return now.getMonth() > 7 ? now.getFullYear() : now.getFullYear() -1;
  }

  getEtapes(annee: string, composante: string): void {
    this.etudiants = [];
    this.selectedAdd = [];
    if (annee && composante) {
      // recherche de la liste des étapes en fonction de l'année et de la composante sélectionnée
      this.etapeList = [];
      this.etapeService.getApogeeEtapes(annee, composante).subscribe((response: any) => {
        response.forEach((r: any) => {
          this.etapeList = this.etapeList.concat(r.listeEtapes.map((e: any) => {
            return {
              diplome: {codeDiplome: r.codeDiplome, versionDiplome: r.versionDiplome, libDiplome: r.libDiplome},
              etape: e
            }
          }));
        });
      }, () => {
        this.emptyEtape();
      });
      this.formAddEtudiants.get('etape')?.enable({ emitEvent: false });
    } else {
      this.emptyEtape();
    }
  }

  emptyEtape(): void {
    this.formAddEtudiants.get('etape')?.setValue(null, { emitEvent: false });
    this.formAddEtudiants.get('etape')?.disable({ emitEvent: false });
  }

  ngOnInit(): void {
    this.conventionService.getListAnnee().subscribe({
      next: (listAnneeData) => this.initCurrentYear(
          listAnneeData.find((a:any) => a?.anneeEnCours === true)?.annee
        ),
      error: (e) => this.initCurrentYear()
    });

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
    const data = {...this.formAddEtudiants.value};
    data.codeComposante = data.composante;
    data.codeEtape = data.etape.etape.codeEtp;
    data.versionEtape = data.etape.etape.codVrsVet;
    data.codeDiplome = data.etape.diplome.codeDiplome;
    data.versionDiplome = data.etape.diplome.versionDiplome;

    this.etudiantService.searchEtudiantsDiplomeEtape(data).subscribe((response: any) => {
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
      this.selectedAdd.push(this.getEtudiantValue(data));
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
        this.selectedAdd.push(this.getEtudiantValue(d));
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

  getEtudiantValue(etu: any): any {
    return {
      ...etu,
      annee: this.formAddEtudiants.get('annee')?.value,
      codeComposante: this.formAddEtudiants.get('composante')?.value,
      codeDiplome: this.formAddEtudiants.get('etape')?.value.diplome.codeDiplome,
      versionDiplome: this.formAddEtudiants.get('etape')?.value.diplome.versionDiplome,
      codeEtape: this.formAddEtudiants.get('etape')?.value.etape.codeEtp,
      versionEtape: this.formAddEtudiants.get('etape')?.value.etape.codVrsVet,
    }
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

}
