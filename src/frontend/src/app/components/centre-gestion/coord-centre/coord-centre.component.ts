import { Component, EventEmitter, Input, OnInit, Output, QueryList, ViewChild, ViewChildren } from '@angular/core';
import {FormBuilder, FormControl, FormGroup, isFormControl, Validators} from "@angular/forms";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { CommuneService } from "../../../services/commune.service";
import { NiveauCentreService } from "../../../services/niveau-centre.service";
import { CritereGestionService } from "../../../services/critere-gestion.service";
import { MessageService } from "../../../services/message.service";
import { ReplaySubject, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MatSelect } from '@angular/material/select';
import { HttpErrorResponse } from '@angular/common/http';
import { MatExpansionPanel } from '@angular/material/expansion';
import { TableComponent } from "../../table/table.component";

@Component({
  selector: 'app-coord-centre',
  templateUrl: './coord-centre.component.html',
  styleUrls: ['./coord-centre.component.scss']
})
export class CoordCentreComponent implements OnInit {

  @Input() centreGestion: any;
  @Input() form!: FormGroup;

  @Output() refreshCentreGestion = new EventEmitter<any>();
  @Output() update = new EventEmitter<any>();

  etapeFilterCtrl: FormControl = new FormControl();
  filteredEtapes: ReplaySubject<any> = new ReplaySubject<any>(1);
  selectedValues: any[] = [];
  @ViewChild('multiSelect') multiSelect!: MatSelect;

  _onDestroy = new Subject<void>();

  niveauxCentre: any[] = [];
  composantes: any[] = [];
  etapes: any[] = [];
  communes: any[] = [];

  displayedEtapesColumns = ['id.code', 'id.codeVersionEtape', 'libelle', 'action'];
  sortColumn = 'id.code';
  filters: any[] = [];

  selectedComposante: any;

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChildren(MatExpansionPanel) pannels!: QueryList<MatExpansionPanel>;

  constructor(
    private centreGestionService: CentreGestionService,
    public communeService: CommuneService,
    private niveauCentreService: NiveauCentreService,
    public critereGestionService: CritereGestionService,
    private messageService: MessageService,
    private fb: FormBuilder) { }

  ngOnInit(): void {
    this.communeService.getPaginated(1, 0, 'lib', 'asc', "").subscribe((response: any) => {
      this.communes = response;
    });
    this.niveauCentreService.findList().subscribe((response: any) => {
      // Si c'est un centre de type établissement, on ajoute le niveau centre ETABLISSEMENT pour qu'il apparaisse au niveau du select
      if (this.centreGestion.niveauCentre && this.centreGestion.niveauCentre.libelle === 'ETABLISSEMENT') {
        response.push(this.centreGestion.niveauCentre);
      }
      this.niveauxCentre = response;
    });
    this.form = new FormGroup({
      nomCentre: new FormControl(
        this.centreGestion.nomCentre || '',
        [Validators.required]
      ),
      niveauCentre: new FormControl(
        this.centreGestion.niveauCentre || '',
        [Validators.required]
      ),
      siteWeb: new FormControl(
        this.centreGestion.siteWeb || '',
        [
          Validators.pattern(/^(https?:\/\/)?([a-zA-Z0-9.-]+)\.([a-zA-Z]{2,})(:[0-9]{1,5})?(\/.*)?$/)
        ]
      ),
      mail: new FormControl(
        this.centreGestion.mail || '',
        [
          Validators.required,
          Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/) // Regex email RFC 5322
        ]
      ),
      telephone: new FormControl(
        this.centreGestion.telephone || '',
        [
          Validators.required,
          Validators.pattern(/^(?:(?:\+|00)\d{1,4}[-.\s]?|0)\d{1,4}([-.\s]?\d{1,4})*$/) // Accepte E.164 et numéros locaux
        ]
      ),
      fax: new FormControl(
        this.centreGestion.fax || ''
      ),
      adresse: new FormControl(
        this.centreGestion.adresse || ''
      ),
      voie: new FormControl(
        this.centreGestion.voie || ''
      ),
      commune: new FormControl(
        this.centreGestion.commune || ''
      ),
      codePostal: new FormControl(
        this.centreGestion.codePostal || '',
        [
          Validators.required,
          Validators.pattern(/^\d{5}$/) // Validation pour un code postal français à 5 chiffres
        ]
      )
    });

