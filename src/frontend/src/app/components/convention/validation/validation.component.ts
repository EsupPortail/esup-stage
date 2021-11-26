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
    // TODO gestion de l'ordre des validations
    if (this.convention.centreGestion.validationPedagogique) this.validations.push('validationPedagogique');
    this.validations.push('validationConvention');
  }

}
