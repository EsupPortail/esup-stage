import { Component, OnInit, Input, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormGroup } from "@angular/forms";
import { FicheEvaluationService } from "../../../services/fiche-evaluation.service";
import { MessageService } from "../../../services/message.service";
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { QuestionSupplementaireFormComponent } from './question-supplementaire-form/question-supplementaire-form.component';
import { ContenuService } from "../../../services/contenu.service";
import { QuestionsEvaluationService } from "../../../services/questions-evaluation.service";
import { forkJoin } from "rxjs";
import { TypeQuestionEvaluation } from "../../../constants/type-question-evaluation";

@Component({
    selector: 'app-fiche-evaluation',
    templateUrl: './fiche-evaluation.component.html',
    styleUrls: ['./fiche-evaluation.component.scss'],
    encapsulation: ViewEncapsulation.None,
    standalone: false
})
export class FicheEvaluationComponent implements OnInit {

  texteAlerte: string = '';
  ficheEvaluation: any;
  questionsSupplementaires: any;

  typeQuestions: any = [
    {code: "txt", libelle: "Champ de texte libre"},
    {code: "not", libelle: "Notation"},
    {code: "yn", libelle: "Oui/Non"},
  ]

  ficheEtudiantForm: FormGroup;
  ficheEnseignantForm: FormGroup;
  ficheEntrepriseForm: FormGroup;

  FicheEtudiantIQuestions: any[] = [];
  FicheEtudiantIIQuestions: any[] = [];
  FicheEtudiantIIIQuestions: any[] = [];
  FicheEnseignantIQuestions: any[] = [];
  FicheEnseignantIIQuestions: any[] = [];
  FicheEntrepriseIQuestions: any[] = [];
  FicheEntrepriseIIQuestions: any[] = [];
  FicheEntrepriseIIIQuestions: any[] = [];

  private readonly LIKERT_5 = ['Excellent', 'Très bien', 'Bien', 'Satisfaisant', 'Insuffisant'];
  private readonly AGREEMENT_5 = ['Tout à fait d\'accord', 'Plutôt d\'accord', 'Sans avis', 'Plutôt pas d\'accord', 'Pas du tout d\'accord'];

  @Input() idCentreGestion: any;

  constructor(private fb: FormBuilder,
              private ficheEvaluationService: FicheEvaluationService,
              private messageService: MessageService,
              public matDialog: MatDialog,
              public contenuService: ContenuService,
              private questionsEvaluationService: QuestionsEvaluationService,
  ) {
    this.ficheEtudiantForm = this.fb.group({
      questionEtuI1: [null],
      questionEtuI2: [null],
      questionEtuI3: [null],
      questionEtuI4: [null],
      questionEtuI5: [null],
      questionEtuI6: [null],
      questionEtuI7: [null],
      questionEtuI8: [null],
      questionEtuII1: [null],
      questionEtuII2: [null],
      questionEtuII3: [null],
      questionEtuII4: [null],
      questionEtuII5: [null],
      questionEtuII6: [null],
      questionEtuIII1: [null],
      questionEtuIII2: [null],
      questionEtuIII4: [null],
      questionEtuIII5: [null],
      questionEtuIII6: [null],
      questionEtuIII7: [null],
      questionEtuIII8: [null],
      questionEtuIII9: [null],
      questionEtuIII10: [null],
      questionEtuIII11: [null],
      questionEtuIII12: [null],
      questionEtuIII14: [null],
      questionEtuIII15: [null],
      questionEtuIII16: [null],
    });
    this.ficheEnseignantForm = this.fb.group({
      questionEnsI1: [null],
      questionEnsI2: [null],
      questionEnsI3: [null],
      questionEnsII1: [null],
      questionEnsII2: [null],
      questionEnsII3: [null],
      questionEnsII4: [null],
      questionEnsII5: [null],
      questionEnsII6: [null],
      questionEnsII7: [null],
      questionEnsII8: [null],
      questionEnsII9: [null],
      questionEnsII10: [null],
      questionEnsII11: [null],
    });
    this.ficheEntrepriseForm = this.fb.group({
      questionEnt1: [null],
      questionEnt2: [null],
      questionEnt3: [null],
      questionEnt4: [null],
      questionEnt5: [null],
      questionEnt6: [null],
      questionEnt7: [null],
      questionEnt8: [null],
      questionEnt9: [null],
      questionEnt10: [null],
      questionEnt11: [null],
      questionEnt12: [null],
      questionEnt13: [null],
      questionEnt14: [null],
      questionEnt15: [null],
      questionEnt16: [null],
      questionEnt17: [null],
      questionEnt18: [null],
      questionEnt19: [null],
    });
  }

