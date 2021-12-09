import { Component, OnInit, Input } from '@angular/core';
import { PeriodeInterruptionStageService } from "../../../services/periode-interruption-stage.service";
import { ConventionService } from "../../../services/convention.service";
import { MessageService } from "../../../services/message.service";
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

  constructor(private periodeInterruptionStageService: PeriodeInterruptionStageService,
              private conventionService: ConventionService,
              private messageService: MessageService,
              private router: Router) {
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
    this.conventionService.validationCreation(this.convention.id).subscribe((response: any) => {
      this.messageService.setSuccess('Convention créée avec succès');
      this.router.navigate([`/conventions/${this.convention.id}`], )
    });
  }

  printConvention() : void {
    this.conventionService.getConventionPDF(this.convention.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: "application/pdf"});
      let filename = 'convention_' + this.convention.id + '_' + this.convention.etudiant.prenom + '_' + this.convention.etudiant.nom + '.pdf';
      FileSaver.saveAs(blob, filename);
    });
  }

  printAvenant() : void {
    this.conventionService.getAvenantPDF(this.convention.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: "application/pdf"});
      let filename = 'avenant_' + this.convention.id + '_' + this.convention.etudiant.prenom + '_' + this.convention.etudiant.nom + '.pdf';
      FileSaver.saveAs(blob, filename);
    });
  }

}
