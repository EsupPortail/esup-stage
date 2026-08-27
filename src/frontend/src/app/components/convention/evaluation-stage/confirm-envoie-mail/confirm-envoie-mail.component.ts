import { Component, Inject, OnInit } from '@angular/core';
import { TemplateMailService } from '../../../../services/template-mail.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ReponseEvaluationService } from '../../../../services/reponse-evaluation.service';
import { MessageService } from '../../../../services/message.service';
import { templateMail } from '../../../../models/template-mail.model';
import { ConventionService } from '../../../../services/convention.service';
import { finalize, switchMap } from 'rxjs/operators';

@Component({
    selector: 'app-confirm-envoie-mail',
    templateUrl: './confirm-envoie-mail.component.html',
    styleUrl: './confirm-envoie-mail.component.scss',
    standalone: false
})
export class ConfirmEnvoieMailComponent implements OnInit {

  convention: any;
  typeFiche: number;
  templateMail: templateMail | null = null;
  sending = false;
  loadingTemplate = false;

  constructor(
    public templateMailService: TemplateMailService,
    private dialogRef: MatDialogRef<ConfirmEnvoieMailComponent>,
    private messageService: MessageService,
    private reponseEvaluationService: ReponseEvaluationService,
    private conventionService: ConventionService,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.convention = data.convention;
    this.typeFiche = data.typeFiche;
  }

  ngOnInit(): void {
    this.loadingTemplate = true;
    this.templateMailService.getTemplateMailByType(this.typeFiche, this.convention.id)
      .pipe(finalize(() => (this.loadingTemplate = false)))
      .subscribe({
        next: (response: templateMail) => {
          this.templateMail = response;
        },
        error: () => {
          this.templateMail = null;
          this.messageService.setError('Erreur lors du chargement du modèle de mail');
        }
      });
  }

  send(): void {
    if (!this.templateMail) {
      return;
    }

    this.sending = true;
    this.reponseEvaluationService.sendMailEvaluation(this.convention.id, this.typeFiche)
      .pipe(
        switchMap(() => this.conventionService.getById(this.convention.id)),
        finalize(() => (this.sending = false))
      )
      .subscribe({
        next: (updatedConvention) => {
          this.messageService.setSuccess('Mail envoyé avec succès');
          this.dialogRef.close({ convention: updatedConvention });
        },
        error: () => {
          this.messageService.setError('Erreur lors de l\'envoi du mail');
          this.dialogRef.close(null);
        }
      });
  }

  close(): void {
    this.dialogRef.close(null);
  }
}