import { Component, OnInit, Input } from '@angular/core';
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

  form: FormGroup;
  niveauxCentre: any[] = [];

  constructor(private centreGestionService: CentreGestionService, private niveauCentreService: NiveauCentreService, private messageService: MessageService, private fb: FormBuilder) {
    this.form = this.fb.group({
      nomCentre: [null, [Validators.required, Validators.maxLength(100)]],
      niveauCentre: [null, [Validators.required]],
      siteWeb: [null, [Validators.maxLength(50)]],
      mail: [null, [Validators.required, Validators.email, Validators.maxLength(50)]],
      telephone: [null, [Validators.required, Validators.maxLength(20)]],
      fax: [null, [Validators.maxLength(20)]],
      adresse: [null, [Validators.maxLength(200)]],
      voie: [null, [Validators.required, Validators.maxLength(200)]],
      commune: [null, [Validators.required, Validators.maxLength(200)]],
      codePostal: [null, [Validators.required, Validators.maxLength(10)]],
    });
  }

  ngOnInit(): void {
    this.niveauCentreService.findList().subscribe(response => {
      this.niveauxCentre = response;
    });
    if (this.centreGestion.id) {
      this.setFormData();
    }
  }

  validate(): void {
    if (this.form.valid) {
      const data = {...this.form.value}
      if (this.centreGestion.id) {

      } else {
        this.centreGestionService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess("Centre de gestion créé");
          this.centreGestion = response;
        });
      }
    }
  }

  compare(option: any, value: any): boolean {
    if (option && value) {
      return option.id === value.id;
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

}
