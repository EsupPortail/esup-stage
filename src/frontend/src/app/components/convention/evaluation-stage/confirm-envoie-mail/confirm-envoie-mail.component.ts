import {Component, Inject, OnInit} from '@angular/core';
import { TemplateMailService} from "../../../../services/template-mail.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ReponseEvaluationService} from "../../../../services/reponse-evaluation.service";
import {MessageService} from "../../../../services/message.service";
import{ templateMail } from "../../../../models/template-mail.model";
import {ConventionService} from "../../../../services/convention.service";
import {finalize, switchMap} from "rxjs/operators";

@Component({
    selector: 'app-confirm-envoie-mail',
    templateUrl: './confirm-envoie-mail.component.html',
    styleUrl: './confirm-envoie-mail.component.scss',
    standalone: false
})
export class ConfirmEnvoieMailComponent implements OnInit{

  convention: any;
  typeFiche: number; // 0 = étudiant, 1 = tuteur pédagogique, 2 = tuteur professionnel
  templateMail!: templateMail;
  sending: boolean = false;

  constructor(
    public templateMailService: TemplateMailService,
    private dialogRef: MatDialogRef<ConfirmEnvoieMailComponent>,
    private messageService: MessageService,
    private reponseEvaluationService: ReponseEvaluationService,
    private conventionService:ConventionService,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.convention = data.convention;
    this.typeFiche = data.typeFiche;
  }

  ngOnInit(): void {
    console.log(this.typeFiche)
    console.log(this.convention)
    this.templateMailService.getTemplateMailByType(this.typeFiche,this.convention.id).subscribe((response: any) => {
      console.log(response)
      this.templateMail = response;
    });
  }

  send(): void {
    this.sending = true;
    this.reponseEvaluationService.sendMailEvaluation(this.convention.id, this.typeFiche)
      .pipe(
        switchMap(() => this.conventionService.getById(this.convention.id)),
        finalize(() => this.sending = false)
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

  envoiMailEvaluation(typeFiche: number): void {
    this.reponseEvaluationService.sendMailEvaluation(this.convention.id, typeFiche).subscribe((response: any) => {
      this.messageService.setSuccess('Mail envoyé avec succès');
      if (typeFiche == 0)
        this.convention.envoiMailEtudiant = true
      if (typeFiche == 1)
        this.convention.envoiMailTuteurPedago = true
      if (typeFiche == 2)
        this.convention.envoiMailTuteurPro = true
    });
  }


}
