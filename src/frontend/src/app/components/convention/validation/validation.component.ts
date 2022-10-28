import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { ConventionService } from "../../../services/convention.service";
import { ConfigService } from "../../../services/config.service";
import { MessageService } from "../../../services/message.service";

@Component({
  selector: 'app-validation',
  templateUrl: './validation.component.html',
  styleUrls: ['./validation.component.scss']
})
export class ValidationComponent implements OnInit, OnChanges {

  @Input() convention: any;
  @Output() conventionChanged = new EventEmitter<any>();

  validationLibelles: any = {};
  validations: string[] = [];
  historiques: any[] = [];
  columnsHisto = ['modifiePar', 'type', 'valeurAvant', 'valeurApres', 'date'];
  validee = false;

  constructor(
    private configService: ConfigService,
    private conventionService: ConventionService,
    private messageService: MessageService,
  ) { }

  ngOnChanges(): void {
    this.setValidee();
  }

  ngOnInit(): void {
    for (let ordre of [1, 2, 3]) {
      for (let validation of ['validationPedagogique', 'verificationAdministrative', 'validationConvention']) {
        if (this.convention.centreGestion[validation] && this.convention.centreGestion[validation + 'Ordre'] === ordre) {
          this.validations.push(validation);
        }
      }
    }
    this.configService.getConfigGenerale().subscribe((response) => {
      this.validationLibelles.validationPedagogique = response.validationPedagogiqueLibelle;
      this.validationLibelles.validationConvention = response.validationAdministrativeLibelle;
    });

    this.setValidee();
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

  setValidee(): void {
    this.validee = (!this.convention.centreGestion.validationPedagogique || this.convention.validationPedagogique)
      && (!this.convention.centreGestion.verificationAdministrative || this.convention.verificationAdministrative)
      && (!this.convention.centreGestion.validationConvention || this.convention.validationConvention);
  }

  envoiSignatureElectronique(): void {
    this.conventionService.envoiSignatureElectronique([this.convention.id]).subscribe((response: any) => {
      if (response === 1) {
        this.messageService.setSuccess(`Convention envoyée`);
        this.conventionService.getById(this.convention.id).subscribe((responseConv) => {
          this.convention = responseConv;
          this.conventionChanged.emit(this.convention);
        });
      }
    });
  }

}
