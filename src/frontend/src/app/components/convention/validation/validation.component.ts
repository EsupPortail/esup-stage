import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-validation',
  templateUrl: './validation.component.html',
  styleUrls: ['./validation.component.scss']
})
export class ValidationComponent implements OnInit {

  @Input() convention: any;
  @Output() conventionChanged = new EventEmitter<any>();

  validations: string[] = [];

  constructor() { }

  ngOnInit(): void {
    for (let validation of ['validationPedagogique', 'verificationAdministrative', 'validationConvention']) {
      for (let ordre of [1, 2, 3]) {
        if (this.convention.centreGestion[validation] && this.convention.centreGestion[validation + 'Ordre'] === ordre) {
          this.validations.push(validation);
        }
      }
    }
  }

  updateConvention(convention: any): void {
    this.convention = convention;
    this.conventionChanged.emit(this.convention);
  }

}