  ngOnInit(): void {
    this.contenuService.get('TEXTE_ALERTE_FICHE').subscribe((response: any) => {
      this.texteAlerte = response.texte;
    });

    forkJoin({
      etu: this.questionsEvaluationService.getQuestionsEtu(),
      ens: this.questionsEvaluationService.getQuestionsEns(),
      ent: this.questionsEvaluationService.getQuestionsEnt(),
    }).subscribe(({ etu, ens, ent }) => {
      this.buildQuestionSections(etu, ens, ent);
      this.loadFicheEvaluation();
    });
  }

  private loadFicheEvaluation(): void {
    this.ficheEvaluationService.getByCentreGestion(this.idCentreGestion).subscribe((response: any) => {
      this.ficheEvaluation = response;
      this.getQuestionSupplementaire();

      this.ficheEtudiantForm.setValue({
        questionEtuI1: this.ficheEvaluation.questionEtuI1,
        questionEtuI2: this.ficheEvaluation.questionEtuI2,
        questionEtuI3: this.ficheEvaluation.questionEtuI3,
        questionEtuI4: this.ficheEvaluation.questionEtuI4,
        questionEtuI5: this.ficheEvaluation.questionEtuI5,
        questionEtuI6: this.ficheEvaluation.questionEtuI6,
        questionEtuI7: this.ficheEvaluation.questionEtuI7,
        questionEtuI8: this.ficheEvaluation.questionEtuI8,
        questionEtuII1: this.ficheEvaluation.questionEtuII1,
        questionEtuII2: this.ficheEvaluation.questionEtuII2,
        questionEtuII3: this.ficheEvaluation.questionEtuII3,
        questionEtuII4: this.ficheEvaluation.questionEtuII4,
        questionEtuII5: this.ficheEvaluation.questionEtuII5,
        questionEtuII6: this.ficheEvaluation.questionEtuII6,
        questionEtuIII1: this.ficheEvaluation.questionEtuIII1,
        questionEtuIII2: this.ficheEvaluation.questionEtuIII2,
        questionEtuIII4: this.ficheEvaluation.questionEtuIII4,
        questionEtuIII5: this.ficheEvaluation.questionEtuIII5,
        questionEtuIII6: this.ficheEvaluation.questionEtuIII6,
        questionEtuIII7: this.ficheEvaluation.questionEtuIII7,
        questionEtuIII8: this.ficheEvaluation.questionEtuIII8,
        questionEtuIII9: this.ficheEvaluation.questionEtuIII9,
        questionEtuIII10: this.ficheEvaluation.questionEtuIII10,
        questionEtuIII11: this.ficheEvaluation.questionEtuIII11,
        questionEtuIII12: this.ficheEvaluation.questionEtuIII12,
        questionEtuIII14: this.ficheEvaluation.questionEtuIII14,
        questionEtuIII15: this.ficheEvaluation.questionEtuIII15,
        questionEtuIII16: this.ficheEvaluation.questionEtuIII16,
      });

      this.ficheEnseignantForm.setValue({
        questionEnsI1: this.ficheEvaluation.questionEnsI1,
        questionEnsI2: this.ficheEvaluation.questionEnsI2,
        questionEnsI3: this.ficheEvaluation.questionEnsI3,
        questionEnsII1: this.ficheEvaluation.questionEnsII1,
        questionEnsII2: this.ficheEvaluation.questionEnsII2,
        questionEnsII3: this.ficheEvaluation.questionEnsII3,
        questionEnsII4: this.ficheEvaluation.questionEnsII4,
        questionEnsII5: this.ficheEvaluation.questionEnsII5,
        questionEnsII6: this.ficheEvaluation.questionEnsII6,
        questionEnsII7: this.ficheEvaluation.questionEnsII7,
        questionEnsII8: this.ficheEvaluation.questionEnsII8,
        questionEnsII9: this.ficheEvaluation.questionEnsII9,
        questionEnsII10: this.ficheEvaluation.questionEnsII10,
        questionEnsII11: this.ficheEvaluation.questionEnsII11,
      });

      this.ficheEntrepriseForm.setValue({
        questionEnt1: this.ficheEvaluation.questionEnt1,
        questionEnt2: this.ficheEvaluation.questionEnt2,
        questionEnt3: this.ficheEvaluation.questionEnt3,
        questionEnt4: this.ficheEvaluation.questionEnt4,
        questionEnt5: this.ficheEvaluation.questionEnt5,
        questionEnt6: this.ficheEvaluation.questionEnt6,
        questionEnt7: this.ficheEvaluation.questionEnt7,
        questionEnt8: this.ficheEvaluation.questionEnt8,
        questionEnt9: this.ficheEvaluation.questionEnt9,
        questionEnt10: this.ficheEvaluation.questionEnt10,
        questionEnt11: this.ficheEvaluation.questionEnt11,
        questionEnt12: this.ficheEvaluation.questionEnt12,
        questionEnt13: this.ficheEvaluation.questionEnt13,
        questionEnt14: this.ficheEvaluation.questionEnt14,
        questionEnt15: this.ficheEvaluation.questionEnt15,
        questionEnt16: this.ficheEvaluation.questionEnt16,
        questionEnt17: this.ficheEvaluation.questionEnt17,
        questionEnt18: this.ficheEvaluation.questionEnt18,
        questionEnt19: this.ficheEvaluation.questionEnt19,
      });
    });
  }

