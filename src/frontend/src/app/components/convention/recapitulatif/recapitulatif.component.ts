import { Component, OnInit, Input } from '@angular/core';
import { PeriodeInterruptionStageService } from "../../../services/periode-interruption-stage.service";
import { ConventionService } from "../../../services/convention.service";
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

  constructor(private periodeInterruptionStageService: PeriodeInterruptionStageService, private conventionService: ConventionService) {
  }

  ngOnInit(): void {
    this.isValide = this.convention.validationPedagogique && this.convention.validationConvention;
    if(this.convention.interruptionStage){
      this.loadInterruptionsStage();
    }
  }

  loadInterruptionsStage() : void{
    this.periodeInterruptionStageService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.interruptionsStage = response;
    });
  }

  validate(): void {
    //TODO send mail
  }

  printConvention() : void {
    this.conventionService.getConventionPDF(this.convention.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: "application/pdf"});
      let filename = 'convention_' + this.convention.id + '_' + this.convention.etudiant.prenom + '_' + this.convention.etudiant.nom + '.pdf';
      FileSaver.saveAs(blob, filename);
    });
  }

}
