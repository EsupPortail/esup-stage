import { Component, Output, EventEmitter, OnInit, Input } from '@angular/core';
import { AuthService } from "../../../../services/auth.service";
import { MessageService } from "../../../../services/message.service";
import { AvenantService } from "../../../../services/avenant.service";
import { PeriodeInterruptionStageService } from "../../../../services/periode-interruption-stage.service";
import { PeriodeInterruptionAvenantService } from "../../../../services/periode-interruption-avenant.service";

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

  constructor(private authService: AuthService,
              private messageService: MessageService,
              private avenantService: AvenantService,
              private periodeInterruptionStageService: PeriodeInterruptionStageService,
              private periodeInterruptionAvenantService: PeriodeInterruptionAvenantService,
  ) { }

  ngOnInit(): void {
    this.loadInterruptionsStage();
  }

  isEtudiant(): boolean {
    return this.authService.isEtudiant();
  }

  isGestionnaire(): boolean {
    return this.authService.isGestionnaire();
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
          console.log('interruption : ' + JSON.stringify(interruption, null, 2));
          this.modifiedInterruptionsStage.push(interruption);
        }else{
          this.addedInterruptionsStage.push(interruption);
        }
      }
    });
  }
}