  private buildQuestionSections(etu: any[], ens: any[], ent: any[]): void {
    this.FicheEtudiantIQuestions = [];
    this.FicheEtudiantIIQuestions = [];
    this.FicheEtudiantIIIQuestions = [];
    this.FicheEnseignantIQuestions = [];
    this.FicheEnseignantIIQuestions = [];
    this.FicheEntrepriseIQuestions = [];
    this.FicheEntrepriseIIQuestions = [];
    this.FicheEntrepriseIIIQuestions = [];

    for (const q of etu ?? []) {
      this.bucketForEtu(q.code).push(this.buildPreviewQuestion(q));
    }
    for (const q of ens ?? []) {
      this.bucketForEns(q.code).push(this.buildPreviewQuestion(q));
    }
    for (const q of ent ?? []) {
      this.bucketForEnt(q.code).push(this.buildPreviewQuestion(q));
    }
  }

  private bucketForEtu(code: string): any[] {
    if (code.startsWith('ETUIII')) return this.FicheEtudiantIIIQuestions;
    if (code.startsWith('ETUII')) return this.FicheEtudiantIIQuestions;
    return this.FicheEtudiantIQuestions;
  }

  private bucketForEns(code: string): any[] {
    if (code.startsWith('ENSII')) return this.FicheEnseignantIIQuestions;
    return this.FicheEnseignantIQuestions;
  }

  private bucketForEnt(code: string): any[] {
    if (['ENT4', 'ENT6', 'ENT7', 'ENT8', 'ENT15'].includes(code)) return this.FicheEntrepriseIIQuestions;
    if (['ENT16', 'ENT17', 'ENT18', 'ENT19', 'ENT10'].includes(code)) return this.FicheEntrepriseIIIQuestions;
    return this.FicheEntrepriseIQuestions;
  }

  private toControlName(code: string): string {
    if (code.startsWith('ETU')) return 'questionEtu' + code.substring(3);
    if (code.startsWith('ENS')) return 'questionEns' + code.substring(3);
    if (code.startsWith('ENT')) return 'questionEnt' + code.substring(3);
    return 'question' + code;
  }

  private parseParamsJson(paramsJson?: string | null): any {
    if (!paramsJson) return null;
    try {
      return JSON.parse(paramsJson);
    } catch {
      return null;
    }
  }

  private extractItems(q: any): string[] {
    const params = this.parseParamsJson(q.paramsJson);
    if (params?.items && Array.isArray(params.items)) {
      return params.items.map((x: any) => String(x));
    }
    if (q.type === TypeQuestionEvaluation.SCALE_LIKERT_5) return this.LIKERT_5;
    if (q.type === TypeQuestionEvaluation.SCALE_AGREEMENT_5) return this.AGREEMENT_5;
    return [];
  }

