import { Component, OnInit, Input, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { FicheEvaluationService } from "../../../services/fiche-evaluation.service";
import { MessageService } from "../../../services/message.service";
import { MatLegacyDialog as MatDialog, MatLegacyDialogConfig as MatDialogConfig } from '@angular/material/legacy-dialog';
import { QuestionSupplementaireFormComponent } from './question-supplementaire-form/question-supplementaire-form.component';
import { ContenuService } from "../../../services/contenu.service";

@Component({
  selector: 'app-fiche-evaluation',
  templateUrl: './fiche-evaluation.component.html',
  styleUrls: ['./fiche-evaluation.component.scss'],
  encapsulation: ViewEncapsulation.None,
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

  FicheEtudiantIQuestions: any = [
    {
      title: "Avez-vous rencontré des difficultés pour trouver un stage ?",
      texte: [
              "Non, il est automatiquement proposé dans le cadre de la formation",
              "Non, je l’ai trouvé assez facilement par moi-même",
              "Oui j’ai eu des difficultés",
             ],
      controlName: "questionEtuI1",
    },
    {
      title: "Combien de temps a duré votre recherche de stage ?",
      texte: [
              "1 jour à 1 semaine",
              "2 semaines à 1 mois",
              "1 mois à 3 mois",
              "3 mois à 6 mois",
              "+ de 6 mois",
             ],
      controlName: "questionEtuI2",
    },
    {
      title: "Combien d'établissement(s) d'accueil avez-vous prospecté(s) ?",
      texte: [
              "1 à 5",
              "6 à 10",
              "11 à 20",
              "20 et plus",
             ],
      controlName: "questionEtuI3",
    },
    {
      title: "Quel(s) procédé(s) de démarchage avez-vous utilisé(s) ?",
      texte: [
              "Mail Oui / Non",
              "Téléphone Oui / Non",
              "Courrier Oui / Non",
              "Prospection directe Oui / Non",
             ],
      controlName: "questionEtuI4",
    },
    {
      title: "Comment avez-vous trouvé votre stage ? (récupération de l'information depuis la convention)",
      texte: [
              "Réponse à une offre de stage",
              "Candidature spontanée",
              "Réseau de connaissance",
              "Proposé par le département",
             ],
      controlName: "questionEtuI5",
    },
    {
      title: "Comment avez-vous déterminé le contenu de stage ?",
      texte: [
              "Proposé par votre tuteur professionnel",
              "Proposé par votre tuteur enseignant",
              "Élaboré par vous-même",
              "Négocié entre les parties",
             ],
      controlName: "questionEtuI6",
    },
    {
      title: "Avez-vous été accompagné(e) dans vos démarches ?",
      texte: [
              "Oui / Non",
              " ",
              "Si oui, par qui ?",
              "    Par votre réseau personnel",
              "    Au sein de votre formation",
              "    Par le service d'Information, d'Orientation et d'Insertion Professionnelle",
              "    Par le Bureau d'Aide à l'Insertion Professionnelle",
              "    Autre",
              " ",
              "Si non, pourquoi ?",
              "    Par choix",
              "    Par méconnaissance des dispositifs proposés par votre université",
             ],
      controlName: "questionEtuI7",
    },
    {
      title: "Est-ce que les modalités d'évaluation de stage vous ont été clairement présentées avant le début du stage ?",
      texte: [
              "Oui / Non",
             ],
      controlName: "questionEtuI8",
    },
  ];

  FicheEtudiantIIQuestions: any = [
    {
      title: "Comment qualifieriez-vous l'accueil de votre entreprise ?",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEtuII1",
    },
    {
      title: "Comment qualifieriez-vous l'encadrement de votre stage ?",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEtuII2",
    },
    {
      title: "Comment qualifieriez-vous votre adaptation au sein de l'établissement d'accueil ?",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEtuII3",
    },
    {
      title: "Les conditions matérielles vous ont permis d’atteindre les objectifs de votre stage :",
      texte: [
              "Tout à fait d'accord",
              "Plutôt d'accord",
              "Plutôt pas d'accord",
              "Pas du tout d'accord",
             ],
      controlName: "questionEtuII4",
    },
    {
      title: "Avez-vous exercé des responsabilités ?",
      texte: [
              "Oui / Non",
              " ",
              "Si oui : a) de quel ordre ?",
              "    Très importantes",
              "    Importantes",
              "    Peu importantes",
              " ",
              "b) avec autonomie ?",
              "    Oui / Non",
             ],
      controlName: "questionEtuII5",
    },
    {
      title: "Votre stage avait-il une dimension internationale ?",
      texte: [
              "Oui / Non",
             ],
      controlName: "questionEtuII6",
    },
  ];

  FicheEtudiantIIIQuestions: any = [
    {
      title: "Votre sujet de stage était à l'origine : (récuperation du sujet depuis la convention). L'avez-vous modifié ?",
      texte: [
              "Oui / Non",
             ],
      controlName: "questionEtuIII1",
    },
    {
      title: "Selon vous, le stage a-t-il bien été en adéquation avec votre formation ?",
      texte: [
              "Oui / Non",
             ],
      controlName: "questionEtuIII2",
    },
    {
      title: "Les missions confiées ont été ?",
      texte: [
              "Très au-dessous de vos compétences",
              "Au-dessous de vos compétences",
              "A votre niveau de compétences",
              "Au-dessus de vos compétences",
              "Très au-dessus de vos compétences",
              "Inatteignables",
             ],
      controlName: "questionEtuIII4",
    },
    {
      title: "Ce stage vous a-t-il permis d'acquérir :",
      texte: [
              "Compétences techniques Oui / Non",
              "Nouvelles méthodologies Oui / Non",
              "Nouvelles connaissances théoriques Oui / Non",
             ],
      controlName: "questionEtuIII5",
    },
    {
      title: "Ce stage vous a permis de progresser dans la construction de votre projet personnel et professionnel :",
      texte: [
              "Tout à fait d'accord",
              "Plutôt d'accord",
              "Sans avis",
              "Plutôt pas d'accord",
              "Pas du tout d'accord",
             ],
      controlName: "questionEtuIII6",
    },
    {
      title: "Ce stage vous paraît déterminant à cette étape de votre formation :",
      texte: [
              "Tout à fait d'accord",
              "Plutôt d'accord",
              "Sans avis",
              "Plutôt pas d'accord",
              "Pas du tout d'accord",
             ],
      controlName: "questionEtuIII7",
    },
    {
      title: "Votre travail a-t-il abouti à une réorganisation du travail ?",
      texte: [
              "Oui / Non",
             ],
      controlName: "questionEtuIII8",
    },
    {
      title: "Allez-vous valoriser cette expérience dans une prochaine recherche d'emploi/stage ?",
      texte: [
              "Oui / Non",
             ],
      controlName: "questionEtuIII9",
    },
    {
      title: "Votre travail va-t-il donner lieu à un dépôt de brevet ?",
      texte: [
              "Oui / Non",
             ],
      controlName: "questionEtuIII10",
    },
    {
      title: "Avez-vous reçu une attestation de stage ?",
      texte: [
              "Oui / Non",
             ],
      controlName: "questionEtuIII11",
    },
    {
      title: "Avez-vous rencontré des difficultés à percevoir votre gratification ?",
      texte: [
              "Oui / Non",
             ],
      controlName: "questionEtuIII12",
    },
    {
      title: "Ce stage a-t-il donné lieu à une proposition d'emploi ou d'alternance ?",
      texte: [
              "Oui / Non",
             ],
      controlName: "questionEtuIII14",
    },
    {
      title: "Conseilleriez-vous cet établissement d'accueil à un autre étudiant ?",
      texte: [
              "Tout à fait d'accord",
              "Plutôt d'accord",
              "Sans avis",
              "Plutôt pas d'accord",
              "Pas du tout d'accord",
             ],
      controlName: "questionEtuIII15",
    },
    {
      title: "Indiquez votre appréciation générale sur le stage :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEtuIII16",
    },
  ];

  FicheEnseignantIQuestions: any = [
    {
      title: "Modalité(s) d’échange(s) avec le stagiaire :",
      texte: [
              "Téléphone Oui / Non",
              "Mail Oui / Non",
              "Rencontre Oui / Non",
             ],
      controlName: "questionEnsI1",
    },
    {
      title: "Modalité(s) d’échange(s) avec le tuteur professionnel :",
      texte: [
              "Téléphone Oui / Non",
              "Mail Oui / Non",
              "Rencontre Oui / Non",
             ],
      controlName: "questionEnsI2",
    },
    {
      title: "Commentaire(s) :",
      texte: [
              "Champ de texte libre",
             ],
      controlName: "questionEnsI3",
    },
  ]

  FicheEnseignantIIQuestions: any = [
    {
      title: "Impression générale et présentation de l’étudiant :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnsII1",
    },
    {
      title: "Aptitude à cerner et situer le projet :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnsII2",
    },
    {
      title: "Aptitude à appliquer ses connaissances :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnsII3",
    },
    {
      title: "Maîtrise du sujet, argumentation, analyse :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnsII4",
    },
    {
      title: "Mise en évidence des éléments importants de l’étude :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnsII5",
    },
    {
      title: "Utilisation des moyens de communication :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnsII6",
    },
    {
      title: "Qualité de l’expression orale :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnsII7",
    },
    {
      title: "Capacité à intéresser l’auditoire :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnsII8",
    },
    {
      title: "Pertinence des réponses :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnsII9",
    },
    {
      title: "Respect du temps alloué :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnsII10",
    },
    {
      title: "Commentaire(s) :",
      texte: [
              "Champ de texte libre",
             ],
      controlName: "questionEnsII11",
    },
  ]

  FicheEntrepriseIQuestions: any = [
    {
      title: "Adaptation au milieu professionnel :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnt1",
    },
    {
      title: "Intégration au groupe de travail :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnt2",
    },
    {
      title: "Assiduité - ponctualité :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnt3",
    },
    {
      title: "Intérêt pour l'établissement, les services, et les métiers :",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      controlName: "questionEnt5",
    },
    {
      title: "Sens de l'organisation :",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      controlName: "questionEnt9",
    },
    {
      title: "Capacité d'autonomie :",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      controlName: "questionEnt11",
    },
    {
      title: "Initiative personnelle :",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      controlName: "questionEnt12",
    },
    {
      title: "Implication :",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      controlName: "questionEnt13",
    },
    {
      title: "Rigueur et précision dans le travail :",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      controlName: "questionEnt14",
    },
  ]

  FicheEntrepriseIIQuestions: any = [
    {
      title: "Aptitude à cerner et situer le projet :",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      controlName: "questionEnt4",
    },
    {
      title: "Aptitude à appliquer ses connaissances",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      controlName: "questionEnt6",
    },
    {
      title: "Esprit d'observation et pertinence des remarques :",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      controlName: "questionEnt7",
    },
    {
      title: "Esprit de synthèse :",
      texte: [
        "Excellent",
        "Très bien",
        "Bien",
        "Satisfaisant",
        "Insuffisant",
      ],
      controlName: "questionEnt8",
    },
    {
      title: "Aptitude à la communication :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnt15",
    },
  ]

  FicheEntrepriseIIIQuestions: any = [
    {
      title: "Les objectifs ont-ils été atteints ?",
      texte: [
              "Tout à fait d'accord",
              "Plutôt d'accord",
              "Sans avis",
              "Plutôt pas d'accord",
              "Pas du tout d'accord",
             ],
      controlName: "questionEnt16",
    },
    {
      title: "Indiquez votre appréciation générale de ce stage :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "questionEnt17",
    },
    {
      title: "Observations :",
      texte: [
        "Champ de texte libre",
      ],
      controlName: "questionEnt19",
    },
    {
      title: "Avez-vous remis au stagiaire une attestation de stage ?",
      texte: [
        "Oui / Non",
      ],
      controlName: "questionEnt10",
    },
    {
      title: "Accepteriez-vous de reprendre un de nos étudiants en stage ?",
      texte: [
        "Oui / Non",
      ],
      controlName: "questionEnt18",
    },
  ]

  @Input() idCentreGestion: any;

  constructor(private fb: FormBuilder,
              private ficheEvaluationService: FicheEvaluationService,
              private messageService: MessageService,
              public matDialog: MatDialog,
              public contenuService: ContenuService,
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
      questionEtuIII3: [null],
      questionEtuIII4: [null],
      questionEtuIII5: [null],
      questionEtuIII6: [null],
      questionEtuIII7: [null],
      questionEtuIII8: [null],
      questionEtuIII9: [null],
      questionEtuIII10: [null],
      questionEtuIII11: [null],
      questionEtuIII12: [null],
      questionEtuIII13: [null],
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
        questionEtuIII3: this.ficheEvaluation.questionEtuIII3,
        questionEtuIII4: this.ficheEvaluation.questionEtuIII4,
        questionEtuIII5: this.ficheEvaluation.questionEtuIII5,
        questionEtuIII6: this.ficheEvaluation.questionEtuIII6,
        questionEtuIII7: this.ficheEvaluation.questionEtuIII7,
        questionEtuIII8: this.ficheEvaluation.questionEtuIII8,
        questionEtuIII9: this.ficheEvaluation.questionEtuIII9,
        questionEtuIII10: this.ficheEvaluation.questionEtuIII10,
        questionEtuIII11: this.ficheEvaluation.questionEtuIII11,
        questionEtuIII12: this.ficheEvaluation.questionEtuIII12,
        questionEtuIII13: this.ficheEvaluation.questionEtuIII13,
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
