import { Component, OnInit, Inject } from '@angular/core';
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-confirm-delete-centre',
  templateUrl: './confirm-delete-centre.component.html',
  styleUrls: ['./confirm-delete-centre.component.scss']
})
export class ConfirmDeleteCentreComponent implements OnInit {

  centreGestionId: any;
  centreGestion: any;
  conventionsCount: number = 0;
  contactsCount: number = 0;
  criteresCount: number = 0;
  personnelsCount: number = 0;

  constructor(public centreGestionService: CentreGestionService,
              private dialogRef: MatDialogRef<ConfirmDeleteCentreComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.centreGestionId = data.centreGestionId
  }

  ngOnInit(): void {

    this.centreGestionService.getById(this.centreGestionId).subscribe(response => {
        this.centreGestion = response;
        this.personnelsCount = response.personnels.length;

    });
    this.centreGestionService.countCritereWithCentre(this.centreGestionId).subscribe(response => {
        this.criteresCount = response;
    });
    this.centreGestionService.countConventionWithCentre(this.centreGestionId).subscribe(response => {
        this.conventionsCount = response;
    });
    this.centreGestionService.countContactWithCentre(this.centreGestionId).subscribe(response => {
        this.contactsCount = response;
    });
  }

  cancel(): void {
    this.dialogRef.close(null);
  }

  valid(): void {
    this.dialogRef.close(true);
  }

}
