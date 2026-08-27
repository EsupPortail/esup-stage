import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize, forkJoin, map, Observable, of, startWith, switchMap, tap } from 'rxjs';
import { TemplateMailService } from '../../../services/template-mail.service';
import { MessageService } from '../../../services/message.service';
import { ReponseEvaluationService } from '../../../services/reponse-evaluation.service';
import { templateMail } from '../../../models/template-mail.model';
import {CentreGestionService} from "../../../services/centre-gestion.service";

type TypeEnvoi = 0 | 1;
type TypeFiche = 0 | 1 | 2;
type MailMode = 'premier' | 'rappel' | 'mixte';

type TemplatePreviewState = {
  single: templateMail | null;
  premier: templateMail | null;
  rappel: templateMail | null;
};

@Component({
  selector: 'app-envoi-mail-en-masse-eval',
  templateUrl: './envoi-mail-en-masse-eval.component.html',
  styleUrls: ['./envoi-mail-en-masse-eval.component.scss'],
  standalone: false,
})
export class EnvoiMailEnMasseEvalComponent implements OnInit {

  form!: FormGroup;
  templateMail: templateMail | null = null;
  templateMailPremier: templateMail | null = null;
  templateMailRappel: templateMail | null = null;
  loadingTemplate = false;
  sending = false;
  conventionIds!: number[];