  private buildPreviewQuestion(q: any): { title: string; texte: string[]; controlName: string } {
    const lines: string[] = [];
    const params = this.parseParamsJson(q.paramsJson);
    const type = q.type as TypeQuestionEvaluation;

    if (q.code === 'ETUI7') {
      lines.push('Oui / Non');
      if (params?.oui?.items) {
        lines.push(' ', params.oui.label || 'Si oui, par qui ?');
        params.oui.items.forEach((item: string) => lines.push('    ' + item));
      }
      if (params?.non?.items) {
        lines.push(' ', params.non.label || 'Si non, pourquoi ?');
        params.non.items.forEach((item: string) => lines.push('    ' + item));
      }
    } else if (q.code === 'ETUII5') {
      lines.push('Oui / Non', ' ', 'Si oui : a) de quel ordre ?');
      (params?.a?.items ?? params?.items ?? []).forEach((item: string) => lines.push('    ' + item));
      lines.push(' ', 'b) avec autonomie ?', '    Oui / Non');
    } else if (q.code === 'ETUIII5') {
      const items = params?.items ?? [
        'Compétences techniques',
        'Nouvelles méthodologies',
        'Nouvelles connaissances théoriques',
      ];
      items.forEach((item: string) => lines.push(item + ' Oui / Non'));
    } else if (type === TypeQuestionEvaluation.YES_NO) {
      lines.push('Oui / Non');
      if (q.bisQuestion) lines.push(' ', q.bisQuestion);
    } else if (type === TypeQuestionEvaluation.BOOLEAN_GROUP) {
      this.extractItems(q).forEach(item => lines.push(item + ' Oui / Non'));
    } else if (type === TypeQuestionEvaluation.AUTO) {
      lines.push('(Récupération automatique depuis la convention)');
    } else if (type === TypeQuestionEvaluation.TEXT) {
      lines.push('Champ de texte libre');
    } else {
      this.extractItems(q).forEach(item => lines.push(item));
      if (q.bisQuestion) lines.push(' ', q.bisQuestion);
    }

    return {
      title: q.texte,
      texte: lines.length ? lines : ['—'],
      controlName: this.toControlName(q.code),
    };
  }

  saveAndValidateFicheEtudiant(): void {
    this.ficheEvaluationService.saveAndValidateFicheEtudiant(this.ficheEvaluation.id,this.ficheEtudiantForm.value).subscribe((response: any) => {
      this.messageService.setSuccess("Fiche Etudiant enregistrée avec succès");
      this.ficheEvaluation = response;
    });
  }

  saveAndValidateFicheEnseignant(): void {
    this.ficheEvaluationService.saveAndValidateFicheEnseignant(this.ficheEvaluation.id,this.ficheEnseignantForm.value).subscribe((response: any) => {
      this.messageService.setSuccess("Fiche Enseignant enregistrée avec succès");
      this.ficheEvaluation = response;
    });
  }

  saveAndValidateFicheEntreprise(): void {
    this.ficheEvaluationService.saveAndValidateFicheEntreprise(this.ficheEvaluation.id,this.ficheEntrepriseForm.value).subscribe((response: any) => {
      this.messageService.setSuccess("Fiche Entreprise enregistrée avec succès");
      this.ficheEvaluation = response;
    });
  }

  getTypeQuestionLibelle(code: string) : string {
    const typeQuestion = this.typeQuestions.find((tq: any) => tq.code == code);
    if (typeQuestion) {
      return typeQuestion.libelle;
    }
    return '';
  }

  getQuestionSupplementaire(): void {
    this.ficheEvaluationService.getQuestionsSupplementaires(this.ficheEvaluation.id).subscribe((response: any) => {
      this.questionsSupplementaires = [];
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 0));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 1));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 2));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 3));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 4));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 5));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 6));
      this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 7));
    });
  }

  openQuestionSupplementaireFormModal(idPlacement: number, question: any) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '600px';
    let isModif = question?true:false
    dialogConfig.data = {question: question, isModif: isModif};
    const modalDialog = this.matDialog.open(QuestionSupplementaireFormComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(data => {
      if (data) {
        data.idPlacement = idPlacement;
        if (isModif) {
          this.editQuestionSupplementaire(question.id,data);
        }else{
          this.addQuestionSupplementaire(data);
        }
      }
    });
  }

  addQuestionSupplementaire(data: any): void {
    this.ficheEvaluationService.addQuestionSupplementaire(this.ficheEvaluation.id,data).subscribe((response: any) => {
      this.messageService.setSuccess("QuestionSupplementaire ajoutée avec succès");
      this.getQuestionSupplementaire();
    });
  }

  editQuestionSupplementaire(id: number, data: any): void {
    this.ficheEvaluationService.editQuestionSupplementaire(id,data).subscribe((response: any) => {
      this.messageService.setSuccess("QuestionSupplementaire éditée avec succès");
      this.getQuestionSupplementaire();
    });
  }

  deleteQuestionSupplementaire(id: number): void {
    this.ficheEvaluationService.deleteQuestionSupplementaire(id).subscribe((response: any) => {
      this.messageService.setSuccess("QuestionSupplementaire supprimée avec succès");
      this.getQuestionSupplementaire();
    });
  }
}
