import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
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

  setMessage(message: string, type: string, keep: boolean = true, onClose?: () => void) {
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
      case 'accessDenied':
        this.title = 'Accès refusé'
        break;
    }
    this.message = message;
    if (!keep) {
      setTimeout(() => this.close(), 1000 );
    }
    this.open(onClose);
  }

  setError(message: string, keep: boolean = true): void {
    this.setMessage(message, 'error', keep);
  }

  setSuccess(message: string, keep: boolean = false): void {
    this.setMessage(message, 'success', keep);
  }

  setWarning(message: string, keep: boolean = true, onClose?: () => void): void {
    this.setMessage(message, 'warning', keep, onClose);
  }

  setAccessDenied(message: string, keep: boolean = true): void {
    this.setMessage(message, 'accessDenied', keep);
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

  open(onClose?: () => void): void {
    this.dialogRef = this.dialog.open(MessageComponent, { minWidth: '30%' });
    if (onClose) {
      this.dialogRef.afterClosed().subscribe(() => onClose());
    }
  }

  close(): void {
    this.dialogRef?.close();
  }
}
