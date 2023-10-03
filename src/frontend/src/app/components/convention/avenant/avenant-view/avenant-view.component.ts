import { Component, Output, EventEmitter, OnInit, Input, ViewChild } from '@angular/core';
import { AuthService } from "../../../../services/auth.service";
import { MessageService } from "../../../../services/message.service";
import { AvenantService } from "../../../../services/avenant.service";
import { ConventionService } from "../../../../services/convention.service";
import { PeriodeInterruptionStageService } from "../../../../services/periode-interruption-stage.service";
import { PeriodeInterruptionAvenantService } from "../../../../services/periode-interruption-avenant.service";
import * as FileSaver from 'file-saver';
import { ConfirmComponent } from 'src/app/components/confirm/confirm.component';
import { ConfigService } from 'src/app/services/config.service';

@Component({
  selector: 'app-avenant-view',
  templateUrl: './avenant-view.component.html',
  styleUrls: ['./avenant-view.component.scss']
})
export class AvenantViewComponent implements OnInit {

  interruptionsStage: any[] = [];
  addedInterruptionsStage: any[] = [];
  modifiedInterruptionsStage: any[] = [];

  @Input() avenant: any;
  @Input() convention: any;
  @Output() updated = new EventEmitter<any>();

  @ViewChild('confirmComponent') confirmComponent!: ConfirmComponent;
  confirmMessage: string = `L'adresse mail ou le numéro de téléphone n'est pas renseigné pour les profils suivants :<div>__profils__</div>Souhaitez-vous continuer ?`;
  errorMessage: string = `L'adresse mail ou le numéro de téléphone doit être renseigné pour les profils suivants :<div>__profils__</div>`;

  signatureEnabled = false;

  numberPeriodeInterruption !:number;

  constructor(private authService: AuthService,
              private messageService: MessageService,
              private avenantService: AvenantService,
              private conventionService: ConventionService,
              private periodeInterruptionStageService: PeriodeInterruptionStageService,
              private periodeInterruptionAvenantService: PeriodeInterruptionAvenantService,
              private configService: ConfigService,
            ) { }

  ngOnInit(): void {
    this.loadInterruptionsStage();
    this.periodeInterruptionExist();
    this.configService.getConfigGenerale().subscribe((response) => {
      this.signatureEnabled = response.signatureEnabled;
    });
  }

  isEtudiant(): boolean {
    return this.authService.isEtudiant();
  }

  isGestionnaire(): boolean {
    return this.authService.isGestionnaire() || this.authService.isAdmin();
  }

  periodeInterruptionExist() {
    if (this.avenant.id)
    {
      this.periodeInterruptionAvenantService.getByAvenant(this.avenant.id).subscribe((res) => {
        this.numberPeriodeInterruption = res.length;
      })
    }
  }
  cancelValidation(): void {
    this.avenantService.cancelValidation(this.avenant.id).subscribe((response: any) => {
      this.avenant = response;
      this.messageService.setSuccess('Validation de l\'avenant annulée avec succès');
      this.updated.emit();
    });
  }


  loadInterruptionsStage() : void{
    this.periodeInterruptionStageService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.interruptionsStage = response;
      this.loadInterruptionsAvenant();
    });
  }

  loadInterruptionsAvenant() : void {
    this.periodeInterruptionAvenantService.getByAvenant(this.avenant.id).subscribe((response: any) => {
      for(let interruption of response){
        if (interruption.isModif){
          this.modifiedInterruptionsStage.push(interruption);
        }else{
          this.addedInterruptionsStage.push(interruption);
        }
      }
    });
  }

  printAvenant() : void {
    this.conventionService.getAvenantPDF(this.avenant.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: "application/pdf"});
      let filename = 'Avenant_' + this.convention.id + '_' + this.convention.etudiant.prenom + '_' + this.convention.etudiant.nom + '.pdf';
      FileSaver.saveAs(blob, filename);
    });
  }

  controleSignatureElectronique(): void {
    this.avenantService.controleSignatureElectronique(this.avenant.id).subscribe((responseControle: any) => {
      if (responseControle.error.length > 0) {
        this.messageService.setError(this.errorMessage.replace('__profils__', `<ul>${responseControle.error.map((e: string) => `<li>${e}</li>`).join('')}</ul>`));
      } else if (responseControle.warning.length > 0) {
        this.confirmMessage = this.confirmMessage.replace('__profils__', `<ul>${responseControle.warning.map((w: string) => `<li>${w}</li>`).join('')}</ul>`);
        this.confirmComponent.onClick();
      } else {
        this.envoiSignatureElectronique();
      }
    });
  }

  envoiSignatureElectronique(): void {
    this.avenantService.envoiSignatureElectronique([this.avenant.id]).subscribe((response: any) => {
      if (response === 1) {
        this.messageService.setSuccess(`Avenant envoyé`);
        this.avenantService.getById(this.avenant.id).subscribe((responseavenant: any) => {
          this.updated.emit(responseavenant);
        });
      }
    });
  }
}
