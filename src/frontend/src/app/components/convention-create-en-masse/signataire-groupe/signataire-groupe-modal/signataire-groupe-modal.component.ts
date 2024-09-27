import { Component, Inject, OnInit} from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';

@Component({
  selector: 'app-signataire-groupe-modal',
  templateUrl: './signataire-groupe-modal.component.html',
  styleUrls: ['./signataire-groupe-modal.component.scss']
})
export class SignataireGroupeModalComponent implements OnInit {

  convention: any;

  constructor(private dialogRef: MatDialogRef<SignataireGroupeModalComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.convention = data.convention;
  }

  ngOnInit(): void {
  }

  close(): void {
    this.dialogRef.close(null);
  }

  updateSignataire(data: any): void {
    this.dialogRef.close(data.id);
  }
}


