import { Component, Inject, OnInit} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
    selector: 'app-etab-accueil-groupe-modal',
    templateUrl: './etab-accueil-groupe-modal.component.html',
    styleUrls: ['./etab-accueil-groupe-modal.component.scss'],
    standalone: false
})
export class EtabAccueilGroupeModalComponent implements OnInit {

  etab: any;

  constructor(private dialogRef: MatDialogRef<EtabAccueilGroupeModalComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.etab = data.etab;
  }

  ngOnInit(): void {
  }

  close(): void {
    this.dialogRef.close(null);
  }

  updateEtab(data: any): void {
    this.dialogRef.close(data.id);
  }
}