  mailMode: MailMode | null = null;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { rows: any[] },
    private dialogRef: MatDialogRef<EnvoiMailEnMasseEvalComponent>,
    private fb: FormBuilder,
    private templateMailService: TemplateMailService,
    private messageService: MessageService,
    private reponseEvaluationService: ReponseEvaluationService,
    private centreGestionService: CentreGestionService,
  ) {
    this.form = this.fb.group({
      typeFiche: [null as TypeFiche | null, Validators.required],
    });
  }

  ngOnInit(): void {
    this.conventionIds = this.data.rows.map((row: any) => row.id);

    this.mailMode = this.computeGlobalMailMode();

    this.setupFormListeners();
  }

  get isSingleSelection(): boolean {
    return this.data.rows.length === 1;
  }

  get modeEnvoiLabel(): string {
    switch (this.mailMode) {
      case 'premier': return 'Premier envoi';
      case 'rappel':  return 'Rappel';
      case 'mixte':   return 'Premier envoi / Rappel';
      default:        return '';
    }
  }

  close(): void {
    this.dialogRef.close();
  }

  send(): void {
    if (!this.form.valid) return;

    const { typeFiche } = this.form.value;
    const idConventions = this.conventionIds;

    this.sending = true;

    const checks$ = this.data.rows.map(row =>
      this.centreGestionService.getById(row.centreGestion.id).pipe(
        map(res => ({
          conventionId: row.id,
          isValid: this.isFicheValidee(res, typeFiche),
        }))
      )
    );

    forkJoin(checks$)
      .pipe(finalize(() => (this.sending = false)))
      .subscribe({
        next: results => {
          const notValideConventions = results
            .filter(r => !r.isValid)
            .map(r => r.conventionId);

          if (notValideConventions.length > 0) {
            this.messageService.setError(
              this.buildValidationWarning(notValideConventions, typeFiche)
            );
            return;
          }

          this.reponseEvaluationService.sendMailEvaluationEnMasse(idConventions, typeFiche)
            .subscribe({
              next: res => this.handleSendSuccess(res, idConventions),
              error: () => this.messageService.setError("Erreur lors de l'envoi des emails"),
            });
        },
        error: () => this.messageService.setError('Erreur lors de la vérification des centres de gestion'),
      });
  }

  private isFicheValidee(res: any, typeFiche: TypeFiche): boolean {
    const fiche = res?.ficheEvaluation;
    switch (typeFiche) {
      case 0: return Boolean(fiche?.validationEtudiant);
      case 1: return Boolean(fiche?.validationEnseignant);
      case 2: return Boolean(fiche?.validationEntreprise);
      default: return false;
    }
  }

  private buildValidationWarning(notValideConventions: number[], typeFiche: TypeFiche): string {
    const count = notValideConventions.length;
    const ids = notValideConventions.join(', ');

    const destinataire = this.getDestinaireLabel(typeFiche);

    if (count === 1) {
      return `Envoi impossible. La convention ${ids} n'a pas de questionnaire ${destinataire} validé.`;
    }

    return `Envoi impossible. Les ${count} conventions suivantes n'ont pas de questionnaire ${destinataire} validé : ${ids}`;
  }

  private getDestinaireLabel(typeFiche: TypeFiche): string {
    switch (typeFiche) {
      case 0: return 'étudiant';
      case 1: return 'enseignant';
      case 2: return 'tuteur';
      default: return '';
    }
  }

  private setupFormListeners(): void {
    this.form.get('typeFiche')!.valueChanges
      .pipe(
        startWith(this.form.get('typeFiche')!.value),
        tap((typeFiche: TypeFiche | null) => {
          if (typeFiche == null) {
            this.resetTemplatePreview();
            return;
          }
          this.mailMode = this.computeMailMode(typeFiche);
          this.loadingTemplate = true;
          this.templateMail = null;
          this.templateMailPremier = null;
          this.templateMailRappel = null;
        }),
        switchMap((typeFiche: TypeFiche | null) => {
          if (typeFiche == null) {
            return of({ single: null, premier: null, rappel: null } as TemplatePreviewState);
          }
          return this.loadTemplatePreview(typeFiche, this.mailMode!)
            .pipe(finalize(() => (this.loadingTemplate = false)));
        }),
      )
      .subscribe({
        next: ({ single, premier, rappel }: TemplatePreviewState) => {
          this.templateMail = single;
          this.templateMailPremier = premier;
          this.templateMailRappel = rappel;
        },
        error: () => {
          this.resetTemplatePreview();
          this.messageService.setError('Erreur lors du chargement du modèle de mail');
        },
      });
  }

  /**
   * Calcule le mailMode global (avant sélection de typeFiche),
   * en vérifiant si au moins un envoi a déjà eu lieu toutes fiches confondues.
   */
  private computeGlobalMailMode(): MailMode {
    const allTypeFiches: TypeFiche[] = [0, 1, 2];

    const hasAnyRappel = allTypeFiches.some(typeFiche =>
      this.data.rows.some(row => this.hasMailAlreadyBeenSent(row, typeFiche))
    );
    const hasAnyPremier = allTypeFiches.some(typeFiche =>
      this.data.rows.some(row => !this.hasMailAlreadyBeenSent(row, typeFiche))
    );

    if (hasAnyRappel && hasAnyPremier) return 'mixte';
    return hasAnyRappel ? 'rappel' : 'premier';
  }

  /**
   * Calcule le mailMode pour une typeFiche donnée.
   */
  private computeMailMode(typeFiche: TypeFiche): MailMode {
    const hasRappel = this.data.rows.some(row => this.hasMailAlreadyBeenSent(row, typeFiche));
    const hasPremier = this.data.rows.some(row => !this.hasMailAlreadyBeenSent(row, typeFiche));

    if (hasRappel && hasPremier) return 'mixte';
    return hasRappel ? 'rappel' : 'premier';
  }

  private hasMailAlreadyBeenSent(row: any, typeFiche: TypeFiche): boolean {
    const { mailKey, dateKey } = this.getMailKeys(typeFiche);
    return Boolean(row?.[mailKey] || row?.[dateKey]);
  }

  private getMailKeys(typeFiche: TypeFiche): { mailKey: string; dateKey: string } {
    const keys: Record<TypeFiche, { mailKey: string; dateKey: string }> = {
      0: { mailKey: 'envoiMailEtudiant',       dateKey: 'dateEnvoiMailEtudiant' },
      1: { mailKey: 'envoiMailTuteurPedago',   dateKey: 'dateEnvoiMailTuteurPedago' },
      2: { mailKey: 'envoiMailTuteurPro',      dateKey: 'dateEnvoiMailTuteurPro' },
    };
    return keys[typeFiche] ?? { mailKey: '', dateKey: '' };
  }

  private loadTemplatePreview(typeFiche: TypeFiche, mailMode: MailMode): Observable<TemplatePreviewState> {
    if (this.isSingleSelection) {
      return this.templateMailService.getTemplateMailByType(typeFiche, this.conventionIds[0]).pipe(
        map(single => ({ single, premier: null, rappel: null }))
      );
    }

    if (mailMode === 'mixte') {
      return forkJoin({
        premier: this.templateMailService.getTemplateMailEvalStage(0, typeFiche),
        rappel:  this.templateMailService.getTemplateMailEvalStage(1, typeFiche),
      }).pipe(
        map(({ premier, rappel }) => ({ single: null, premier, rappel }))
      );
    }

    const typeEnvoi: TypeEnvoi = mailMode === 'rappel' ? 1 : 0;
    return this.templateMailService.getTemplateMailEvalStage(typeEnvoi, typeFiche).pipe(
      map(single => ({ single, premier: null, rappel: null }))
    );
  }

  private resetTemplatePreview(): void {
    this.templateMail = null;
    this.templateMailPremier = null;
    this.templateMailRappel = null;
    this.loadingTemplate = false;
    this.mailMode = this.computeGlobalMailMode();
  }

  private handleSendSuccess(res: any, idConventions: number[]): void {
    const summary = res?.summary ?? res?.resume ?? {};
    const rows = res?.rows ?? res?.conventions ?? [];

    const requested = summary.requested ?? idConventions.length;
    const sent = summary.sent ?? 0;
    const failed = summary.failed ?? rows.filter((r: any) => r?.status === 'ERROR').length;

    const errorIds: number[] = rows
      .filter((r: any) => r?.status === 'ERROR')
      .map((r: any) => r?.conventionId)
      .filter((id: any) => id != null);

    if (failed === 0 && sent > 0) {
      this.messageService.setSuccess(`${sent}/${requested} email(s) envoyés avec succès.`);
      this.dialogRef.close({ summary, rows });
    } else if (sent > 0 && failed > 0) {
      const limit = 15;
      const shown = errorIds.slice(0, limit);
      const more = errorIds.length > limit ? `, ... (+${errorIds.length - limit})` : '';
      this.messageService.setWarning(
        `${sent}/${requested} envoyés. Erreurs sur les conventions : ${shown.join(', ')}${more}`
      );
    } else {
      this.messageService.setError(`Aucun email envoyé. ${failed} erreur(s).`);
    }
  }
}
