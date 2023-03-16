import { Component, Inject, OnInit} from '@angular/core';
import { ConventionService } from "../../../../services/convention.service";
import { GroupeEtudiantService } from "../../../../services/groupe-etudiant.service";
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';

@Component({
  selector: 'app-infos-stage-modal',
  templateUrl: './infos-stage-modal.component.html',
  styleUrls: ['./infos-stage-modal.component.scss']
})
export class InfosStageModalComponent implements OnInit {

  convention: any;
  groupeEtudiant: any;

  constructor(private dialogRef: MatDialogRef<InfosStageModalComponent>,
              private conventionService: ConventionService,
              public groupeEtudiantService: GroupeEtudiantService,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.convention = data.convention;
    this.groupeEtudiant = data.groupeEtudiant;
  }

  ngOnInit(): void {
  }

  validated(status: number): void {
    if(this.groupeEtudiant && status === 2){
      this.groupeEtudiantService.setInfosStageValid(this.groupeEtudiant.id, true).subscribe((response: any) => {});
    }
  }

  updateStage(data: any): void {
    this.conventionService.patch(this.convention.id, data).subscribe((response: any) => {
      this.convention = response;
    });
  }
}