    if (this.centreGestion.id) {
      this.setFormData();
      if (this.centreGestion.niveauCentre.libelle == 'UFR') {
        this.getComposantes();
        this.getCentreComposante();
      }
      else if (this.centreGestion.niveauCentre.libelle == 'ETAPE') {
        this.getEtapes();
        this.getCentreEtapes();
        this.filters.push({id: 'centreGestion.id', value: this.centreGestion.id, type: 'int', hidden: true});
      }
      if (this.centreGestion.niveauCentre && this.centreGestion.niveauCentre.libelle === 'ETABLISSEMENT') {
        this.form.get('niveauCentre')?.disable();
      }
    }
  }

  getComposantes(): void {
    this.centreGestionService.getComposantes(this.centreGestion.id).subscribe((response: any) => {
      this.composantes = response;
    });
  }

  getCentreComposante(): void {
    this.centreGestionService.getCentreComposante(this.centreGestion.id).subscribe((response: any) => {
      this.selectedComposante = response;
    });
  }

  getEtapes(): void {
    this.centreGestionService.getEtapes(this.centreGestion.id).subscribe((response: any) => {
      this.etapes = response;
      this.filteredEtapes.next(this.etapes.slice());
      this.etapeFilterCtrl.valueChanges
        .pipe(takeUntil(this._onDestroy))
        .subscribe(() => {
          this.filterEtapes();
        });
    });
  }

  getCentreEtapes(): void {
    this.centreGestionService.getCentreEtapes(this.centreGestion.id).subscribe((response: any) => {
      this.selectedValues = response;
    });
  }

  validate(): void {
    const data = {...this.form.value}
    if (this.centreGestion.id) {
      this.update.emit();
    } else {
      if (!this.form.valid) {
        this.messageService.setError("Veuillez remplir les champs obligatoires");
        if (this.form.get('nomCentre')?.invalid || this.form.get('niveauCentre')?.invalid) {
          this.pannels.first.open();
          this.form.get('nomCentre')?.markAsTouched();
          this.form.get('niveauCentre')?.markAsTouched();
        }
        return;
      }
      this.centreGestionService.create(data).subscribe((response: any) => {
        this.messageService.setSuccess("Centre de gestion créé");
        this.centreGestion = response;
        this.refreshCentreGestion.emit(this.centreGestion);
        if (this.centreGestion.niveauCentre && this.centreGestion.niveauCentre.libelle === 'ETABLISSEMENT') {
          this.form.get('niveauCentre')?.disable();
        }
        this.getComposantes();
        this.getEtapes();
        this.getCentreEtapes();
        this.filters.push({id: 'centreGestion.id', value: this.centreGestion.id, type: 'int', hidden: true});
      });
    }
  }

  setComposante(): void {
    this.centreGestionService.setComposante(this.selectedComposante, this.centreGestion.id).subscribe((response: any) => {
      this.getComposantes();
      this.getCentreComposante();
    });
  }

  compare(option: any, value: any): boolean {
    if (option && value) {
      return option.id === value.id;
    }
    return false;
  }

  compareCode(option: any, value: any): boolean {
    if (option && value) {
      return option.code === value.code && option.codeVrsEtp === value.codeVrsEtp;
    }
    return false;
  }

  setFormData(): void {
    this.form.setValue({
      nomCentre: this.centreGestion.nomCentre,
      niveauCentre: this.centreGestion.niveauCentre,
      siteWeb: this.centreGestion.siteWeb,
      mail: this.centreGestion.mail,
      telephone: this.centreGestion.telephone,
      fax: this.centreGestion.fax,
      adresse: this.centreGestion.adresse,
      voie: this.centreGestion.voie,
      commune: this.centreGestion.commune,
      codePostal: this.centreGestion.codePostal,
    });
  }

  composanteChange(composante: any) {
    this.selectedComposante = composante;
    this.setComposante();
  }

  etapesChange(etape: any, selected: any) {
    if (selected) {
      this.centreGestionService.addEtape(etape, this.centreGestion.id).subscribe((response: any) => {
        this.getCentreEtapes();
        if (this.appTable)
          this.appTable.update();
      });
    } else {
      this.deleteEtape(etape.code, etape.codeVrsEtp);
    }
  }

  deleteEtape(code: string, codeVrsEtp: string) {
    this.centreGestionService.deleteEtape(code, codeVrsEtp).subscribe((response: any) => {
      this.getCentreEtapes();
      if (this.appTable)
        this.appTable.update();
    }, (err: HttpErrorResponse) => {
      this.getCentreEtapes();
      if (this.appTable)
        this.appTable.update();
    });
  }

  filterEtapes() {
    if (!this.etapes) {
      return;
    }

    let search = this.etapeFilterCtrl.value;
    if (!search) {
      this.filteredEtapes.next(this.etapes.slice());
      return;
    } else {
      search = search.toLowerCase();
    }

    this.filteredEtapes.next(
      this.etapes.filter(etape => etape.code.toLowerCase().indexOf(search) > -1 || etape.libelle.toLowerCase().indexOf(search) > -1)
    );
  }

  updateCommune(commune : any): void {
    this.form.get('commune')?.setValue(commune.split(' - ')[0]);
    this.form.get('codePostal')?.setValue(commune.split(' - ')[1]);
  }

  isCodePostalValid() {
    let codePostal = this.form.get('codePostal')?.value;
    if (codePostal) {
      let commune = this.communes.find(c => c.codePostal === codePostal);
      if (commune)
        return true;
    }
    return false;
  }

  isCommuneValid() {
    let commune = this.form.get('commune')?.value;
    if (commune) {
      let c = this.communes.find(c => c.libelle === commune);
      if (c)
        return true;
    }
    return false;
  }
}
