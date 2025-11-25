import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ViewportScroller } from '@angular/common';
import { ConventionEvaluationTuteur } from "../../models/convention-evaluation-tuteur.model";
import { EvaluationTuteurContextService } from "../../services/evaluation-tuteur-context.service";
import { MessageService } from "../../../../services/message.service";
import { EvaluationTuteurService } from "../../services/evaluation-tuteur.service";
import { DbQuestion } from "../../../../models/question-evaluation.model";
import { TypeQuestionEvaluation } from "../../../../constants/type-question-evaluation";

@Component({
    selector: 'app-questionnaire-tuteur',
    templateUrl: './questionnaire-tuteur.component.html',
    styleUrl: './questionnaire-tuteur.component.scss',
    standalone: false
})
export class QuestionnaireTuteurComponent implements OnInit {

  // ---------- État / Contexte ----------
  convention!: ConventionEvaluationTuteur;
  token!: string;
  ficheEvaluation: any;
  reponseEvaluation: any;
  questionsSupplementaires: any;

  // ---------- Forms ----------
  reponseEntrepriseForm: FormGroup;
  protected reponseSupplementaireEntrepriseForm!: FormGroup;

  // ---------- UI ----------
  selectedTab = 0;
  totalTabs = 3;

  // ---------- VM / Questions (Entreprise uniquement) ----------
  FicheEntrepriseIQuestions: DbQuestion[] = [];   // ENT1..ENT9
  FicheEntrepriseIIQuestions: DbQuestion[] = [];  // ENT10..ENT15
  FicheEntrepriseIIIQuestions: DbQuestion[] = []; // ENT16..ENT19

