import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ConventionService } from "../../../services/convention.service";
import { ConfigService } from "../../../services/config.service";

@Component({
  selector: 'app-validation',
  templateUrl: './validation.component.html',
  styleUrls: ['./validation.component.scss']
})
export class ValidationComponent implements OnInit {

  @Input() convention: any;
  @Output() conventionChanged = new EventEmitter<any>();

  validationLibelles: any = {};
  validations: string[] = [];
  historiques: any[] = [];
  columnsHisto = ['modifiePar', 'type', 'valeurAvant', 'valeurApres', 'date'];

  constructor(
    private configService: ConfigService,
    private conventionService: ConventionService,
  ) { }

  ngOnInit(): void {
    for (let validation of ['validationPedagogique', 'verificationAdministrative', 'validationConvention']) {
      for (let ordre of [1, 2, 3]) {
        if (this.convention.centreGestion[validation] && this.convention.centreGestion[validation + 'Ordre'] === ordre) {
          this.validations.push(validation);
        }
      }
    }
    this.configService.getConfigGenerale().subscribe((response) => {
      this.validationLibelles.validationPedagogique = response.validationPedagogiqueLibelle;
      this.validationLibelles.validationConvention = response.validationAdministrativeLibelle;
    })

    this.getHistorique();
  }

  getHistorique(): void {
    this.conventionService.getHistoriqueValidations(this.convention.id).subscribe((response: any) => {
      this.historiques = response;
    });
  }

  updateConvention(convention: any): void {
    this.getHistorique();
    this.convention = convention;
    this.conventionChanged.emit(this.convention);
  }

}
