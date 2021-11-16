import { Component, OnInit, Input, Output, EventEmitter, ViewChild, OnDestroy, AfterViewInit } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { NiveauCentreService } from "../../../services/niveau-centre.service";
import { MessageService } from "../../../services/message.service";
import { ReplaySubject, Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import { MatSelect } from '@angular/material/select';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-coord-centre',
  templateUrl: './coord-centre.component.html',
  styleUrls: ['./coord-centre.component.scss']
})
export class CoordCentreComponent implements OnInit {

  @Input() centreGestion: any;
  @Input() form: FormGroup;

  @Output() refreshCentreGestion = new EventEmitter<any>();
  @Output() update = new EventEmitter<any>();

  etapeFilterCtrl: FormControl = new FormControl();
  filteredEtapes: ReplaySubject<any> = new ReplaySubject<any>(1);
  selectedValues: any[] = [];
  @ViewChild('multiSelect') multiSelect: MatSelect;

  _onDestroy = new Subject<void>();

  niveauxCentre: any[] = [];
  composantes: any[] = [];
  etapes: any[] = [];

  displayedEtapesColumns: string[] = ['code', 'codeVrsEtp', 'libelle', 'action'];

  selectedComposante: any;

  constructor(private centreGestionService: CentreGestionService, private niveauCentreService: NiveauCentreService, private messageService: MessageService, private fb: FormBuilder) { }

  ngOnInit(): void {
    this.niveauCentreService.findList().subscribe((response: any) => {
      this.niveauxCentre = response;
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
      }
      this.form.get('niveauCentre')?.disable();
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
        return;
      }
      this.centreGestionService.create(data).subscribe((response: any) => {
        this.messageService.setSuccess("Centre de gestion créé");
        this.centreGestion = response;
        this.refreshCentreGestion.emit(this.centreGestion);
        this.form.get('niveauCentre')?.disable();
        this.getComposantes();
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
      return option.code === value.code;
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
      });
    } else {
      this.centreGestionService.deleteEtape(etape.code, etape.codeVrsEtp).subscribe((response: any) => {
        this.getCentreEtapes();
      }, (err: HttpErrorResponse) => {
        this.getCentreEtapes();
      });
    }
  }

  deleteEtape(etape: any) {
    this.centreGestionService.deleteEtape(etape.code, etape.codeVrsEtp).subscribe((response: any) => {
      this.getCentreEtapes();
    }, (err: HttpErrorResponse) => {
      this.getCentreEtapes();
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

}
