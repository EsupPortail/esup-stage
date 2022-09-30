import { Component, OnInit, Input } from '@angular/core';
import { PeriodeInterruptionStageService } from "../../../services/periode-interruption-stage.service";
import { ConventionService } from "../../../services/convention.service";
import { MessageService } from "../../../services/message.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import * as FileSaver from 'file-saver';

@Component({
  selector: 'app-recapitulatif',
  templateUrl: './recapitulatif.component.html',
  styleUrls: ['./recapitulatif.component.scss']
})
export class RecapitulatifComponent implements OnInit {

  @Input() convention: any;
  interruptionsStage: any[] = [];
  isValide: boolean = false;
  canPrint: boolean = true;

  constructor(private periodeInterruptionStageService: PeriodeInterruptionStageService,
              private conventionService: ConventionService,
              private messageService: MessageService,
              private authService: AuthService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.isValide = this.convention.validationPedagogique && this.convention.validationConvention && this.convention.nomenclature;
    if(this.convention.interruptionStage){
      this.loadInterruptionsStage();
    }

    if(this.authService.isEtudiant()){
      this.canPrint = false;
      const centreGestion : any = this.convention.centreGestion;
      if(centreGestion.autoriserImpressionConvention){
        if(centreGestion.conditionValidationImpression == 0){
          this.canPrint = true;
        }
        if(centreGestion.conditionValidationImpression == 1 && this.convention.validationPedagogique){
          this.canPrint = true;
        }
        if(centreGestion.conditionValidationImpression == 2 && this.convention.verificationAdministrative){
          this.canPrint = true;
        }
        if(centreGestion.conditionValidationImpression == 3 && this.convention.validationPedagogique && this.convention.verificationAdministrative){
          this.canPrint = true;
        }
      }
    }
  }

  loadInterruptionsStage() : void{
    this.periodeInterruptionStageService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.interruptionsStage = response;
    });
  }

  validate(): void {
    this.conventionService.validationCreation(this.convention.id).subscribe((response: any) => {
      this.messageService.setSuccess('Convention créée avec succès');
      this.router.navigate([`/conventions/${this.convention.id}`], )
    });
  }

  printConvention() : void {
    this.conventionService.getConventionPDF(this.convention.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: "application/pdf"});
      let filename = 'Convention_' + this.convention.id + '_' + this.convention.etudiant.prenom + '_' + this.convention.etudiant.nom + '.pdf';
      FileSaver.saveAs(blob, filename);
    });
  }

}
