import { Component, Input, OnInit } from '@angular/core';
import { ConfigService } from "../../../../services/config.service";

@Component({
  selector: 'app-validation-card',
  templateUrl: './validation-card.component.html',
  styleUrls: ['./validation-card.component.scss']
})
export class ValidationCardComponent implements OnInit {

  @Input() convention: any;
  @Input() validation: string;

  validationLibelles: any = {};

  constructor(
    private configService: ConfigService,
  ) { }

  ngOnInit(): void {
    this.configService.getConfigGenerale().subscribe((response) => {
      this.validationLibelles.validationPedagogique = response.validationPedagogiqueLibelle;
      this.validationLibelles.validationConvention = response.validationAdministrativeLibelle;
    })
  }

  canRevertValidation(): boolean {
    const validationOrdre = this.convention.centreGestion[this.validation + 'Ordre'];
    if (validationOrdre === 2) { // On peut tout le temps dévalider la dernière validation
      return true;
    }
    const otherValidation = this.validation === 'validationPedagogique' ? 'validationConvention' : 'validationPedagogique';
    return !this.convention[otherValidation];
  }

}
