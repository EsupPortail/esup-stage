import { Component, Inject, OnInit} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-service-accueil-groupe-modal',
  templateUrl: './service-accueil-groupe-modal.component.html',
  styleUrls: ['./service-accueil-groupe-modal.component.scss']
})
export class ServiceAccueilGroupeModalComponent implements OnInit {

  convention: any;

  constructor(private dialogRef: MatDialogRef<ServiceAccueilGroupeModalComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.convention = data.convention;
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

