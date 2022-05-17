import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ReponseEvaluationService } from "../../../services/reponse-evaluation.service";
import { MessageService } from "../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";

@Component({
  selector: 'app-evaluation-stage',
  templateUrl: './evaluation-stage.component.html',
  styleUrls: ['./evaluation-stage.component.scss']
})
export class EvaluationStageComponent implements OnInit {

  reponseEvaluation: any;
  questionsSupplementaires: any;
  @Input() convention: any;

  reponseEtudiantForm: FormGroup;

  edit: boolean = false;
  editEtu: boolean = false;
  editEns: boolean = false;
  editEnt: boolean = false;

  @ViewChild(MatExpansionPanel) firstPanel: MatExpansionPanel|undefined;


  controlsIndexToLetter:any = ['a','b','c','d','e','f','g','h']
  FicheEtudiantIQuestions: any = [
    {
      title: "Avez-vous rencontré des difficultés pour trouver un stage ?",
      type: "multiple-choice",
      texte: [
              "Non, il est automatiquement proposé dans le cadre de la formation",
              "Non, je l’ai trouvé assez facilement par moi-même",
              "Oui j’ai eu des difficultés",
             ],
      controlName: "EtuI1",
    },
    {
      title: "Combien de temps a duré votre recherche de stage ?",
      type: "multiple-choice",
      texte: [
              "1 jour à 1 semaine",
              "2 semaines à 1 mois",
              "1 mois à 3 mois",
              "3 mois à 6 mois",
              "+ de 6 mois",
             ],
      controlName: "EtuI2",
    },
    {
      title: "Combien d'établissement(s) d'accueil avez-vous prospecté(s) ?",
      type: "multiple-choice",
      texte: [
              "1 à 5",
              "6 à 10",
              "11 à 20",
              "20 et plus",
             ],
      controlName: "EtuI3",
    },
    {
      title: "Quel(s) procédé(s) de démarchage avez-vous utilisé(s) ?",
      type: "multiple-boolean",
      texte: [
              "Mail",
              "Téléphone",
              "Courrier",
             ],
      controlName: "EtuI4",
    },
    {
      title: "Comment avez-vous trouvé votre stage ? (récupération de l'information depuis la convention)",
      type: "EtuI5",
      texte: [
              "Réponse à une offre de stage",
              "Candidature spontanée",
              "Réseau de connaissance",
              "Proposé par le département",
             ],
      controlName: "EtuI5",
    },
    {
      title: "Comment avez-vous déterminé le contenu de stage ?",
      type: "multiple-choice",
      texte: [
              "Proposé par votre tuteur professionnel",
              "Proposé par votre tuteur enseignant",
              "Élaboré par vous-même",
              "Négocié entre les parties",
             ],
      controlName: "EtuI6",
    },
    {
      title: "Avez-vous été accompagné(e) dans vos démarches ?",
      type: "EtuI7",
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
      controlName: "EtuI7",
    },
    {
      title: "Est-ce que les modalités d'évaluation de stage vous ont été clairement présentées avant le début du stage ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuI8",
    },
  ];

  FicheEtudiantIIQuestions: any = [
    {
      title: "Comment qualifieriez-vous l'accueil de votre entreprise ?",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "EtuII1",
    },
    {
      title: "Comment qualifieriez-vous l'encadrement de votre stage ?",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "EtuII2",
    },
    {
      title: "Comment qualifieriez-vous votre adaptation au sein de l'établissement d'accueil ?",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "EtuII3",
    },
    {
      title: "Les conditions matérielles vous ont permis d’atteindre les objectifs de votre stage :",
      type: "multiple-choice",
      texte: [
              "Tout à fait d'accord",
              "Plutôt d'accord",
              "Plutôt pas d'accord",
              "Pas du tout d'accord",
             ],
      controlName: "EtuII4",
    },
    {
      title: "Avez-vous exercé des responsabilités ?",
      type: "EtuII5",
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
      controlName: "EtuII5",
    },
    {
      title: "Votre stage avait-il une dimension internationale ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuII6",
    },
  ];

