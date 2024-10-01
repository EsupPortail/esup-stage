import { Component, Inject, OnInit} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-tuteur-accueil-groupe-modal',
  templateUrl: './tuteur-accueil-groupe-modal.component.html',
  styleUrls: ['./tuteur-accueil-groupe-modal.component.scss']
})
export class TuteurAccueilGroupeModalComponent implements OnInit {

  contact: any;
  etab: any;
  service: any;
  centreGestion: any;

  constructor(private dialogRef: MatDialogRef<TuteurAccueilGroupeModalComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.contact = data.contact;
    this.etab = data.etab;
    this.service = data.service;
    this.centreGestion = data.centreGestion;
  }

  ngOnInit(): void {
  }

  close(): void {
    this.dialogRef.close(null);
  }

  updateTuteurPro(data: any): void {
    this.dialogRef.close(data.id);
  }
}
