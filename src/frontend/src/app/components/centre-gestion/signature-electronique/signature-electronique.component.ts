import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormGroup } from "@angular/forms";
import { CdkDragDrop, moveItemInArray } from "@angular/cdk/drag-drop";

@Component({
  selector: 'app-centre-signature-electronique',
  templateUrl: './signature-electronique.component.html',
  styleUrls: ['./signature-electronique.component.scss']
})
export class CentreSignatureElectroniqueComponent implements OnInit {

  @Input() centreGestion: any;
  @Input() form!: FormGroup;

  signataires: any[] = [];

  constructor() { }

  ngOnInit(): void {
    if (this.centreGestion.id) {
      this.setFormData();
    }
  }

  setFormData(): void {
    this.signataires = this.centreGestion.signataires;
    this.form.patchValue({
      circuitSignature: this.centreGestion.circuitSignature,
      signataires: this.signataires,
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
