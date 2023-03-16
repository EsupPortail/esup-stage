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

  ordreSignature: string[] = [];

  constructor() { }

  ngOnInit(): void {
    if (this.centreGestion.id) {
      this.setFormData();
    }
  }


  setFormData(): void {
    this.ordreSignature = JSON.parse(this.centreGestion.ordreSignature);
    this.form.patchValue({
      circuitSignature: this.centreGestion.circuitSignature,
      ordreSignature: JSON.stringify(this.ordreSignature),
    });
  }

  drop(event: CdkDragDrop<string[]>): void {
    moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    this.form.get('ordreSignature')?.setValue(JSON.stringify(this.ordreSignature));
  }

}
