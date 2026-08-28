import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MessageService } from "../../../../services/message.service";
import { copyToClipboard } from "../../../../utils/clipboard.utils";

export interface ApiTokenValueDialogData {
  /** Titre de la boîte : varie selon création, renouvellement ou simple consultation. */
  titre: string;
  nomApplication: string;
  token: string;
}

/**
 * Affiche la valeur en clair d'un token et permet de la copier dans le presse-papier.
 */
@Component({
  selector: 'app-api-token-value-dialog',
  templateUrl: './api-token-value-dialog.component.html',
  styleUrls: ['./api-token-value-dialog.component.scss'],
  standalone: false
})
export class ApiTokenValueDialogComponent {

  visible = false;
  /** Confirmation discrète affichée dans la boîte, sans empiler une seconde modale. */
  copie = false;

  constructor(
    private dialogRef: MatDialogRef<ApiTokenValueDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ApiTokenValueDialogData,
    private messageService: MessageService,
  ) {}

  toggleVisible(): void {
    this.visible = !this.visible;
  }

  get valeurAffichee(): string {
    return this.visible ? this.data.token : '•'.repeat(this.data.token?.length ?? 0);
  }

  copier(): void {
    copyToClipboard(this.data.token).then(succes => {
      if (succes) {
        this.copie = true;
        setTimeout(() => (this.copie = false), 1800);
      } else {
        this.messageService.setError('La copie automatique a échoué, sélectionnez la valeur manuellement');
      }
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
