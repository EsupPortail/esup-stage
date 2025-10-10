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

enum FicheType { Etudiant = 0, Enseignant = 1, Entreprise = 2 }

@Component({
  selector: 'app-evaluation-stage',
  templateUrl: './evaluation-stage.component.html',
  styleUrls: ['./evaluation-stage.component.scss']
})
export class EvaluationStageComponent implements OnInit, OnDestroy {
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

  FicheEnseignantIQuestions: DbQuestion[] = [];
  FicheEnseignantIIQuestions: DbQuestion[] = [];

  FicheEntrepriseIQuestions: DbQuestion[] = [];
  FicheEntrepriseIIQuestions: DbQuestion[] = [];
  FicheEntrepriseIIIQuestions: DbQuestion[] = [];

  trackByCode = (_index: number, item: { code?: string }) => item?.code ?? _index;


  private destroy$ = new Subject<void>();
  private readonly LIKERT_5 = ['Excellent','Très bien','Bien','Satisfaisant','Insuffisant'];
  private readonly AGREEMENT_5 = ['Tout à fait d\'accord','Plutôt d\'accord','Sans avis','Plutôt pas d\'accord','Pas du tout d\'accord'];
  readonly controlsIndexToLetter = ['a','b','c','d','e','f','g','h'];
  protected readonly TypeQuestionEvaluation = TypeQuestionEvaluation;
  readonly FicheType = FicheType;
  private optionsETUI5 = [
    'Réponse à une offre de stage',
    'Candidature spontanée',
    'Réseau de connaissance',
    'Proposé par le département',
  ];


