import { Component, Inject, OnInit} from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';

@Component({
  selector: 'app-service-accueil-groupe-modal',
  templateUrl: './service-accueil-groupe-modal.component.html',
  styleUrls: ['./service-accueil-groupe-modal.component.scss']
})
export class ServiceAccueilGroupeModalComponent implements OnInit {

  etab: any;
  service: any;
  centreGestion: any;

  constructor(private dialogRef: MatDialogRef<ServiceAccueilGroupeModalComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.etab = data.etab;
    this.service = data.service;
    this.centreGestion = data.centreGestion;
  }

  ngOnInit(): void {
  }

  close(): void {
    this.dialogRef.close(null);
  }

  updateService(data: any): void {
    this.dialogRef.close(data.id);
  }
}

