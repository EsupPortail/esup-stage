import { Component, Inject, OnInit} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
    selector: 'app-enseignant-groupe-modal',
    templateUrl: './enseignant-groupe-modal.component.html',
    styleUrls: ['./enseignant-groupe-modal.component.scss'],
    standalone: false
})
export class EnseignantGroupeModalComponent implements OnInit {

  enseignant: any;

  constructor(private dialogRef: MatDialogRef<EnseignantGroupeModalComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.enseignant = data.enseignant;
  }

  ngOnInit(): void {
  }

  close(): void {
    this.dialogRef.close(null);
  }

  updateEnseignant(data: any): void {
    this.dialogRef.close(data.id);
  }
}