  FicheEtudiantIIIQuestions: any = [
    {
      title: "Votre sujet de stage était à l'origine : (récuperation du sujet depuis la convention). L'avez-vous modifié ?",
      type: "EtuIII1",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII1",
    },
    {
      title: "Selon vous, le stage a-t-il bien été en adéquation avec votre formation ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII2",
    },
    {
      title: "Les missions confiées ont été ?",
      type: "multiple-choice",
      texte: [
              "Très au-dessous de vos compétences",
              "Au-dessous de vos compétences",
              "A votre niveau de compétences",
              "Au-dessus de vos compétences",
              "Très au-dessus de vos compétences",
              "Inatteignables",
             ],
      controlName: "EtuIII3",
    },
    {
      title: "Ce stage vous a-t-il permis d'acquérir :",
      type: "multiple-boolean",
      texte: [
              "Compétences techniques Oui / Non",
              "Nouvelles méthodologies Oui / Non",
              "Nouvelles connaissances théoriques Oui / Non",
             ],
      controlName: "EtuIII4",
    },
    {
      title: "Ce stage vous a permis de progresser dans la construction de votre projet personnel et professionnel :",
      type: "multiple-choice",
      texte: [
              "Tout à fait d'accord",
              "Plutôt d'accord",
              "Sans avis",
              "Plutôt pas d'accord",
              "Pas du tout d'accord",
             ],
      controlName: "EtuIII5",
    },
    {
      title: "Ce stage vous paraît déterminant à cette étape de votre formation :",
      type: "multiple-choice",
      texte: [
              "Tout à fait d'accord",
              "Plutôt d'accord",
              "Sans avis",
              "Plutôt pas d'accord",
              "Pas du tout d'accord",
             ],
      controlName: "EtuIII6",
    },
    {
      title: "Votre travail a-t-il abouti à une réorganisation du travail ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII7",
    },
    {
      title: "Allez-vous valoriser cette expérience dans une prochaine recherche d'emploi/stage ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII8",
    },
    {
      title: "Votre travail va-t-il donner lieu à un dépôt de brevet ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII9",
    },
    {
      title: "Avez-vous reçu une attestation de stage ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII10",
    },
    {
      title: "Avez-vous rencontré des difficultés à percevoir votre gratification ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII11",
    },
    {
      title: "Ce stage a-t-il donné lieu à une proposition d'emploi ou d'alternance ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII12",
    },
    {
      title: "Conseilleriez-vous cet établissement d'accueil à un autre étudiant ?",
      type: "multiple-choice",
      texte: [
              "Tout à fait d'accord",
              "Plutôt d'accord",
              "Sans avis",
              "Plutôt pas d'accord",
              "Pas du tout d'accord",
             ],
      controlName: "EtuIII13",
    },
    {
      title: "Indiquez votre appréciation générale sur le stage :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "EtuIII14",
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
      controlName: "EnsI1",
    },
    {
      title: "Modalité(s) d’échange(s) avec le tuteur professionnel :",
      texte: [
              "Téléphone Oui / Non",
              "Mail Oui / Non",
              "Rencontre Oui / Non",
             ],
      controlName: "EnsI2",
    },
    {
      title: "Commentaire(s) :",
      texte: [
              "Champ de texte libre",
             ],
      controlName: "EnsI3",
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
      controlName: "EnsII1",
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
      controlName: "EnsII2",
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
      controlName: "EnsII3",
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
      controlName: "EnsII4",
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
      controlName: "EnsII5",
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
      controlName: "EnsII6",
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
      controlName: "EnsII7",
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
      controlName: "EnsII8",
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
      controlName: "EnsII9",
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
      controlName: "EnsII10",
    },
    {
      title: "Commentaires :",
      texte: [
              "Champ de texte libre",
             ],
      controlName: "EnsII11",
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
      controlName: "Ent1",
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
      controlName: "Ent2",
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
      controlName: "Ent3",
    },
    {
      title: "ntérêt pour l'établissement, les services, et les métiers :",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      controlName: "Ent4",
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
      controlName: "Ent5",
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
      controlName: "Ent6",
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
      controlName: "Ent7",
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
      controlName: "Ent8",
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
      controlName: "Ent9",
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
      controlName: "Ent10",
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
      controlName: "Ent11",
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
      controlName: "Ent12",
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
      controlName: "Ent13",
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
      controlName: "Ent14",
    },
  ]

