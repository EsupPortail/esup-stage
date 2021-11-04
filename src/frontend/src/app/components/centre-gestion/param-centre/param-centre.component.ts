import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { MessageService } from "../../../services/message.service";

@Component({
  selector: 'app-param-centre',
  templateUrl: './param-centre.component.html',
  styleUrls: ['./param-centre.component.scss']
})
export class ParamCentreComponent implements OnInit {

  @Input() centreGestion: any;
  @Input() form: FormGroup;

  @Output() update = new EventEmitter<any>();

  constructor(private centreGestionService: CentreGestionService, private messageService: MessageService, private fb: FormBuilder) { }

  ngOnInit(): void {
    if (this.centreGestion.id) {
      this.setFormData();
    }
  }

  validate(): void {
    if (this.centreGestion.id) {
      this.update.emit();
    }
  }

  setFormData(): void {
    this.form.setValue({
      saisieTuteurProParEtudiant: this.centreGestion.saisieTuteurProParEtudiant,
      autorisationEtudiantCreationConvention: this.centreGestion.autorisationEtudiantCreationConvention,
      validationPedagogique: this.centreGestion.validationPedagogique,
      recupInscriptionAnterieure: this.centreGestion.recupInscriptionAnterieure,
      dureeRecupInscriptionAnterieure: this.centreGestion.dureeRecupInscriptionAnterieure,
      urlPageInstruction: this.centreGestion.urlPageInstruction,
      nomViseur: this.centreGestion.nomViseur,
      prenomViseur: this.centreGestion.prenomViseur,
      qualiteViseur: this.centreGestion.qualiteViseur,
    });
  }

}