  constructor(
    private reponseEvaluationService: ReponseEvaluationService,
    private ficheEvaluationService: FicheEvaluationService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private authService: AuthService,
    private matDialog: MatDialog,
    private questionsEvaluationService: QuestionsEvaluationService,
  ) {
    // --- forms init (inchangé fonctionnellement) ---
    this.reponseEtudiantForm = this.fb.group({
      reponseEtuI1: [null, [Validators.required]],
      reponseEtuI1bis: [null],
      reponseEtuI2: [null, [Validators.required]],
      reponseEtuI3: [null, [Validators.required]],
      reponseEtuI4a: [null, [Validators.required]],
      reponseEtuI4b: [null, [Validators.required]],
      reponseEtuI4c: [null, [Validators.required]],
      reponseEtuI4d: [null, [Validators.required]],
      reponseEtuI5: [null],
      reponseEtuI6: [null, [Validators.required]],
      reponseEtuI7: [null, [Validators.required]],
      reponseEtuI7bis1: [null],
      reponseEtuI7bis1a: [null],
      reponseEtuI7bis1b: [null],
      reponseEtuI7bis2: [null],
      reponseEtuI8: [null, [Validators.required]],
      reponseEtuII1: [null, [Validators.required]],
      reponseEtuII1bis: [null],
      reponseEtuII2: [null, [Validators.required]],
      reponseEtuII2bis: [null],
      reponseEtuII3: [null, [Validators.required]],
      reponseEtuII3bis: [null],
      reponseEtuII4: [null, [Validators.required]],
      reponseEtuII5: [null, [Validators.required]],
      reponseEtuII5a: [null],
      reponseEtuII5b: [null],
      reponseEtuII6: [null, [Validators.required]],
      reponseEtuIII1: [null, [Validators.required]],
      reponseEtuIII1bis: [null],
      reponseEtuIII2: [null, [Validators.required]],
      reponseEtuIII2bis: [null],
      reponseEtuIII4: [null, [Validators.required]],
      reponseEtuIII5a: [null, [Validators.required]],
      reponseEtuIII5b: [null, [Validators.required]],
      reponseEtuIII5c: [null, [Validators.required]],
      reponseEtuIII5bis: [null],
      reponseEtuIII6: [null, [Validators.required]],
      reponseEtuIII6bis: [null],
      reponseEtuIII7: [null, [Validators.required]],
      reponseEtuIII7bis: [null],
      reponseEtuIII8: [null, [Validators.required]],
      reponseEtuIII8bis: [null],
      reponseEtuIII9: [null, [Validators.required]],
      reponseEtuIII9bis: [null],
      reponseEtuIII10: [null, [Validators.required]],
      reponseEtuIII11: [null, [Validators.required]],
      reponseEtuIII12: [null, [Validators.required]],
      reponseEtuIII14: [null, [Validators.required]],
      reponseEtuIII15: [null, [Validators.required]],
      reponseEtuIII15bis: [null],
      reponseEtuIII16: [null, [Validators.required]],
      reponseEtuIII16bis: [null, [Validators.required]],
    });

    this.reponseEnseignantForm = this.fb.group({
      reponseEnsI1a: [null, [Validators.required]],
      reponseEnsI1b: [null, [Validators.required]],
      reponseEnsI1c: [null, [Validators.required]],
      reponseEnsI2a: [null, [Validators.required]],
      reponseEnsI2b: [null, [Validators.required]],
      reponseEnsI2c: [null, [Validators.required]],
      reponseEnsI3: [null, [Validators.required]],
      reponseEnsII1: [null, [Validators.required]],
      reponseEnsII2: [null, [Validators.required]],
      reponseEnsII3: [null, [Validators.required]],
      reponseEnsII4: [null, [Validators.required]],
      reponseEnsII5: [null, [Validators.required]],
      reponseEnsII6: [null, [Validators.required]],
      reponseEnsII7: [null, [Validators.required]],
      reponseEnsII8: [null, [Validators.required]],
      reponseEnsII9: [null, [Validators.required]],
      reponseEnsII10: [null, [Validators.required]],
      reponseEnsII11: [null, [Validators.required]],
    });

    this.reponseEntrepriseForm = this.fb.group({
      reponseEnt1: [null, [Validators.required]],
      reponseEnt1bis: [null],
      reponseEnt2: [null, [Validators.required]],
      reponseEnt2bis: [null],
      reponseEnt3: [null, [Validators.required]],
      reponseEnt4: [null, [Validators.required]],
      reponseEnt4bis: [null],
      reponseEnt5: [null, [Validators.required]],
      reponseEnt5bis: [null],
      reponseEnt6: [null, [Validators.required]],
      reponseEnt6bis: [null],
      reponseEnt7: [null, [Validators.required]],
      reponseEnt7bis: [null],
      reponseEnt8: [null, [Validators.required]],
      reponseEnt8bis: [null],
      reponseEnt9: [null, [Validators.required]],
      reponseEnt9bis: [null],
      reponseEnt10: [null, [Validators.required]],
      reponseEnt10bis: [null],
      reponseEnt11: [null, [Validators.required]],
      reponseEnt11bis: [null],
      reponseEnt12: [null, [Validators.required]],
      reponseEnt12bis: [null],
      reponseEnt13: [null, [Validators.required]],
      reponseEnt13bis: [null],
      reponseEnt14: [null, [Validators.required]],
      reponseEnt14bis: [null],
      reponseEnt15: [null, [Validators.required]],
      reponseEnt15bis: [null],
      reponseEnt16: [null, [Validators.required]],
      reponseEnt16bis: [null],
      reponseEnt17: [null, [Validators.required]],
      reponseEnt17bis: [null],
      reponseEnt18: [null, [Validators.required]],
      reponseEnt18bis: [null],
      reponseEnt19: [null, [Validators.required]],
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

        this.wireConditionalValidators();
        this.applyFicheVisibilityToValidators();

        this.reponseEvaluation = rep ?? null;
        if (rep) {
          this.reponseEtudiantForm.patchValue(rep);
          this.reponseEnseignantForm.patchValue(rep);
          this.reponseEntrepriseForm.patchValue(rep);
        }
        this.getQuestionSupplementaire();

        const vI5 = this.getAutoValue('ETUI5');
        this.setAutoEtuI5();
        this.setRequired(this.reponseEtudiantForm, ['reponseEtuI5'], !!vI5);
        this.setRequired(this.reponseEtudiantForm, ['reponseEtuI7bis'], false);
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

  private toggleValidators(form: FormGroup, keys: string[], required: boolean): void {
    keys.forEach(key => {
      const c = form.get(key);
      if (!c) return;
      if (required) c.addValidators(Validators.required);
      else c.clearValidators();
      c.updateValueAndValidity({ emitEvent: false });
    });
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

    if (!valid) {
      const invalidMain = this.listInvalidControls(form);
      const invalidSuppl = this.listInvalidControls(supplForm, 'suppl');

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

      // Optionnel : filtrer les "bases" des BOOLEAN_GROUP si tu en as encore
      // console.warn('Sans bases BOOLEAN_GROUP =>', pretty(invalidMain.filter(i => !/reponse(Etu|Ens|Ent).*[a-z]$/.test(i.path))));

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
      if (valid) this.messageService.setSuccess('Evaluation enregistrée avec succès');
      else this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
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

    // Ne pas mettre de required sur le "base" des BOOLEAN_GROUP
    const needBaseRequired = vm.type !== TypeQuestionEvaluation.BOOLEAN_GROUP;

    if (!form.contains(base)) {
      form.addControl(base, new FormControl(null, needBaseRequired ? Validators.required : []));
    }

    if (vm.type === TypeQuestionEvaluation.BOOLEAN_GROUP && Array.isArray(vm.options)) {
      vm.options.forEach((_, i) => {
        const key = base + this.controlsIndexToLetter[i];
        if (!form.contains(key)) form.addControl(key, new FormControl(null, this.requiredNonNull));
      });
    }

    // Bis générique
    if ((vm.bisQuestion || vm.bisQuestionLowNotation || vm.bisQuestionTrue || vm.bisQuestionFalse) && !form.contains(base + 'bis')) {
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
  }

  private bucketFor(code: string, s1: DbQuestion[], s2?: DbQuestion[], s3?: DbQuestion[]): DbQuestion[] {
    if (code.startsWith('ETUIII')) return s3!;
    if (code.startsWith('ETUII')) return s2!;
    if (code.startsWith('ETUI')) return s1;
    if (code.startsWith('ENSII')) return s2!;
    if (code.startsWith('ENSI')) return s1;
    if (code.startsWith('ENT')) {
      const num = parseInt(code.replace('ENT',''), 10);
      if (num <= 9) return s1;
      if (num <= 14) return s2!;
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
    }
  }

  private toLegacyQuestionKey(code: string): string {
    if (code.startsWith('ETU')) return 'questionEtu' + code.substring(3);
    if (code.startsWith('ENS')) return 'questionEns' + code.substring(3);
    if (code.startsWith('ENT')) return 'questionEnt' + code.substring(3);
    return 'question' + code;
  }
  private isQuestionActive(code: string): boolean {
    return !!this.ficheEvaluation?.[this.toLegacyQuestionKey(code)];
  }

  private applyVisibilityForQuestion(form: FormGroup, q: any): void {
    const active = this.isQuestionActive(q.code);
    const base = this.toControlBase(q.code);

    const mainKeys: string[] =
      q.type === TypeQuestionEvaluation.BOOLEAN_GROUP
        ? (q.options || []).map((_: string, i: number) => base + this.controlsIndexToLetter[i])
        : [base];

    this.setRequired(form, mainKeys, active, q.type);

    if (!active) {
      const conditionalKeys: string[] = [];
      if (q.bisQuestionLowNotation || q.bisQuestionTrue || q.bisQuestionFalse) conditionalKeys.push(base + 'bis');
      if (q.code === 'ETUI7') conditionalKeys.push(base + 'bis1', base + 'bis2');
      if (q.code === 'ETUII5') conditionalKeys.push(base + 'a', base + 'b');
      this.setRequired(form, conditionalKeys, false);
    }
  }

  private applyFicheVisibilityToValidators(): void {
    [...this.FicheEtudiantIQuestions, ...this.FicheEtudiantIIQuestions, ...this.FicheEtudiantIIIQuestions]
      .forEach(q => this.applyVisibilityForQuestion(this.reponseEtudiantForm, q));
    [...this.FicheEnseignantIQuestions, ...this.FicheEnseignantIIQuestions]
      .forEach(q => this.applyVisibilityForQuestion(this.reponseEnseignantForm, q));
    [...this.FicheEntrepriseIQuestions, ...this.FicheEntrepriseIIQuestions, ...this.FicheEntrepriseIIIQuestions]
      .forEach(q => this.applyVisibilityForQuestion(this.reponseEntrepriseForm, q));
  }

  private wireConditionalValidators(): void {
    const attach = (form: FormGroup, q: DbQuestion) => {
      const base = this.toControlBase(q.code);
      const main = form.get(base);
      if (!main) return;

      main.valueChanges.subscribe(val => {
        if (!this.isQuestionActive(q.code)) return;

        // bis : required si condition remplie
        const wantBis = !!(
          (q.bisQuestionLowNotation && typeof val === 'number' && val >= 3) ||
          (q.bisQuestionTrue && !!val) ||
          (q.bisQuestionFalse && val === false)
        );

        this.setRequired(form, [base + 'bis'], wantBis);

        // ETUI7
        if (q.code === 'ETUI7') {
          this.setRequired(form, [base + 'bis1'], !!val, TypeQuestionEvaluation.SINGLE_CHOICE); // index requis
          this.setRequired(form, [base + 'bis2'], val === false, TypeQuestionEvaluation.SINGLE_CHOICE);
        }

        // ETUII5 : a = index requis, b = booléen requisNonNull
        if (q.code === 'ETUII5') {
          this.setRequired(form, [base + 'a'], !!val, TypeQuestionEvaluation.SINGLE_CHOICE);
          this.setRequired(form, [base + 'b'], !!val, TypeQuestionEvaluation.YES_NO); // << booléen
        }
      });
    };

    [...this.FicheEtudiantIQuestions, ...this.FicheEtudiantIIQuestions, ...this.FicheEtudiantIIIQuestions].forEach(q => attach(this.reponseEtudiantForm, q));
    [...this.FicheEnseignantIQuestions, ...this.FicheEnseignantIIQuestions].forEach(q => attach(this.reponseEnseignantForm, q));
    [...this.FicheEntrepriseIQuestions, ...this.FicheEntrepriseIIQuestions, ...this.FicheEntrepriseIIIQuestions].forEach(q => attach(this.reponseEntrepriseForm, q));
  }

  // ---------------- AUTO / Params helpers ----------------

  getAutoValue(code: string): string {
    switch (code) {
      case 'ETUI5': return this.convention?.origineStage?.libelle || '';
      case 'ETUIII0': return this.convention?.sujetStage || '';
      default: return '';
    }
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
    return { oui, non, labelOui, labelNon };
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

    // Flags pilotés par la base
    if (q.bisQuestionLowNotation && typeof v === 'number' && v >= 3) return true; // 0..4
    if (q.bisQuestionTrue && v === true) return true;
    if (q.bisQuestionFalse && v === false) return true;

    // Cas legacy explicite
    if (q.code === 'ETUIII1') return v === true;

    return false;
  }

  getMainValue(q: DbQuestion): any {
    const base = this.toControlBase(q.code);
    return this.reponseEtudiantForm.get(base)?.value
      ?? this.reponseEnseignantForm.get(base)?.value
      ?? this.reponseEntrepriseForm.get(base)?.value
      ?? null;
  }

  public getETUII5Options(q: any): { a: string[] } {
    const obj = this.parseObjectLoose(q?.paramsJson);
    const aFromJson: string[] = Array.isArray(obj?.a) ? obj.a : [];
    if (aFromJson.length) return { a: aFromJson };

    const opts: string[] = Array.isArray(q?.options) ? q.options : [];
    if (!opts.length) return { a: ["Technique", "Organisationnelle", "Communication"] };
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
    const libelle = this.convention?.origineStage?.libelle ?? '';
    const idx = this.optionsETUI5.indexOf(libelle);
    const ctrl = this.reponseEtudiantForm.get('reponseEtuI5');
    if (!ctrl) return;
    if (idx >= 0) {
      ctrl.setValue(idx);            // ✅ on envoie un number
      this.setRequired(this.reponseEtudiantForm, ['reponseEtuI5'], true);
    } else {
      // Pas de correspondance → on n’envoie rien et on ne bloque pas la validation
      ctrl.setValue(null);
      this.setRequired(this.reponseEtudiantForm, ['reponseEtuI5'], false);
    }
  }


}