  FicheEntrepriseIIIQuestions: any = [
    {
      title: "Les objectifs ont-ils été atteints ?",
      texte: [
              "Non, il est automatiquement",
              "Non, je l’ai trouvé assez f",
              "Oui j’ai eu des difficultés",
             ],
      controlName: "Ent15",
    },
    {
      title: "Indiquez votre appréciation générale de ce stage :",
      texte: [
              "Non, il est automatiquement",
              "Non, je l’ai trouvé assez f",
              "Oui j’ai eu des difficultés",
             ],
      controlName: "Ent16",
    },
    {
      title: "Observations :",
      texte: [
              "Non, il est automatiquement",
              "Non, je l’ai trouvé assez f",
              "Oui j’ai eu des difficultés",
             ],
      controlName: "Ent17",
    },
    {
      title: "Avez-vous remis au stagiaire une attestation de stage ?",
      texte: [
              "Non, il est automatiquement",
              "Non, je l’ai trouvé assez f",
              "Oui j’ai eu des difficultés",
             ],
      controlName: "Ent18",
    },
    {
      title: "Accepteriez-vous de reprendre un de nos étudiants en stage ?",
      texte: [
              "Non, il est automatiquement",
              "Non, je l’ai trouvé assez f",
              "Oui j’ai eu des difficultés",
             ],
      controlName: "Ent19",
    },
  ]

