import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-confirmation-popup',
  templateUrl: './confirmation-popup.component.html',
  styleUrls: ['./confirmation-popup.component.scss']
})
export class ConfirmationPopupComponent implements OnInit {

  title = "Confirmation";
  message;

  constructor(private dialogRef: MatDialogRef<ConfirmationPopupComponent>, @Inject(MAT_DIALOG_DATA) data: any) {
    this.message = data;
  }

  ngOnInit(): void {
  }

  onConfirm() {
    this.dialogRef.close(true);
  }

  onDismiss() {
    this.dialogRef.close(false);
  }

}
