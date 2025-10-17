import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormGroup } from "@angular/forms";
import { CdkDragDrop, moveItemInArray } from "@angular/cdk/drag-drop";
import {ConfigService} from "../../../services/config.service";

@Component({
    selector: 'app-centre-signature-electronique',
    templateUrl: './signature-electronique.component.html',
    styleUrls: ['./signature-electronique.component.scss'],
    standalone: false
})
export class CentreSignatureElectroniqueComponent implements OnInit {

  @Input() centreGestion: any;
  @Input() form!: FormGroup;

  signataires: any[] = [];
  canMoveSignataire!: boolean;

  constructor(private configService : ConfigService) { }

  ngOnInit(): void {
    if (this.centreGestion.id) {
      this.setFormData();
    }
    this.configService.getConfigSignature().subscribe(res=>{
      this.canMoveSignataire = res.autoriserModifOrdreSignataireCG
    })
  }

  setFormData(): void {
    this.signataires = this.centreGestion.signataires;
    this.form.patchValue({
      circuitSignature: this.centreGestion.circuitSignature,
      signataires: this.signataires,
      envoiDocumentSigne: this.centreGestion.envoiDocumentSigne,
    });
  }

  drop(event: CdkDragDrop<string[]>): void {
    moveItemInArray(this.signataires, event.previousIndex, event.currentIndex);
    let newOrdre = 1;
    this.signataires.forEach((s) => {
      s.ordre = newOrdre++;
    });
    this.updateSignataire();
  }

  updateSignataire(): void {
    this.form.get('signataires')?.setValue(this.signataires);
  }



}
