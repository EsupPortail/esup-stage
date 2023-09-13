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
  tmpConvention: any;
  interruptionsStage: any[] = [];
  canPrint: boolean = true;

  constructor(private periodeInterruptionStageService: PeriodeInterruptionStageService,
              private conventionService: ConventionService,
              private messageService: MessageService,
              private authService: AuthService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.tmpConvention = {
      ...this.convention,
      langueConventionLibelle: this.getNomenclatureValue('langueConvention'),
      typeConventionLibelle: this.getNomenclatureValue('typeConvention'),
      themeLibelle: this.getNomenclatureValue('theme'),
      tempsTravailLibelle: this.getNomenclatureValue('tempsTravail'),
      uniteGratificationLibelle: this.getNomenclatureValue('uniteGratification'),
      uniteDureeGratificationLibelle: this.getNomenclatureValue('uniteDureeGratification'),
      deviseLibelle: this.getNomenclatureValue('devise'),
      modeVersGratificationLibelle: this.getNomenclatureValue('modeVersGratification'),
      origineStageLibelle: this.getNomenclatureValue('origineStage'),
      natureTravailLibelle: this.getNomenclatureValue('natureTravail'),
      modeValidationStageLibelle: this.getNomenclatureValue('modeValidationStage'),
    };
    if(this.tmpConvention.interruptionStage){
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
        if(centreGestion.conditionValidationImpression == 2 && this.convention.validationConvention){
          this.canPrint = true;
        }
        if(centreGestion.conditionValidationImpression == 3 && this.convention.validationPedagogique && this.convention.validationConvention){
          this.canPrint = true;
        }
        if(centreGestion.conditionValidationImpression == 4 && this.convention.verificationAdministrative){
          this.canPrint = true;
        }
      }
    }
  }

  getNomenclatureValue(key: string) {
    if (this.convention.validationCreation && this.convention.nomenclature) {
      return this.convention.nomenclature[key] ?? '';
    }
    return this.convention[key] ? this.convention[key].libelle : '';
  }

  loadInterruptionsStage() : void{
    this.periodeInterruptionStageService.getByConvention(this.tmpConvention.id).subscribe((response: any) => {
      this.interruptionsStage = response;
    });
  }

  validate(): void {
    this.conventionService.validationCreation(this.tmpConvention.id).subscribe((response: any) => {
      this.messageService.setSuccess('Convention créée avec succès');
      this.router.navigate([`/conventions/${this.tmpConvention.id}`], )
    });
  }

  printConvention(isRecap : boolean) : void {
    this.conventionService.getConventionPDF(this.tmpConvention.id, isRecap).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: "application/pdf"});
      let filename = 'Convention_' + this.tmpConvention.id + '_' + this.tmpConvention.etudiant.prenom + '_' + this.tmpConvention.etudiant.nom + '.pdf';
      FileSaver.saveAs(blob, filename);
    });
  }

}
