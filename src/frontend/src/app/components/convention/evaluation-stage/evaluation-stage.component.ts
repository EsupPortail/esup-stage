import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators
} from "@angular/forms";
import { MatDialog, MatDialogConfig } from "@angular/material/dialog";
import { MatExpansionPanel } from "@angular/material/expansion";
import { forkJoin, Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
import * as FileSaver from 'file-saver';
import { ReponseEvaluationService } from "../../../services/reponse-evaluation.service";
import { FicheEvaluationService } from "../../../services/fiche-evaluation.service";
import { MessageService } from "../../../services/message.service";
import { AuthService } from "../../../services/auth.service";
import { QuestionsEvaluationService } from "../../../services/questions-evaluation.service";
import { ConfirmEnvoieMailComponent } from "./confirm-envoie-mail/confirm-envoie-mail.component";
import { DbQuestion } from "../../../models/question-evaluation.model";
import { TypeQuestionEvaluation } from "../../../constants/type-question-evaluation";
import { MAX_LENTGH_INPUT } from "../../../constants/max-length-input";

enum FicheType { Etudiant = 0, Enseignant = 1, Entreprise = 2 }

@Component({
    selector: 'app-evaluation-stage',
    templateUrl: './evaluation-stage.component.html',
    styleUrls: ['./evaluation-stage.component.scss'],
    standalone: false
})
export class EvaluationStageComponent implements OnInit, OnDestroy {
  readonly MAX_LENTGH_INPUT = MAX_LENTGH_INPUT;

  // ---- Inputs / Outputs ----
  @Input() convention: any;
  @Output() conventionChange = new EventEmitter<any>();
  @ViewChild("generalPanel") generalPanel: MatExpansionPanel | undefined;

  // ---- State ----
  ficheEvaluation: any;
  reponseEvaluation: any;
  questionsSupplementaires: any;

  isEtudiant = false;
  isEnseignant = false;
  isGestionnaireOrAdmin = false;

  reponseEtudiantForm: FormGroup;
  reponseEnseignantForm: FormGroup;
  reponseEntrepriseForm: FormGroup;

  reponseSupplementaireEtudiantForm: FormGroup;
  reponseSupplementaireEnseignantForm: FormGroup;
  reponseSupplementaireEntrepriseForm: FormGroup;

  FicheEtudiantIQuestions: DbQuestion[] = [];
  FicheEtudiantIIQuestions: DbQuestion[] = [];
  FicheEtudiantIIIQuestions: DbQuestion[] = [];

  edit: boolean = false;

  FicheEnseignantIQuestions: DbQuestion[] = [];
  FicheEnseignantIIQuestions: DbQuestion[] = [];

  FicheEntrepriseIQuestions: DbQuestion[] = [];
  FicheEntrepriseIIQuestions: DbQuestion[] = [];
  FicheEntrepriseIIIQuestions: DbQuestion[] = [];

  trackByCode = (_index: number, item: { code?: string }) => item?.code ?? _index;


  private destroy$ = new Subject<void>();
  private readonly LIKERT_5 = ['Excellent','Très bien','Bien','Satisfaisant','Insuffisant'];
  private readonly AGREEMENT_5 = ['Tout à fait d\'accord','Plutôt d\'accord','Sans avis','Plutôt pas d\'accord','Pas du tout d\'accord'];
  private readonly FALLBACK_ETUI5 = ['Réponse à une offre de stage', 'Candidature spontanée', 'Réseau de connaissance', 'Proposé par le département'];
  readonly controlsIndexToLetter = ['a','b','c','d','e','f','g','h'];
  protected readonly TypeQuestionEvaluation = TypeQuestionEvaluation;
  readonly FicheType = FicheType;
  private optionsETUI5: string[] = [...this.FALLBACK_ETUI5];
  /** Correspondance contrôle → intitulé affiché (message d'incomplétude). */
  private controlLabels = new Map<string, string>();



  constructor(
    private reponseEvaluationService: ReponseEvaluationService,
    private ficheEvaluationService: FicheEvaluationService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private authService: AuthService,
    private matDialog: MatDialog,
    private questionsEvaluationService: QuestionsEvaluationService,
  ) {
    // Contrôles créés sans validateur : syncValidators est la seule source d'obligation.
    this.reponseEtudiantForm = this.fb.group({
      reponseEtuI1: [null],
      reponseEtuI1bis: [null],
      reponseEtuI2: [null],
      reponseEtuI3: [null],
      reponseEtuI4a: [null],
      reponseEtuI4b: [null],
      reponseEtuI4c: [null],
      reponseEtuI4d: [null],
      reponseEtuI5: [null],
      reponseEtuI6: [null],
      reponseEtuI7: [null],
      reponseEtuI7bis1: [null],
      reponseEtuI7bis1a: [null],
      reponseEtuI7bis1b: [null],
      reponseEtuI7bis2: [null],
      reponseEtuI8: [null],
      reponseEtuII1: [null],
      reponseEtuII1bis: [null],
      reponseEtuII2: [null],
      reponseEtuII2bis: [null],
      reponseEtuII3: [null],
      reponseEtuII3bis: [null],
      reponseEtuII4: [null],
      reponseEtuII5: [null],
      reponseEtuII5a: [null],
      reponseEtuII5b: [null],
      reponseEtuII6: [null],
      reponseEtuIII1: [null],
      reponseEtuIII1bis: [null],
      reponseEtuIII2: [null],
      reponseEtuIII2bis: [null],
      reponseEtuIII4: [null],
      reponseEtuIII5a: [null],
      reponseEtuIII5b: [null],
      reponseEtuIII5c: [null],
      reponseEtuIII5bis: [null],
      reponseEtuIII6: [null],
      reponseEtuIII6bis: [null],
      reponseEtuIII7: [null],
      reponseEtuIII7bis: [null],
      reponseEtuIII8: [null],
      reponseEtuIII8bis: [null],
      reponseEtuIII9: [null],
      reponseEtuIII9bis: [null],
      reponseEtuIII10: [null],
      reponseEtuIII11: [null],
      reponseEtuIII12: [null],
      reponseEtuIII14: [null],
      reponseEtuIII15: [null],
      reponseEtuIII15bis: [null],
      reponseEtuIII16: [null],
      reponseEtuIII16bis: [null],
    });

    this.reponseEnseignantForm = this.fb.group({
      reponseEnsI1a: [null],
      reponseEnsI1b: [null],
      reponseEnsI1c: [null],
      reponseEnsI2a: [null],
      reponseEnsI2b: [null],
      reponseEnsI2c: [null],
      reponseEnsI3: [null],
      reponseEnsII1: [null],
      reponseEnsII10: [null],
      reponseEnsII11: [null],
      reponseEnsII2: [null],
      reponseEnsII3: [null],
      reponseEnsII4: [null],
      reponseEnsII5: [null],
      reponseEnsII6: [null],
      reponseEnsII7: [null],
      reponseEnsII8: [null],
      reponseEnsII9: [null],
    });

    this.reponseEntrepriseForm = this.fb.group({
      reponseEnt1: [null],
      reponseEnt1bis: [null],
      reponseEnt2: [null],
      reponseEnt2bis: [null],
      reponseEnt3: [null],
      reponseEnt4: [null],
      reponseEnt4bis: [null],
      reponseEnt5: [null],
      reponseEnt5bis: [null],
      reponseEnt6: [null],
      reponseEnt6bis: [null],
      reponseEnt7: [null],
      reponseEnt7bis: [null],
      reponseEnt8: [null],
      reponseEnt8bis: [null],
      reponseEnt9: [null],
      reponseEnt9bis: [null],
      reponseEnt10: [null],
      reponseEnt10bis: [null],
      reponseEnt11: [null],
      reponseEnt11bis: [null],
      reponseEnt12: [null],
      reponseEnt12bis: [null],
      reponseEnt13: [null],
      reponseEnt13bis: [null],
      reponseEnt14: [null],
      reponseEnt14bis: [null],
      reponseEnt15: [null],
      reponseEnt15bis: [null],
      reponseEnt16: [null],
      reponseEnt16bis: [null],
      reponseEnt17: [null],
      reponseEnt17bis: [null],
      reponseEnt18: [null],
      reponseEnt18bis: [null],
      reponseEnt19: [null],
    });

    this.reponseSupplementaireEtudiantForm = this.fb.group({});
    this.reponseSupplementaireEnseignantForm = this.fb.group({});
    this.reponseSupplementaireEntrepriseForm = this.fb.group({});
  }

  ngOnInit(): void {
    this.isEtudiant = this.authService.isEtudiant();
    this.isEnseignant = this.authService.isEnseignant();
    this.isGestionnaireOrAdmin = this.authService.isGestionnaire() || this.authService.isAdmin();

    forkJoin({
      etu: this.questionsEvaluationService.getQuestionsEtu(),
      ens: this.questionsEvaluationService.getQuestionsEns(),
      ent: this.questionsEvaluationService.getQuestionsEnt(),
      fiche: this.ficheEvaluationService.getByCentreGestion(this.convention.centreGestion.id),
      rep: this.reponseEvaluationService.getByConvention(this.convention.id)
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ etu, ens, ent, fiche, rep }) => {
        this.ficheEvaluation = fiche;

        this.applyDbQuestions(etu, this.FicheEtudiantIQuestions, this.FicheEtudiantIIQuestions, this.FicheEtudiantIIIQuestions);
        this.applyDbQuestions(ens, this.FicheEnseignantIQuestions, this.FicheEnseignantIIQuestions);
        this.applyDbQuestions(ent, this.FicheEntrepriseIQuestions, this.FicheEntrepriseIIQuestions, this.FicheEntrepriseIIIQuestions);

        this.wireValidatorSync();
        this.syncAllValidators();

        this.reponseEvaluation = rep ?? null;
        if (rep) {
          const repToPatch = { ...rep };
          const schemaVersion = rep.schemaVersion == null || rep.schemaVersion < 2 ? 1 : rep.schemaVersion;
          if (schemaVersion < 2) {
            repToPatch.reponseEtuI7bis1 = null;
            repToPatch.reponseEtuI7bis2 = null;
          } else {
            repToPatch.reponseEtuI7bis1 = this.coerceIndex(repToPatch.reponseEtuI7bis1);
            repToPatch.reponseEtuI7bis2 = this.coerceIndex(repToPatch.reponseEtuI7bis2);
          }
          this.reponseEtudiantForm.patchValue(repToPatch);
          this.reponseEnseignantForm.patchValue(rep);
          this.reponseEntrepriseForm.patchValue(rep);
        }
        // patchValue émet valueChanges, mais on resynchronise pour les questions sans contrôle principal (BOOLEAN_GROUP)
        this.syncAllValidators();
        this.getQuestionSupplementaire();
        this.setAutoEtuI5();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ---------------- Helpers génériques ----------------

  private getFormByType(type: FicheType): FormGroup {
    switch (type) {
      case FicheType.Etudiant: return this.reponseEtudiantForm;
      case FicheType.Enseignant: return this.reponseEnseignantForm;
      case FicheType.Entreprise: return this.reponseEntrepriseForm;
    }
  }

  // ---------------- Sauvegarde ----------------

  saveReponse(typeFiche: FicheType): void {
    const form = this.getFormByType(typeFiche);
    const supplForm = typeFiche === FicheType.Etudiant
      ? this.reponseSupplementaireEtudiantForm
      : typeFiche === FicheType.Enseignant
        ? this.reponseSupplementaireEnseignantForm
        : this.reponseSupplementaireEntrepriseForm;

    const supplBuckets = {
      [FicheType.Etudiant]: [0, 1, 2],
      [FicheType.Enseignant]: [3, 4],
      [FicheType.Entreprise]: [5, 6, 7]
    } as const;

    const valid = form.valid && supplForm.valid;
    const data = { ...form.value };
    const invalidMain = valid ? [] : this.listInvalidControls(form);
    const invalidSuppl = valid ? [] : this.listInvalidControls(supplForm, 'suppl');
    const invalidPaths = [...invalidMain, ...invalidSuppl].map(x => x.path);

    if (!valid) {
      console.groupCollapsed(
        `%c[EvaluationStage] Champs invalides (type=${FicheType[typeFiche]})`,
        'color:#d32f2f;font-weight:bold;'
      );

      const pretty = (arr: Array<{ path: string; errors: any; value: any }>) =>
        arr.map(x => ({
          path: x.path,
          errors: x.errors,
          value: x.value
        }));

      console.warn('Form principal INVALID =>', pretty(invalidMain));
      console.warn('Form suppl. INVALID =>', pretty(invalidSuppl));
      console.groupEnd();
    }

    // Réponses supplémentaires
    const ids = (supplBuckets[typeFiche] as any as number[])
      .flatMap(idx => this.questionsSupplementaires?.[idx] ?? []);

    for (const qs of ids) {
      const payload: any = { reponseTxt: null, reponseInt: null, reponseBool: null };
      if (qs.typeQuestion === 'txt') payload.reponseTxt = supplForm.get(qs.formControlName)?.value ?? null;
      if (qs.typeQuestion === 'not') payload.reponseInt = supplForm.get(qs.formControlName)?.value ?? null;
      if (qs.typeQuestion === 'yn')  payload.reponseBool = supplForm.get(qs.formControlName)?.value ?? null;

      const upsert$ = qs.reponse
        ? this.reponseEvaluationService.updateReponseSupplementaire(this.convention.id, qs.id, payload)
        : this.reponseEvaluationService.createReponseSupplementaire(this.convention.id, qs.id, payload);

      upsert$.pipe(takeUntil(this.destroy$)).subscribe(); // fire & forget
    }

    const onDone = (response: any) => {
      this.reponseEvaluation = response;
      if (valid) {
        this.messageService.setSuccess('Evaluation enregistrée avec succès');
      } else {
        this.messageService.setWarning(this.buildIncompleteWarning(invalidPaths));
      }
    };

    const calls = {
      [FicheType.Etudiant]: () => this.reponseEvaluation
        ? this.reponseEvaluationService.updateReponseEtudiant(this.convention.id, valid, data)
        : this.reponseEvaluationService.createReponseEtudiant(this.convention.id, valid, data),
      [FicheType.Enseignant]: () => this.reponseEvaluation
        ? this.reponseEvaluationService.updateReponseEnseignant(this.convention.id, valid, data)
        : this.reponseEvaluationService.createReponseEnseignant(this.convention.id, valid, data),
      [FicheType.Entreprise]: () => this.reponseEvaluation
        ? this.reponseEvaluationService.updateReponseEntreprise(this.convention.id, valid, data)
        : this.reponseEvaluationService.createReponseEntreprise(this.convention.id, valid, data),
    } as const;

    calls[typeFiche]().pipe(takeUntil(this.destroy$)).subscribe(onDone);
  }

  private buildIncompleteWarning(paths: string[]): string {
    const labels = [...new Set(
      paths.map(p => {
        const key = p.startsWith('suppl.') ? p.slice('suppl.'.length) : p;
        return this.controlLabels.get(key) || this.controlLabels.get(p) || key;
      })
    )];
    const max = 5;
    const shown = labels.slice(0, max);
    const rest = labels.length - shown.length;
    let detail = shown.join(' ; ');
    if (rest > 0) detail += ` ; et ${rest} autre${rest > 1 ? 's' : ''}`;
    return labels.length
      ? `Evaluation enregistrée, mais des champs restent à remplir : ${detail}`
      : 'Evaluation enregistrée avec succès, mais certains champs restent à remplir';
  }

  // ---------------- Impression / Modale ----------------

  printFiche(typeFiche: FicheType): void {
    this.reponseEvaluationService.getFichePDF(this.convention.id, typeFiche)
      .pipe(takeUntil(this.destroy$))
      .subscribe((response: any) => {
        const blob = new Blob([response as BlobPart], { type: "application/pdf" });
        const map = { [FicheType.Etudiant]: 'FicheEtudiant_', [FicheType.Enseignant]: 'FicheEnseignant_', [FicheType.Entreprise]: 'FicheEntreprise_' };
        FileSaver.saveAs(blob, `${map[typeFiche]}${this.convention.id}.pdf`);
      });
  }

  openConfirmEnvoiMailEvaluation(typeFiche: FicheType): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.disableClose = true;
    dialogConfig.data = { typeFiche, convention: this.convention };

    this.matDialog.open(ConfirmEnvoieMailComponent, dialogConfig)
      .afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((result?: { convention?: any }) => {
        if (result?.convention) {
          this.convention = result.convention;
          this.conventionChange.emit(result.convention);
        }
      });
  }

  // ---------------- Supplémentaires ----------------

  getQuestionSupplementaire(): void {
    this.ficheEvaluationService.getQuestionsSupplementaires(this.ficheEvaluation.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((response: any[]) => {
        for (const q of response) {
          const form = (q.idPlacement <= 2)
            ? this.reponseSupplementaireEtudiantForm
            : (q.idPlacement <= 4)
              ? this.reponseSupplementaireEnseignantForm
              : this.reponseSupplementaireEntrepriseForm;

          const name = 'questionSupplementaire' + q.id;
          if (!form.contains(name)) {
            const validator = q.typeQuestion === 'yn'
              ? this.requiredNonNull
              : Validators.required;
            form.addControl(name, new FormControl(null, validator));
          }
          q.formControlName = name;
          if (q.question) this.controlLabels.set(name, q.question);

          if (this.reponseEvaluation) {
            this.reponseEvaluationService.getReponseSupplementaire(this.convention.id, q.id)
              .pipe(takeUntil(this.destroy$))
              .subscribe((r: any) => {
                q.reponse = false;
                if (r) {
                  q.reponse = true;
                  if (q.typeQuestion === 'txt') form.get(name)?.setValue(r.reponseTxt);
                  if (q.typeQuestion === 'not') form.get(name)?.setValue(r.reponseInt);
                  if (q.typeQuestion === 'yn')  form.get(name)?.setValue(r.reponseBool);
                }
              });
          }
        }

        this.questionsSupplementaires = [
          response.filter(q => q.idPlacement === 0),
          response.filter(q => q.idPlacement === 1),
          response.filter(q => q.idPlacement === 2),
          response.filter(q => q.idPlacement === 3),
          response.filter(q => q.idPlacement === 4),
          response.filter(q => q.idPlacement === 5),
          response.filter(q => q.idPlacement === 6),
          response.filter(q => q.idPlacement === 7),
        ];
      });
  }

  // ---------------- Questions (VM / options / flags) ----------------

  private parseParamsJsonLooseArr(paramsJson?: string | null): string[] {
    if (!paramsJson) return [];
    try {
      const obj = JSON.parse(paramsJson);
      if (Array.isArray(obj?.items)) return obj.items;
      return [];
    } catch {
      const m = paramsJson.match(/\[([\s\S]*)\]/);
      if (!m) return [];
      return m[1].split(',').map(s => s.trim().replace(/^"|"$/g, '')).filter(Boolean);
    }
  }

  private extractOptions(q: { code: string; type: TypeQuestionEvaluation; paramsJson?: string | null; options?: string[] | null }): string[] {
    if (q.options?.length) return q.options;
    const fromParams = this.parseParamsJsonLooseArr(q.paramsJson);
    if (fromParams.length) return fromParams;

    if (q.type === TypeQuestionEvaluation.SCALE_LIKERT_5) return this.LIKERT_5;
    if (q.type === TypeQuestionEvaluation.SCALE_AGREEMENT_5) return this.AGREEMENT_5;

    return [];
  }

  private toVM(q: DbQuestion): DbQuestion {
    const needsOptions =
      q.type === TypeQuestionEvaluation.SINGLE_CHOICE ||
      q.type === TypeQuestionEvaluation.MULTI_CHOICE ||
      q.type === TypeQuestionEvaluation.BOOLEAN_GROUP ||
      q.type === TypeQuestionEvaluation.SCALE_LIKERT_5 ||
      q.type === TypeQuestionEvaluation.SCALE_AGREEMENT_5;

    return {
      code: q.code,
      texte: q.texte,
      type: q.type as TypeQuestionEvaluation,
      options: needsOptions ? this.extractOptions(q) : [],
      bisQuestion: q.bisQuestion,
      bisQuestionLowNotation: q.bisQuestionLowNotation ?? false,
      bisQuestionTrue: q.bisQuestionTrue ?? false,
      bisQuestionFalse: q.bisQuestionFalse ?? false,
      paramsJson: (q as any).paramsJson ?? null,
    };
  }

  private ensureFormControls(vm: DbQuestion): void {
    const form = vm.code.startsWith('ETU') ? this.reponseEtudiantForm
      : vm.code.startsWith('ENS') ? this.reponseEnseignantForm
        : this.reponseEntrepriseForm;

    const base = this.toControlBase(vm.code);

    if (!form.contains(base)) {
      form.addControl(base, new FormControl(null));
    }

    if (vm.type === TypeQuestionEvaluation.BOOLEAN_GROUP && Array.isArray(vm.options)) {
      vm.options.forEach((_, i) => {
        const key = base + this.controlsIndexToLetter[i];
        if (!form.contains(key)) form.addControl(key, new FormControl(null));
      });
    }

    // Bis texte uniquement s'il y a un libellé affichable (évite le fantôme ETUI7)
    if (vm.bisQuestion && !form.contains(base + 'bis')) {
      form.addControl(base + 'bis', new FormControl(null));
    }

    if (vm.code === 'ETUI7') {
      if (!form.contains(base + 'bis1')) form.addControl(base + 'bis1', new FormControl(null));
      if (!form.contains(base + 'bis2')) form.addControl(base + 'bis2', new FormControl(null));
    }
    if (vm.code === 'ETUII5') {
      if (!form.contains(base + 'a')) form.addControl(base + 'a', new FormControl(null));
      if (!form.contains(base + 'b')) form.addControl(base + 'b', new FormControl(null));
    }

    this.registerControlLabels(vm, base);
  }

  private registerControlLabels(vm: DbQuestion, base: string): void {
    if (vm.texte) this.controlLabels.set(base, vm.texte);

    if (vm.type === TypeQuestionEvaluation.BOOLEAN_GROUP && Array.isArray(vm.options)) {
      vm.options.forEach((opt, i) => {
        const key = base + this.controlsIndexToLetter[i];
        this.controlLabels.set(key, opt ? `${vm.texte} — ${opt}` : vm.texte);
      });
    }

    if (vm.bisQuestion) this.controlLabels.set(base + 'bis', vm.bisQuestion);

    if (vm.code === 'ETUI7') {
      const opts = this.getETUI7Options(vm);
      this.controlLabels.set(base + 'bis1', opts.labelOui || 'Si oui, par qui ?');
      this.controlLabels.set(base + 'bis2', opts.labelNon || 'Si non, pourquoi ?');
    }
    if (vm.code === 'ETUII5') {
      this.controlLabels.set(base + 'a', 'Si oui : a) De quel ordre ?');
      this.controlLabels.set(base + 'b', 'b) Avec autonomie ?');
    }
  }

  private bucketFor(code: string, s1: DbQuestion[], s2?: DbQuestion[], s3?: DbQuestion[]): DbQuestion[] {
    if (code.startsWith('ETUIII')) return s3!;
    if (code.startsWith('ETUII')) return s2!;
    if (code.startsWith('ETUI')) return s1;
    if (code.startsWith('ENSII')) return s2!;
    if (code.startsWith('ENSI')) return s1;
    if (code.startsWith('ENT')) {
      if (['ENT1', 'ENT2', 'ENT3', 'ENT5', 'ENT9', 'ENT11', 'ENT12', 'ENT13', 'ENT14'].includes(code)) return s1;
      if (['ENT4', 'ENT6', 'ENT7', 'ENT8', 'ENT15'].includes(code)) return s2!;
      return s3!;
    }
    return s1;
  }

  private applyDbQuestions(dbQuestions: DbQuestion[], s1: DbQuestion[], s2?: DbQuestion[], s3?: DbQuestion[]): void {
    for (const q of dbQuestions) {
      const vm = this.toVM(q);
      const target = this.bucketFor(vm.code, s1, s2, s3);
      const idx = target.findIndex(x => x.code === vm.code);
      if (idx > -1) target[idx] = { ...target[idx], ...vm };
      else target.push(vm);
      this.ensureFormControls(vm);

      if (vm.code === 'ETUI5') {
        const fromParams = this.parseParamsJsonLooseArr(vm.paramsJson);
        this.optionsETUI5 = fromParams.length ? fromParams : [...this.FALLBACK_ETUI5];
      }
    }
  }

  private toLegacyQuestionKey(code: string): string {
    if (code.startsWith('ETU')) return 'questionEtu' + code.substring(3);
    if (code.startsWith('ENS')) return 'questionEns' + code.substring(3);
    if (code.startsWith('ENT')) return 'questionEnt' + code.substring(3);
    return 'question' + code;
  }

  isQuestionActive(code: string): boolean {
    const key = this.toLegacyQuestionKey(code);
    if (!this.ficheEvaluation || !(key in this.ficheEvaluation)) return true;
    return !!this.ficheEvaluation[key];
  }

  /**
   * Unique assignateur de validateurs : obligation dérivée des mêmes prédicats
   * que le template (isQuestionActive, options, shouldShowBis, Oui/Non).
   */
  private syncValidators(form: FormGroup, q: DbQuestion): void {
    const active = this.isQuestionActive(q.code);
    const base = this.toControlBase(q.code);
    const mainVal = form.get(base)?.value;

    if (q.type === TypeQuestionEvaluation.BOOLEAN_GROUP) {
      this.setRequired(form, [base], false);
      const options = q.options || [];
      for (let i = 0; i < this.controlsIndexToLetter.length; i++) {
        const key = base + this.controlsIndexToLetter[i];
        if (!form.contains(key)) continue;
        this.setRequired(form, [key], active && i < options.length, TypeQuestionEvaluation.BOOLEAN_GROUP);
      }
    } else if (q.type === TypeQuestionEvaluation.AUTO) {
      this.setRequired(form, [base], false);
    } else {
      this.setRequired(form, [base], active, q.type);
    }

    // Affichage (shouldShowBis) ≠ obligation : les bis ENT sont souvent visibles mais optionnels
    this.setRequired(form, [base + 'bis'], active && this.shouldRequireBis(q, mainVal));

    if (q.code === 'ETUI7') {
      this.setRequired(form, [base + 'bis1'], active && !!mainVal, TypeQuestionEvaluation.SINGLE_CHOICE);
      this.setRequired(form, [base + 'bis2'], active && mainVal === false, TypeQuestionEvaluation.SINGLE_CHOICE);
    }

    if (q.code === 'ETUII5') {
      this.setRequired(form, [base + 'a'], active && !!mainVal, TypeQuestionEvaluation.SINGLE_CHOICE);
      this.setRequired(form, [base + 'b'], active && !!mainVal, TypeQuestionEvaluation.YES_NO);
    }
  }

  private syncAllValidators(): void {
    [...this.FicheEtudiantIQuestions, ...this.FicheEtudiantIIQuestions, ...this.FicheEtudiantIIIQuestions]
      .forEach(q => this.syncValidators(this.reponseEtudiantForm, q));
    [...this.FicheEnseignantIQuestions, ...this.FicheEnseignantIIQuestions]
      .forEach(q => this.syncValidators(this.reponseEnseignantForm, q));
    [...this.FicheEntrepriseIQuestions, ...this.FicheEntrepriseIIQuestions, ...this.FicheEntrepriseIIIQuestions]
      .forEach(q => this.syncValidators(this.reponseEntrepriseForm, q));
  }

  private wireValidatorSync(): void {
    const attach = (form: FormGroup, q: DbQuestion) => {
      const base = this.toControlBase(q.code);
      const main = form.get(base);
      if (!main) return;
      main.valueChanges
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => this.syncValidators(form, q));
    };

    [...this.FicheEtudiantIQuestions, ...this.FicheEtudiantIIQuestions, ...this.FicheEtudiantIIIQuestions]
      .forEach(q => attach(this.reponseEtudiantForm, q));
    [...this.FicheEnseignantIQuestions, ...this.FicheEnseignantIIQuestions]
      .forEach(q => attach(this.reponseEnseignantForm, q));
    [...this.FicheEntrepriseIQuestions, ...this.FicheEntrepriseIIQuestions, ...this.FicheEntrepriseIIIQuestions]
      .forEach(q => attach(this.reponseEntrepriseForm, q));
  }

  // ---------------- AUTO / Params helpers ----------------

  getAutoValue(code: string): string {
    switch (code) {
      case 'ETUI5':
        return this.convention?.origineStage?.libelle
          || this.convention?.nomenclature?.origineStage
          || '';
      case 'ETUIII0': return this.convention?.sujetStage || '';
      default: return '';
    }
  }

  isEtui7LegacySchema(): boolean {
    const v = this.reponseEvaluation?.schemaVersion;
    return v == null || v < 2;
  }

  toControlBase(code: string): string {
    if (!code) return '';
    if (code.startsWith('ETU')) return 'reponseEtu' + code.substring(3);
    if (code.startsWith('ENS')) return 'reponseEns' + code.substring(3);
    if (code.startsWith('ENT')) return 'reponseEnt' + code.substring(3);
    return 'reponse' + code;
  }

  // ---------------- Cas spéciaux JSON ----------------

  private parseObjectLoose(s?: string | null): any {
    if (!s) return {};
    try { return JSON.parse(s); } catch { return {}; }
  }

  getETUI7Options(q: DbQuestion): { oui: string[]; non: string[]; labelOui?: string; labelNon?: string } {
    const obj = this.parseObjectLoose(q?.paramsJson);
    const oui = Array.isArray(obj?.oui?.items) ? obj.oui.items : [];
    const non = Array.isArray(obj?.non?.items) ? obj.non.items : [];
    const labelOui = typeof obj?.oui?.label === 'string' ? obj.oui.label : undefined;
    const labelNon = typeof obj?.non?.label === 'string' ? obj.non.label : undefined;
    if (oui.length || non.length) {
      return { oui, non, labelOui, labelNon };
    }
    return {
      oui: [
        'Proposé par votre tuteur professionnel',
        'Proposé par votre tuteur enseignant',
        'Élaboré par vous-même',
        'Négocié entre les parties',
        'Autre'
      ],
      non: [
        'Je n\'ai pas eu besoin d\'aide',
        'Je ne savais pas à qui m\'adresser'
      ],
      labelOui: 'Si oui, par qui ?',
      labelNon: 'Si non, pourquoi ?'
    };
  }

  /** Affichage du bloc “bis” texte */
  shouldShowBis(q: DbQuestion): boolean {
    // Pas de texte bis => pas d’affichage
    if (!q?.bisQuestion) return false;

    const v = this.getMainValue(q);

    // ENT sur échelles : toujours afficher la bis (comportement existant)
    if ((q.type === TypeQuestionEvaluation.SCALE_LIKERT_5 || q.type === TypeQuestionEvaluation.SCALE_AGREEMENT_5) && q.code.startsWith('ENT')) {
      return true;
    }

    return this.shouldRequireBis(q, v);
  }

  /** Obligation du champ bis texte (flags base + texte bisQuestion). */
  private shouldRequireBis(q: DbQuestion, val: any): boolean {
    if (!q?.bisQuestion) return false;
    return !!(
      (q.bisQuestionLowNotation && typeof val === 'number' && val >= 3) ||
      (q.bisQuestionTrue && val === true) ||
      (q.bisQuestionFalse && val === false) ||
      (q.code === 'ETUIII1' && val === true)
    );
  }

  getMainValue(q: DbQuestion): any {
    const base = this.toControlBase(q.code);
    return this.reponseEtudiantForm.get(base)?.value
      ?? this.reponseEnseignantForm.get(base)?.value
      ?? this.reponseEntrepriseForm.get(base)?.value
      ?? null;
  }

  public getETUII5Options(q: any): { a: string[] } {
    const schemaVersion = this.reponseEvaluation?.schemaVersion == null || this.reponseEvaluation.schemaVersion < 2 ? 1 : 2;
    if (schemaVersion < 2) {
      return { a: ['Très importantes', 'Importantes', 'Peu importantes'] };
    }
    const obj = this.parseObjectLoose(q?.paramsJson);
    const aFromJson: string[] = Array.isArray(obj?.a) ? obj.a : [];
    if (aFromJson.length) return { a: aFromJson };

    const opts: string[] = Array.isArray(q?.options) ? q.options : [];
    if (!opts.length) return { a: ['Technique', 'Organisationnelle', 'Communication'] };
    return { a: opts };
  }

  private readonly requiredNonNull: ValidatorFn = (c: AbstractControl): ValidationErrors | null =>
    (c.value === null || c.value === undefined) ? { required: true } : null;

  private setRequired(form: FormGroup, keys: string[], on: boolean, type?: TypeQuestionEvaluation) {
    keys.forEach(k => {
      const c = form.get(k);
      if (!c) return;
      if (!on) {
        c.clearValidators();
      } else {
        const v = (type === TypeQuestionEvaluation.YES_NO || type === TypeQuestionEvaluation.BOOLEAN_GROUP)
          ? this.requiredNonNull
          : Validators.required;
        c.setValidators(v);
      }
      c.updateValueAndValidity({ emitEvent: false });
    });
  }

  /** Retourne la liste des contrôles invalides (chemin complet, erreurs et valeur courante) */
  private listInvalidControls(form: FormGroup, prefix = ''): Array<{ path: string; errors: any; value: any }> {
    const invalid: Array<{ path: string; errors: any; value: any }> = [];
    const stack: Array<{ group: FormGroup; base: string }> = [{ group: form, base: prefix }];

    while (stack.length) {
      const { group, base } = stack.pop()!;
      Object.entries(group.controls).forEach(([key, control]) => {
        const path = base ? `${base}.${key}` : key;

        if (control instanceof FormGroup) {
          stack.push({ group: control, base: path });
          return;
        }

        if (control.invalid) {
          invalid.push({ path, errors: control.errors, value: control.value });
        }
      });
    }
    return invalid;
  }

  private setAutoEtuI5(): void {
    const libelle = this.getAutoValue('ETUI5');
    const idx = this.optionsETUI5.indexOf(libelle);
    const ctrl = this.reponseEtudiantForm.get('reponseEtuI5');
    if (!ctrl) return;
    // AUTO : jamais obligatoire. Index si correspondance, sinon null (export via origineStageLibelle).
    ctrl.setValue(idx >= 0 ? idx : null, { emitEvent: false });
  }

  getLongTextControl(name: string): AbstractControl | null {
    return [
      this.reponseEtudiantForm,
      this.reponseEnseignantForm,
      this.reponseEntrepriseForm,
      this.reponseSupplementaireEtudiantForm,
      this.reponseSupplementaireEnseignantForm,
      this.reponseSupplementaireEntrepriseForm,
    ].map(form => form?.get(name)).find(control => !!control) ?? null;
  }

  isLongTextLimitReached(name: string): boolean {
    const value = this.getLongTextControl(name)?.value;
    return typeof value === 'string' && value.length >= this.MAX_LENTGH_INPUT.longText;
  }

  private coerceIndex(value: unknown): number | null {
    if (value === null || value === undefined || value === '') return null;
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
  }


}
