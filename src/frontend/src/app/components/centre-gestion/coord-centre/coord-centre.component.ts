import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { NiveauCentreService } from "../../../services/niveau-centre.service";
import { MessageService } from "../../../services/message.service";

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

  niveauxCentre: any[] = [];
  composantes: any[] = [];
  etapes: any[] = [];

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

}
