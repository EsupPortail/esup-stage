import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-validation',
  templateUrl: './validation.component.html',
  styleUrls: ['./validation.component.scss']
})
export class ValidationComponent implements OnInit {

  @Input() convention: any;

  validations: string[] = [];

  constructor() { }

  ngOnInit(): void {
    if (this.convention.centreGestion.validationPedagogique && this.convention.centreGestion.validationPedagogiqueOrdre === 1) {
      this.validations.push('validationPedagogique');
    }
    if (this.convention.centreGestion.validationConvention && this.convention.centreGestion.validationConventionOrdre === 1) {
      this.validations.push('validationConvention');
    }
    if (this.convention.centreGestion.validationPedagogique && this.convention.centreGestion.validationPedagogiqueOrdre === 2) {
      this.validations.push('validationPedagogique');
    }
    if (this.convention.centreGestion.validationConvention && this.convention.centreGestion.validationConventionOrdre === 2) {
      this.validations.push('validationConvention');
    }
  }

}
