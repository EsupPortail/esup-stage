import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { finalize, startWith, combineLatest, filter, switchMap, tap } from 'rxjs';

import { TemplateMailService } from '../../../services/template-mail.service';
import { MessageService } from '../../../services/message.service';
import { ReponseEvaluationService } from '../../../services/reponse-evaluation.service';
import { templateMail } from '../../../models/template-mail.model';

type TypeEnvoi = 1 | 2;
type TypeFiche = 0 | 1 | 2; // 0 = étudiant, 1 = enseignant référent, 2 = tuteur pro

@Component({
  selector: 'app-envoi-mail-en-masse-eval',
  templateUrl: './envoi-mail-en-masse-eval.component.html',
  styleUrls: ['./envoi-mail-en-masse-eval.component.scss']
})
export class EnvoiMailEnMasseEvalComponent implements OnInit {

  form!: FormGroup;
  templateMail: templateMail | null = null;
  loadingTemplate = false;
  sending = false;

  constructor(
    private dialogRef: MatDialogRef<EnvoiMailEnMasseEvalComponent>,
    private fb: FormBuilder,
    private templateMailService: TemplateMailService,
    private messageService: MessageService,
    private reponseEvaluationService: ReponseEvaluationService,
  ) {
    // Initialize form in constructor to ensure it's available immediately
    this.initializeForm();
  }

  ngOnInit(): void {
    // Set up form value change listeners
    this.setupFormListeners();
  }

  private initializeForm(): void {
    this.form = this.fb.group({
      // null par défaut, mais required pour le submit
      typeEnvoi: [null as TypeEnvoi | null, Validators.required],
      typeFiche: [null as TypeFiche | null, Validators.required],
    });
  }

  private setupFormListeners(): void {
    // Écoute synchronisée des 2 contrôles
    combineLatest([
      this.form.get('typeEnvoi')!.valueChanges.pipe(startWith(this.form.get('typeEnvoi')!.value)),
      this.form.get('typeFiche')!.valueChanges.pipe(startWith(this.form.get('typeFiche')!.value)),
    ])
      .pipe(
        tap(([te, tf]) => {
          // si un des deux redevient null -> on vide la preview
          if (te == null || tf == null) this.templateMail = null;
        }),
        filter(([te, tf]) => te != null && tf != null),
        tap(() => { this.loadingTemplate = true; this.templateMail = null; }),
        switchMap(([te, tf]) =>
          this.templateMailService.getTemplateMailEvalStage(te as TypeEnvoi, tf as TypeFiche)
            .pipe(finalize(() => (this.loadingTemplate = false)))
        )
      )
      .subscribe({
        next: (templateMail: any) => (this.templateMail = templateMail),
        error: () => {
          this.templateMail = null;
          this.messageService.setError('Erreur lors du chargement du modèle de mail');
        },
      });
  }

  close(): void {
    this.dialogRef.close();
  }

  send(): void {
    if (this.form.valid) {
      this.sending = true;
      const formValue = this.form.value;
      console.log('Sending email with:', formValue);
    } else {
      this.form.markAllAsTouched();
    }
  }
}
