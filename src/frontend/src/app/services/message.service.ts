import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { MessageComponent } from "../components/message/message.component";

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  message: string = '';
  type: string = '';
  title: string = '';
  interval: number = 0;
  dialogRef: MatDialogRef<any>|undefined = undefined;

  constructor(private dialog: MatDialog) { }

  setMessage(message: string, type: string, keep: boolean = true) {
    this.type = type;
    switch (this.type) {
      case 'error':
        this.title = 'Erreur'
        break;
      case 'success':
        this.title = 'Succès'
        break;
      case 'warning':
        this.title = 'Attention'
        break;
    }
    this.message = message;
    if (!keep) {
      setTimeout(() => this.close(), 5000 );
    }
    this.open();
  }

  setError(message: string, keep: boolean = true): void {
    this.setMessage(message, 'error', keep);
  }

  setSuccess(message: string, keep: boolean = false): void {
    this.setMessage(message, 'success', keep);
  }

  setWarning(message: string, keep: boolean = true): void {
    this.setMessage(message, 'warning', keep);
  }

  getType(): string {
    return this.type;
  }

  getTitle(): string {
    return this.title;
  }

  getMessage(): string {
    return this.message;
  }

  open(): void {
    this.dialogRef = this.dialog.open(MessageComponent, { minWidth: '30%' });
  }

  close(): void {
    this.dialogRef?.close();
  }
}