  // ---------- Constantes / Helpers ----------
  readonly controlsIndexToLetter = ['a','b','c','d','e','f','g','h'];
  private readonly LIKERT_5 = ['Excellent','Très bien','Bien','Satisfaisant','Insuffisant'];
  private readonly AGREEMENT_5 = ['Tout à fait d\'accord','Plutôt d\'accord','Sans avis','Plutôt pas d\'accord','Pas du tout d\'accord'];
  protected readonly TypeQuestionEvaluation = TypeQuestionEvaluation;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private viewportScroller: ViewportScroller,
    private ctx: EvaluationTuteurContextService,
    private messageService: MessageService,
    private evaluationTuteurService: EvaluationTuteurService,
  ) {

    this.reponseSupplementaireEntrepriseForm = this.fb.group({});

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
  }

  ngOnInit(): void {
    this.ctx.convention$.subscribe(c => {
      if (!c) return;
      this.convention = c;
      this.ficheEvaluation = c.ficheEvaluation;
      this.buildEntrepriseQuestionsVM();
      this.applyFicheVisibilityToValidators();
      if (c.reponseEvaluation) {
        this.reponseEvaluation = c.reponseEvaluation;
        this.reponseEntrepriseForm.patchValue(c.reponseEvaluation);
      }
      this.getQuestionSupplementaire();
    });

    this.ctx.token$.subscribe(t => { if (t != null) this.token = t; });
  }

  // ------------------------------------------------------------------------------------
  // VM ENTREPRISE
  // ------------------------------------------------------------------------------------
  private buildEntrepriseQuestionsVM(): void {
    // ENT1..ENT9 : Savoir-être (LIKERT)
    this.FicheEntrepriseIQuestions = [
      this.mkLikert('ENT1', "Adaptation au milieu professionnel :", true),
      this.mkLikert('ENT2', "Intégration au groupe de travail :", true),
      this.mkLikert('ENT3', "Assiduité - ponctualité :"),
      this.mkLikert('ENT5', "Intérêt pour l'établissement, les services, et les métiers :", true),
      this.mkLikert('ENT9', "Sens de l'organisation :", true),
      this.mkLikert('ENT11', "Capacité d'autonomie :", true),
      this.mkLikert('ENT12', "Initiative personnelle :", true),
      this.mkLikert('ENT13', "Implication :", true),
      this.mkLikert('ENT14', "Rigueur et précision dans le travail :", true),
    ];

    // ENT10..ENT15 : Savoir-faire (LIKERT)
    this.FicheEntrepriseIIQuestions = [
      this.mkLikert('ENT4', "Aptitude à cerner et situer le projet :", true),
      this.mkLikert('ENT6', "Aptitude à appliquer ses connaissances :", true),
      this.mkLikert('ENT7', "Esprit d'observation et pertinence des remarques :", true),
      this.mkLikert('ENT8', "Esprit de synthèse :", true),
      this.mkLikert('ENT15', "Aptitude à la communication :", true),
    ];

    // ENT16..ENT19 : Appréciation générale (AGREEMENT + LIKERT + TEXTE + YES/NO)
    this.FicheEntrepriseIIIQuestions = [
      {
        code: 'ENT16',
        texte: "Les objectifs ont-ils été atteints ?",
        type: TypeQuestionEvaluation.SCALE_AGREEMENT_5,
        options: this.AGREEMENT_5,
        bisQuestion: "Commentaire (facultatif)",
        bisQuestionLowNotation: false,
        bisQuestionTrue: false,
        bisQuestionFalse: false,
      },
      {
        code: 'ENT17',
        texte: "Indiquez votre appréciation générale de ce stage :",
        type: TypeQuestionEvaluation.SCALE_LIKERT_5,
        options: this.LIKERT_5,
        bisQuestion: "Pour quelles raisons ?",
        bisQuestionLowNotation: true,
        bisQuestionTrue: false,
        bisQuestionFalse: false,
      },
      {
        code: 'ENT19',
        texte: "Observations :",
        type: TypeQuestionEvaluation.TEXT,
        options: [],
        bisQuestion: undefined as any,
        bisQuestionLowNotation: false,
        bisQuestionTrue: false,
        bisQuestionFalse: false,
      },
      {
        code: 'ENT10',
        texte: "Avez-vous remis au stagiaire une attestation de stage ?",
        type: TypeQuestionEvaluation.YES_NO,
        options: [],
        bisQuestion: undefined as any,
        bisQuestionLowNotation: false,
        bisQuestionTrue: false,
        bisQuestionFalse: false,
      },
      {
        code: 'ENT18',
        texte: "Accepteriez-vous de reprendre un de nos étudiants en stage ?",
        type: TypeQuestionEvaluation.YES_NO,
        options: [],
        bisQuestion: "Pour quelles raisons ?",
        bisQuestionLowNotation: false,
        bisQuestionTrue: false,
        bisQuestionFalse: true,
      },
    ];

    [...this.FicheEntrepriseIQuestions, ...this.FicheEntrepriseIIQuestions, ...this.FicheEntrepriseIIIQuestions]
      .forEach(q => this.ensureFormControls(q));
  }

  private mkLikert(code: string, texte: string, bis = false): DbQuestion {
    return {
      code,
      texte,
      type: TypeQuestionEvaluation.SCALE_LIKERT_5,
      options: this.LIKERT_5,
      bisQuestion: bis ? 'Commentaire (facultatif)' : undefined as any,
      bisQuestionLowNotation: false,
      bisQuestionTrue: false,
      bisQuestionFalse: false,
    };
  }

  // ------------------------------------------------------------------------------------
  // Visibilité / Validators
  // ------------------------------------------------------------------------------------
  private applyFicheVisibilityToValidators(): void {
    const apply = (q: DbQuestion) => this.applyVisibilityForQuestion(this.reponseEntrepriseForm, q);
    [...this.FicheEntrepriseIQuestions, ...this.FicheEntrepriseIIQuestions, ...this.FicheEntrepriseIIIQuestions].forEach(apply);
    // Et on câble les validators conditionnels liés à la valeur
    this.wireConditionalValidators();
  }

  private applyVisibilityForQuestion(form: FormGroup, q: DbQuestion): void {
    const active = this.isQuestionActive(q.code);
    const base = this.toControlBase(q.code);

    const keys = [base];
    this.setRequired(form, keys, active, q.type);

    if (!active) {
      const conditionalKeys: string[] = [];
      if (q.bisQuestion || q.bisQuestionLowNotation || q.bisQuestionTrue || q.bisQuestionFalse) {
        conditionalKeys.push(base + 'bis');
      }
      this.setRequired(form, conditionalKeys, false);
    }
  }

  private wireConditionalValidators(): void {
    const attach = (q: DbQuestion) => {
      const base = this.toControlBase(q.code);
      const main = this.reponseEntrepriseForm.get(base);
      if (!main) return;

      main.valueChanges.subscribe(val => {
        if (!this.isQuestionActive(q.code)) return;

        const wantBis = !!(
            (q.bisQuestionLowNotation && typeof val === 'number' && val >= 3) ||
            (q.bisQuestionTrue && !!val) ||
            (q.bisQuestionFalse && val === false)
        );

        this.setRequired(this.reponseEntrepriseForm, [base + 'bis'], wantBis);
      });
    };

    [...this.FicheEntrepriseIQuestions, ...this.FicheEntrepriseIIQuestions, ...this.FicheEntrepriseIIIQuestions].forEach(attach);
  }

  // ------------------------------------------------------------------------------------
  // Questions supplémentaires (5,6,7)
  // ------------------------------------------------------------------------------------
  getQuestionSupplementaire(): void {
    if (!this.convention?.questionSupplementaire) {
      console.log('Aucune question supplémentaire trouvée');
      return;
    }

    const questionsSupplementaires = this.convention.questionSupplementaire;
    console.log('Questions supplémentaires chargées:', questionsSupplementaires);

    // Initialiser le tableau
    this.questionsSupplementaires = {
      5: [],
      6: [],
      7: []
    };

    for (let qs of questionsSupplementaires) {
      if (qs.idPlacement == 5 || qs.idPlacement == 6 || qs.idPlacement == 7) {
        const name = 'questionSupplementaire' + qs.id;

        // Ajouter le contrôle au formulaire s'il n'existe pas
        if (!this.reponseSupplementaireEntrepriseForm.contains(name)) {
          const validator = qs.typeQuestion === 'yn' ? this.requiredNonNull : Validators.required;
          this.reponseSupplementaireEntrepriseForm.addControl(name, new FormControl(null, validator));
        }

        qs.formControlName = name;
        qs.reponse = false;

        // Pré-remplir si des réponses existent
        if (this.reponseEvaluation && this.convention.reponseSupplementaires) {
          // Chercher la réponse correspondante dans le tableau
          const reponseCorrespondante = Array.isArray(this.convention.reponseSupplementaires)
            ? this.convention.reponseSupplementaires.find((r: any) => r.questionId === qs.id)
            : null;

          if (reponseCorrespondante) {
            qs.reponse = true;
            const control = this.reponseSupplementaireEntrepriseForm.get(name);

            if (qs.typeQuestion === 'txt' && reponseCorrespondante.reponseTxt !== undefined) {
              control?.setValue(reponseCorrespondante.reponseTxt);
            }
            if (qs.typeQuestion === 'not' && reponseCorrespondante.reponseInt !== undefined) {
              control?.setValue(reponseCorrespondante.reponseInt);
            }
            if (qs.typeQuestion === 'yn' && reponseCorrespondante.reponseBool !== undefined) {
              control?.setValue(reponseCorrespondante.reponseBool);
            }
          }
        }

        // Ajouter à la bonne catégorie
        this.questionsSupplementaires[qs.idPlacement].push(qs);
      }
    }

    console.log('Questions supplémentaires triées:', this.questionsSupplementaires);
    console.log('Bloc 5:', this.questionsSupplementaires[5]);
    console.log('Bloc 6:', this.questionsSupplementaires[6]);
    console.log('Bloc 7:', this.questionsSupplementaires[7]);
  }
  // ------------------------------------------------------------------------------------
  // Sauvegarde / Navigation
  // ------------------------------------------------------------------------------------
  saveReponse(): void {
    const source = Array.isArray(this.questionsSupplementaires) ? this.questionsSupplementaires : [];
    const questionsSupplementaires = [5, 6, 7].reduce<any[]>((acc, i) => {
      const arr = Array.isArray(source[i]) ? source[i] : [];
      return acc.concat(arr);
    }, []);

    const data = { ...this.reponseEntrepriseForm.value };

    for (let qs of questionsSupplementaires) {
      const payload = { reponseTxt: null as any, reponseInt: null as any, reponseBool: null as any };
      if (qs.typeQuestion === 'txt') payload.reponseTxt = this.reponseSupplementaireEntrepriseForm.get(qs.formControlName)?.value;
      if (qs.typeQuestion === 'not') payload.reponseInt = this.reponseSupplementaireEntrepriseForm.get(qs.formControlName)?.value;
      if (qs.typeQuestion === 'yn')  payload.reponseBool = this.reponseSupplementaireEntrepriseForm.get(qs.formControlName)?.value;

      const req$ = qs.reponse
        ? this.evaluationTuteurService.updateReponseSupplementaire(this.token, this.convention.id, qs.id, payload)
        : this.evaluationTuteurService.createReponseSupplementaire(this.token, this.convention.id, qs.id, payload);

      req$.subscribe();
    }

    if (this.reponseEvaluation) {
      this.evaluationTuteurService.updateReponse(this.token, this.convention.id, data).subscribe(resp => { this.reponseEvaluation = resp; });
    } else {
      this.evaluationTuteurService.createReponse(this.token, this.convention.id, data).subscribe(resp => { this.reponseEvaluation = resp; });
    }
  }

  terminer(): void {
    const valid = this.reponseEntrepriseForm.valid && this.reponseSupplementaireEntrepriseForm.valid;
    this.saveReponse();
    this.evaluationTuteurService.validate(this.token, this.convention.id, valid).subscribe();
    this.router.navigate(['/evaluation-tuteur', this.convention.id, 'terminee'], {
      relativeTo: this.route.parent,
      replaceUrl: true,
    });
  }

  enregistrer(): void {
    this.saveReponse();
    this.messageService.setSuccess('Evaluation enregistrée avec succès');
  }

  get isLastTab(): boolean {
    return this.selectedTab >= this.totalTabs - 1;
  }
  prevTab(): void { if (this.selectedTab > 0) { this.selectedTab--; this.scrollTop(); } }
  nextOrFinish(): void { this.isLastTab ? this.terminer() : (this.selectedTab++, this.scrollTop()); }
  private scrollTop(): void { requestAnimationFrame(() => this.viewportScroller.scrollToAnchor('page-top')); }

  // ------------------------------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------------------------------
  public toControlBase(code: string): string {
    if (!code) return '';
    if (code.startsWith('ENT')) return 'reponseEnt' + code.substring(3);
    return 'reponse' + code;
  }

  private requiredNonNull: ValidatorFn = (c: AbstractControl): ValidationErrors | null =>
    (c.value === null || c.value === undefined) ? { required: true } : null;

  private setRequired(form: FormGroup, keys: string[], on: boolean, type?: TypeQuestionEvaluation) {
    keys.forEach(k => {
      const c = form.get(k);
      if (!c) return;
      if (!on) c.clearValidators();
      else {
        const v = (type === TypeQuestionEvaluation.YES_NO || type === TypeQuestionEvaluation.BOOLEAN_GROUP)
          ? this.requiredNonNull
          : Validators.required;
        c.setValidators(v);
      }
      c.updateValueAndValidity({ emitEvent: false });
    });
  }

  private ensureFormControls(vm: DbQuestion): void {
    const form = this.reponseEntrepriseForm;
    const base = this.toControlBase(vm.code);

    // base
    if (!form.contains(base)) {
      const needRequired = vm.type !== TypeQuestionEvaluation.BOOLEAN_GROUP;
      form.addControl(base, new FormControl(null, needRequired ? Validators.required : []));
    }

    // bis
    if ((vm.bisQuestion || vm.bisQuestionLowNotation || vm.bisQuestionTrue || vm.bisQuestionFalse) && !form.contains(base + 'bis')) {
      form.addControl(base + 'bis', new FormControl(null));
    }
  }

  public toLegacyQuestionKey(code: string): string {
    if (code.startsWith('ENT')) return 'questionEnt' + code.substring(3);
    return 'question' + code;
  }
  private isQuestionActive(code: string): boolean {
    return !!this.ficheEvaluation?.[this.toLegacyQuestionKey(code)];
  }

  getMainValue(q: DbQuestion): any {
    return this.reponseEntrepriseForm.get(this.toControlBase(q.code))?.value ?? null;
  }

  shouldShowBis(q: DbQuestion): boolean {
    if (!q?.bisQuestion) return false;
    const v = this.getMainValue(q);
    if (q.type === TypeQuestionEvaluation.SCALE_LIKERT_5 || q.type === TypeQuestionEvaluation.SCALE_AGREEMENT_5) {
      if (q.bisQuestionLowNotation) return typeof v === 'number' && v >= 3;
      return true;
    }
    if (q.bisQuestionTrue && v === true) return true;
    if (q.bisQuestionFalse && v === false) return true;
    return false;
  }
}