  constructor(public reponseEvaluationService: ReponseEvaluationService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private authService: AuthService,
  ) {
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
      reponseEtuIII3: [null],
      reponseEtuIII3bis: [null],
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
      reponseEtuIII13: [null],
      reponseEtuIII14: [null],
      reponseEtuIII15: [null],
      reponseEtuIII15bis: [null],
      reponseEtuIII16: [null],
      reponseEtuIII16bis: [null],
    });
  }

  ngOnInit(): void {

    this.reponseEvaluationService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.reponseEvaluation = response;

      this.getQuestionSupplementaire();

      this.reponseEtudiantForm.setValue({
        reponseEtuI1: this.reponseEvaluation.reponseEtuI1,
        reponseEtuI1bis: this.reponseEvaluation.reponseEtuI1bis,
        reponseEtuI2: this.reponseEvaluation.reponseEtuI2,
        reponseEtuI3: this.reponseEvaluation.reponseEtuI3,
        reponseEtuI4a: this.reponseEvaluation.reponseEtuI4a,
        reponseEtuI4b: this.reponseEvaluation.reponseEtuI4b,
        reponseEtuI4c: this.reponseEvaluation.reponseEtuI4c,
        reponseEtuI4d: this.reponseEvaluation.reponseEtuI4d,
        reponseEtuI5: this.reponseEvaluation.reponseEtuI5,
        reponseEtuI6: this.reponseEvaluation.reponseEtuI6,
        reponseEtuI7: this.reponseEvaluation.reponseEtuI7,
        reponseEtuI7bis1: this.reponseEvaluation.reponseEtuI7bis1,
        reponseEtuI7bis1a: this.reponseEvaluation.reponseEtuI7bis1a,
        reponseEtuI7bis1b: this.reponseEvaluation.reponseEtuI7bis1b,
        reponseEtuI7bis2: this.reponseEvaluation.reponseEtuI7bis2,
        reponseEtuI8: this.reponseEvaluation.reponseEtuI8,
        reponseEtuII1: this.reponseEvaluation.reponseEtuII1,
        reponseEtuII1bis: this.reponseEvaluation.reponseEtuII1bis,
        reponseEtuII2: this.reponseEvaluation.reponseEtuII2,
        reponseEtuII2bis: this.reponseEvaluation.reponseEtuII2bis,
        reponseEtuII3: this.reponseEvaluation.reponseEtuII3,
        reponseEtuII3bis: this.reponseEvaluation.reponseEtuII3bis,
        reponseEtuII4: this.reponseEvaluation.reponseEtuII4,
        reponseEtuII5: this.reponseEvaluation.reponseEtuII5,
        reponseEtuII5a: this.reponseEvaluation.reponseEtuII5a,
        reponseEtuII5b: this.reponseEvaluation.reponseEtuII5b,
        reponseEtuII6: this.reponseEvaluation.reponseEtuII6,
        reponseEtuIII1: this.reponseEvaluation.reponseEtuIII1,
        reponseEtuIII1bis: this.reponseEvaluation.reponseEtuIII1bis,
        reponseEtuIII2: this.reponseEvaluation.reponseEtuIII2,
        reponseEtuIII2bis: this.reponseEvaluation.reponseEtuIII2bis,
        reponseEtuIII3: this.reponseEvaluation.reponseEtuIII3,
        reponseEtuIII3bis: this.reponseEvaluation.reponseEtuIII3bis,
        reponseEtuIII4: this.reponseEvaluation.reponseEtuIII4,
        reponseEtuIII5a: this.reponseEvaluation.reponseEtuIII5a,
        reponseEtuIII5b: this.reponseEvaluation.reponseEtuIII5b,
        reponseEtuIII5c: this.reponseEvaluation.reponseEtuIII5c,
        reponseEtuIII5bis: this.reponseEvaluation.reponseEtuIII5bis,
        reponseEtuIII6: this.reponseEvaluation.reponseEtuIII6,
        reponseEtuIII6bis: this.reponseEvaluation.reponseEtuIII6bis,
        reponseEtuIII7: this.reponseEvaluation.reponseEtuIII7,
        reponseEtuIII7bis: this.reponseEvaluation.reponseEtuIII7bis,
        reponseEtuIII8: this.reponseEvaluation.reponseEtuIII8,
        reponseEtuIII8bis: this.reponseEvaluation.reponseEtuIII8bis,
        reponseEtuIII9: this.reponseEvaluation.reponseEtuIII9,
        reponseEtuIII9bis: this.reponseEvaluation.reponseEtuIII9bis,
        reponseEtuIII10: this.reponseEvaluation.reponseEtuIII10,
        reponseEtuIII11: this.reponseEvaluation.reponseEtuIII11,
        reponseEtuIII12: this.reponseEvaluation.reponseEtuIII12,
        reponseEtuIII13: this.reponseEvaluation.reponseEtuIII13,
        reponseEtuIII14: this.reponseEvaluation.reponseEtuIII14,
        reponseEtuIII15: this.reponseEvaluation.reponseEtuIII15,
        reponseEtuIII15bis: this.reponseEvaluation.reponseEtuIII15bis,
        reponseEtuIII16: this.reponseEvaluation.reponseEtuIII16,
        reponseEtuIII16bis: this.reponseEvaluation.reponseEtuIII16bis,
      });

    });
  }

  getQuestionSupplementaire(): void {
    //this.ficheEvaluationService.getQuestionsSupplementaires(this.reponseEvaluation.ficheEvaluation.id).subscribe((response: any) => {
    //  this.questionsSupplementaires = [];
    //  this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 0));
    //  this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 1));
    //  this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 2));
    //  this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 3));
    //  this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 4));
    //  this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 5));
    //  this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 6));
    //  this.questionsSupplementaires.push(response.filter((q: any) => q.idPlacement == 7));
    //});
  }

  choose(row: any): void {
    this.edit = false;
    if (this.firstPanel) {
      this.firstPanel.expanded = false;
    }
  }

  editFicheEtudiant(): void {
    this.edit = true;
    this.editEtu = true;
  }

  cancelEdit(): void {
    this.edit = false;
    this.editEtu = false;
    this.editEns = false;
    this.editEnt = false;
  }

  compare(option: any, value: any): boolean {
    if (option && value) {
      return option.id === value.id;
    }
    return false;
  }

  saveReponseEtudiant(): void {
    if (this.reponseEtudiantForm.valid) {

      const data = {...this.reponseEtudiantForm.value};

    }
  }

}

