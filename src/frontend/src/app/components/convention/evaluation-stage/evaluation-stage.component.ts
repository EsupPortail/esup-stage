import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";
import { ReponseEvaluationService } from "../../../services/reponse-evaluation.service";
import { FicheEvaluationService } from "../../../services/fiche-evaluation.service";
import { MessageService } from "../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";
import * as FileSaver from 'file-saver';

@Component({
  selector: 'app-evaluation-stage',
  templateUrl: './evaluation-stage.component.html',
  styleUrls: ['./evaluation-stage.component.scss']
})
export class EvaluationStageComponent implements OnInit {

  ficheEvaluation: any;
  reponseEvaluation: any;
  questionsSupplementaires: any;
  @Input() convention: any;

  reponseEtudiantForm: FormGroup;
  reponseEnseignantForm: FormGroup;
  reponseEntrepriseForm: FormGroup;

  reponseSupplementaireEtudiantForm: FormGroup;
  reponseSupplementaireEnseignantForm: FormGroup;
  reponseSupplementaireEntrepriseForm: FormGroup;

  edit: boolean = false;
  editEtu: boolean = false;
  editEns: boolean = false;
  editEnt: boolean = false;

  isEtudiant:boolean = false;
  isEnseignant:boolean = false;
  isGestionnaireOrAdmin:boolean = false;

  @ViewChild("generalPanel") generalPanel: MatExpansionPanel|undefined;

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
              "Prospection directe",
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
              "Par votre réseau personnel",
              "Au sein de votre formation",
              "Par le service d'Information, d'Orientation et d'Insertion Professionnelle",
              "Par le Bureau d'Aide à l'Insertion Professionnelle",
              "Autre",
              "Par choix",
              "Par méconnaissance des dispositifs proposés par votre université",
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
      bisQuestionLowNotation:"Pour quelles raisons ?",
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
      bisQuestionLowNotation:"Pour quelles raisons ?",
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
      bisQuestionLowNotation:"Pour quelles raisons ?",
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
              "Très importantes",
              "Importantes",
              "Peu importantes",
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
      title: "Votre sujet de stage était à l'origine : (récuperation du sujet depuis la convention).",
      type: "EtuIII1",
      texte: [
              "Oui / Non",
             ],
      bisQuestionTrue:"Quel a été le nouveau sujet ?",
      controlName: "EtuIII1",
    },
    {
      title: "Selon vous, le stage a-t-il bien été en adéquation avec votre formation ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      bisQuestionFalse:"Commentaire:",
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
      controlName: "EtuIII4",
    },
    {
      title: "Ce stage vous a-t-il permis d'acquérir :",
      type: "multiple-boolean",
      texte: [
              "Compétences techniques",
              "Nouvelles méthodologies",
              "Nouvelles connaissances théoriques",
             ],
      controlName: "EtuIII5",
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
      bisQuestionLowNotation:"Pour quelles raisons ?",
      controlName: "EtuIII6",
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
      bisQuestionLowNotation:"Pour quelles raisons ?",
      controlName: "EtuIII7",
    },
    {
      title: "Votre travail a-t-il abouti à une réorganisation du travail ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      bisQuestionTrue:"Pouvez-vous en quelques lignes spécifier ce changement ?",
      controlName: "EtuIII8",
    },
    {
      title: "Allez-vous valoriser cette expérience dans une prochaine recherche d'emploi/stage ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      bisQuestionFalse:"Commentaire:",
      controlName: "EtuIII9",
    },
    {
      title: "Votre travail va-t-il donner lieu à un dépôt de brevet ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII10",
    },
    {
      title: "Avez-vous reçu une attestation de stage ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII11",
    },
    {
      title: "Avez-vous rencontré des difficultés à percevoir votre gratification ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII12",
    },
    {
      title: "Ce stage a-t-il donné lieu à une proposition d'emploi ou d'alternance ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "EtuIII13",
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
      bisQuestionLowNotation:"Pour quelles raisons ?",
      controlName: "EtuIII15",
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
      bisQuestionLowNotation:"Pour quelles raisons ?",
      controlName: "EtuIII16",
    },
  ];

  FicheEnseignantIQuestions: any = [
    {
      title: "Modalité(s) d’échange(s) avec le stagiaire :",
      type: "multiple-boolean",
      texte: [
              "Téléphone",
              "Mail",
              "Rencontre",
             ],
      controlName: "EnsI1",
    },
    {
      title: "Modalité(s) d’échange(s) avec le tuteur professionnel :",
      type: "multiple-boolean",
      texte: [
              "Téléphone",
              "Mail",
              "Rencontre",
             ],
      controlName: "EnsI2",
    },
    {
      title: "Commentaire(s) :",
      type: "texte",
      texte: [
              "Champ de texte libre",
             ],
      controlName: "EnsI3",
    },
  ]

  FicheEnseignantIIQuestions: any = [
    {
      title: "Impression générale et présentation de l’étudiant :",
      type: "multiple-choice",
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
      type: "multiple-choice",
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
      type: "multiple-choice",
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
      type: "multiple-choice",
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
      type: "multiple-choice",
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
      type: "multiple-choice",
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
      type: "multiple-choice",
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
      type: "multiple-choice",
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
      type: "multiple-choice",
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
      type: "multiple-choice",
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
      title: "Commentaire(s) :",
      type: "texte",
      texte: [
              "Champ de texte libre",
             ],
      controlName: "EnsII11",
    },
  ]

  FicheEntrepriseIQuestions: any = [
    {
      title: "Adaptation au milieu professionnel :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent1",
    },
    {
      title: "Intégration au groupe de travail :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent2",
    },
    {
      title: "Assiduité - ponctualité :",
      type: "multiple-choice",
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
      title: "Intérêt pour l'établissement, les services, et les métiers :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent4",
    },
    {
      title: "Sens de l'organisation :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent5",
    },
    {
      title: "Capacité d'autonomie :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent6",
    },
    {
      title: "Initiative personnelle :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent7",
    },
    {
      title: "Implication :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent8",
    },
    {
      title: "Rigueur et précision dans le travail :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent9",
    },
  ]

  FicheEntrepriseIIQuestions: any = [
    {
      title: "Aptitude à cerner et situer le projet :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent10",
    },
    {
      title: "Aptitude à appliquer ses connaissances :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent11",
    },
    {
      title: "Esprit d'observation et pertinence des remarques :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent12",
    },
    {
      title: "Esprit de synthèse :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent13",
    },
    {
      title: "Aptitude à la communication :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent14",
    },
  ]

  FicheEntrepriseIIIQuestions: any = [
    {
      title: "Les objectifs ont-ils été atteints ?",
      type: "multiple-choice",
      texte: [
              "Tout à fait d'accord",
              "Plutôt d'accord",
              "Sans avis",
              "Plutôt pas d'accord",
              "Pas du tout d'accord",
             ],
      bisQuestion:"Commentaire (facultatif)",
      controlName: "Ent15",
    },
    {
      title: "Indiquez votre appréciation générale de ce stage :",
      type: "multiple-choice",
      texte: [
              "Excellent",
              "Très bien",
              "Bien",
              "Satisfaisant",
              "Insuffisant",
             ],
      bisQuestionLowNotation:"Pour quelles raisons ?",
      controlName: "Ent16",
    },
    {
      title: "Observations :",
      type: "texte",
      texte: [
              "Champ de texte libre",
             ],
      controlName: "Ent17",
    },
    {
      title: "Avez-vous remis au stagiaire une attestation de stage ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      controlName: "Ent19",
    },
    {
      title: "Accepteriez-vous de reprendre un de nos étudiants en stage ?",
      type: "boolean",
      texte: [
              "Oui / Non",
             ],
      bisQuestionTrue:"Pour quelles raisons ?",
      controlName: "Ent18",
    },
  ]

  constructor(private reponseEvaluationService: ReponseEvaluationService,
              private ficheEvaluationService: FicheEvaluationService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private authService: AuthService,
  ) {
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
      reponseEtuII1bis: [null, [Validators.required]],
      reponseEtuII2: [null, [Validators.required]],
      reponseEtuII2bis: [null, [Validators.required]],
      reponseEtuII3: [null, [Validators.required]],
      reponseEtuII3bis: [null, [Validators.required]],
      reponseEtuII4: [null, [Validators.required]],
      reponseEtuII5: [null, [Validators.required]],
      reponseEtuII5a: [null, [Validators.required]],
      reponseEtuII5b: [null, [Validators.required]],
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
      reponseEtuIII13: [null, [Validators.required]],
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

    //gestion des champs required conditionnels
    for(let question of this.FicheEtudiantIQuestions.concat(this.FicheEtudiantIIQuestions).concat(this.FicheEtudiantIIIQuestions)){
      if(question.bisQuestionLowNotation || question.bisQuestionTrue || question.bisQuestionFalse ||
      question.controlName == 'EtuI7'){
        let key = 'reponse' + question.controlName;
        let bisKey = key + 'bis';
        this.reponseEtudiantForm.get(key)?.valueChanges.subscribe(val => {
          if((question.bisQuestionLowNotation && val>=3) || (question.bisQuestionTrue && val) || (question.bisQuestionFalse && !val) ){
            this.toggleValidators(this.reponseEtudiantForm,[bisKey],true);
          }else if(question.controlName == 'EtuI7'){
            let bisKey1 = key + 'bis1';
            let bisKey2 = key + 'bis2';
            if(val){
              this.toggleValidators(this.reponseEtudiantForm,[bisKey1],true);
              this.toggleValidators(this.reponseEtudiantForm,[bisKey2],false);
            }else{
              this.toggleValidators(this.reponseEtudiantForm,[bisKey2],true);
              this.toggleValidators(this.reponseEtudiantForm,[bisKey1],false);
            }
          }else if(question.controlName == 'EtuII5'){
            let bisKey1 = key + 'a';
            let bisKey2 = key + 'b';
            if(val){
              this.toggleValidators(this.reponseEtudiantForm,[bisKey1],true);
              this.toggleValidators(this.reponseEtudiantForm,[bisKey2],true);
            }else{
              this.toggleValidators(this.reponseEtudiantForm,[bisKey1],false);
              this.toggleValidators(this.reponseEtudiantForm,[bisKey2],false);
            }
          }else{
            this.toggleValidators(this.reponseEtudiantForm,[bisKey],false);
          }
        });
      }
    }
    for(let question of this.FicheEnseignantIQuestions.concat(this.FicheEnseignantIIQuestions)){
      if(question.bisQuestionLowNotation || question.bisQuestionTrue || question.bisQuestionFalse){
        let key = 'reponse' + question.controlName;
        let bisKey = key + 'bis';
        this.reponseEnseignantForm.get(key)?.valueChanges.subscribe(val => {
          if((question.bisQuestionLowNotation && val>=3) || (question.bisQuestionTrue && val) || (question.bisQuestionFalse && !val) ){
            this.toggleValidators(this.reponseEnseignantForm,[bisKey],true);
          }else{
            this.toggleValidators(this.reponseEnseignantForm,[bisKey],false);
          }
        });
      }
    }
    for(let question of this.FicheEntrepriseIQuestions.concat(this.FicheEntrepriseIIQuestions).concat(this.FicheEntrepriseIIIQuestions)){
      if(question.bisQuestionLowNotation || question.bisQuestionTrue || question.bisQuestionFalse){
        let key = 'reponse' + question.controlName;
        let bisKey = key + 'bis';
        this.reponseEntrepriseForm.get(key)?.valueChanges.subscribe(val => {
          if((question.bisQuestionLowNotation && val>=3) || (question.bisQuestionTrue && val) || (question.bisQuestionFalse && !val) ){
            this.toggleValidators(this.reponseEntrepriseForm,[bisKey],true);
          }else{
            this.toggleValidators(this.reponseEntrepriseForm,[bisKey],false);
          }
        });
      }
    }

    this.reponseSupplementaireEtudiantForm = this.fb.group({});
    this.reponseSupplementaireEnseignantForm = this.fb.group({});
    this.reponseSupplementaireEntrepriseForm = this.fb.group({});
  }

  toggleValidators(form: FormGroup, keys: string[],toggle: boolean): void {
    keys.forEach((key: string) => {
      if (toggle){
        form.get(key)!.addValidators(Validators.required);
      }else{
        form.get(key)!.clearValidators();
      }
      form.get(key)!.updateValueAndValidity();
    });
  }

  ngOnInit(): void {

    this.isEtudiant = this.authService.isEtudiant();
    this.isEnseignant= this.authService.isEnseignant();
    this.isGestionnaireOrAdmin = this.authService.isGestionnaire() || this.authService.isAdmin() ;

    this.ficheEvaluationService.getByCentreGestion(this.convention.centreGestion.id).subscribe((response: any) => {

      this.ficheEvaluation = response;

      if(this.ficheEvaluation){
        this.reponseEvaluationService.getByConvention(this.convention.id).subscribe((response2: any) => {
          this.reponseEvaluation = response2;
          this.getQuestionSupplementaire();
          if(this.reponseEvaluation){

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
              reponseEtuIII15: this.reponseEvaluation.reponseEtuIII15,
              reponseEtuIII15bis: this.reponseEvaluation.reponseEtuIII15bis,
              reponseEtuIII16: this.reponseEvaluation.reponseEtuIII16,
              reponseEtuIII16bis: this.reponseEvaluation.reponseEtuIII16bis,
            });

            this.reponseEnseignantForm.setValue({
              reponseEnsI1a: this.reponseEvaluation.reponseEnsI1a,
              reponseEnsI1b: this.reponseEvaluation.reponseEnsI1b,
              reponseEnsI1c: this.reponseEvaluation.reponseEnsI1c,
              reponseEnsI2a: this.reponseEvaluation.reponseEnsI2a,
              reponseEnsI2b: this.reponseEvaluation.reponseEnsI2b,
              reponseEnsI2c: this.reponseEvaluation.reponseEnsI2c,
              reponseEnsI3: this.reponseEvaluation.reponseEnsI3,
              reponseEnsII1: this.reponseEvaluation.reponseEnsII1,
              reponseEnsII2: this.reponseEvaluation.reponseEnsII2,
              reponseEnsII3: this.reponseEvaluation.reponseEnsII3,
              reponseEnsII4: this.reponseEvaluation.reponseEnsII4,
              reponseEnsII5: this.reponseEvaluation.reponseEnsII5,
              reponseEnsII6: this.reponseEvaluation.reponseEnsII6,
              reponseEnsII7: this.reponseEvaluation.reponseEnsII7,
              reponseEnsII8: this.reponseEvaluation.reponseEnsII8,
              reponseEnsII9: this.reponseEvaluation.reponseEnsII9,
              reponseEnsII10: this.reponseEvaluation.reponseEnsII10,
              reponseEnsII11: this.reponseEvaluation.reponseEnsII11,
            });

            this.reponseEntrepriseForm.setValue({
              reponseEnt1: this.reponseEvaluation.reponseEnt1,
              reponseEnt1bis: this.reponseEvaluation.reponseEnt1bis,
              reponseEnt2: this.reponseEvaluation.reponseEnt2,
              reponseEnt2bis: this.reponseEvaluation.reponseEnt2bis,
              reponseEnt3: this.reponseEvaluation.reponseEnt3,
              reponseEnt4: this.reponseEvaluation.reponseEnt4,
              reponseEnt4bis: this.reponseEvaluation.reponseEnt4bis,
              reponseEnt5: this.reponseEvaluation.reponseEnt5,
              reponseEnt5bis: this.reponseEvaluation.reponseEnt5bis,
              reponseEnt6: this.reponseEvaluation.reponseEnt6,
              reponseEnt6bis: this.reponseEvaluation.reponseEnt6bis,
              reponseEnt7: this.reponseEvaluation.reponseEnt7,
              reponseEnt7bis: this.reponseEvaluation.reponseEnt7bis,
              reponseEnt8: this.reponseEvaluation.reponseEnt8,
              reponseEnt8bis: this.reponseEvaluation.reponseEnt8bis,
              reponseEnt9: this.reponseEvaluation.reponseEnt9,
              reponseEnt9bis: this.reponseEvaluation.reponseEnt9bis,
              reponseEnt10: this.reponseEvaluation.reponseEnt10,
              reponseEnt10bis: this.reponseEvaluation.reponseEnt10bis,
              reponseEnt11: this.reponseEvaluation.reponseEnt11,
              reponseEnt11bis: this.reponseEvaluation.reponseEnt11bis,
              reponseEnt12: this.reponseEvaluation.reponseEnt12,
              reponseEnt12bis: this.reponseEvaluation.reponseEnt12bis,
              reponseEnt13: this.reponseEvaluation.reponseEnt13,
              reponseEnt13bis: this.reponseEvaluation.reponseEnt13bis,
              reponseEnt14: this.reponseEvaluation.reponseEnt14,
              reponseEnt14bis: this.reponseEvaluation.reponseEnt14bis,
              reponseEnt15: this.reponseEvaluation.reponseEnt15,
              reponseEnt15bis: this.reponseEvaluation.reponseEnt15bis,
              reponseEnt16: this.reponseEvaluation.reponseEnt16,
              reponseEnt16bis: this.reponseEvaluation.reponseEnt16bis,
              reponseEnt17: this.reponseEvaluation.reponseEnt17,
              reponseEnt17bis: this.reponseEvaluation.reponseEnt17bis,
              reponseEnt18: this.reponseEvaluation.reponseEnt18,
              reponseEnt18bis: this.reponseEvaluation.reponseEnt18bis,
              reponseEnt19: this.reponseEvaluation.reponseEnt19,
            });
          }
        });
      }
    });
  }

  getQuestionSupplementaire(): void {

    this.ficheEvaluationService.getQuestionsSupplementaires(this.ficheEvaluation.id).subscribe((response: any) => {

      let questionsSupplementaires = response;

      for(let questionSupplementaire of questionsSupplementaires){

        let form = this.fb.group({});

        if(questionSupplementaire.idPlacement == 0 || questionSupplementaire.idPlacement == 1 || questionSupplementaire.idPlacement == 2){
          form = this.reponseSupplementaireEtudiantForm;
        }

        if(questionSupplementaire.idPlacement == 3 || questionSupplementaire.idPlacement == 4){
          form = this.reponseSupplementaireEnseignantForm;
        }

        if(questionSupplementaire.idPlacement == 5 || questionSupplementaire.idPlacement == 6 || questionSupplementaire.idPlacement == 7){
          form = this.reponseSupplementaireEntrepriseForm;
        }
        const questionSupplementaireFormControlName = 'questionSupplementaire' + questionSupplementaire.id
        form.addControl(questionSupplementaireFormControlName,new FormControl(null, Validators.required));
        questionSupplementaire.formControlName = questionSupplementaireFormControlName

         if(this.reponseEvaluation){
          this.reponseEvaluationService.getReponseSupplementaire(this.convention.id, questionSupplementaire.id).subscribe((response2: any) => {

            questionSupplementaire.reponse = false;
            if (response2){
              questionSupplementaire.reponse = true;
              if(questionSupplementaire.typeQuestion == 'txt'){
                form.get(questionSupplementaireFormControlName)!.setValue(response2.reponseTxt);
              }
              if(questionSupplementaire.typeQuestion == 'not'){
                form.get(questionSupplementaireFormControlName)!.setValue(response2.reponseInt);
              }
              if(questionSupplementaire.typeQuestion == 'yn'){
                form.get(questionSupplementaireFormControlName)!.setValue(response2.reponseBool);
              }
            }
          });
        }
      }

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

  editFicheEtudiant(): void {
    this.edit = true;
    this.editEtu = true;
    this.generalPanel!.expanded = false;
  }

  editFicheEnseignant(): void {
    this.edit = true;
    this.editEns = true;
    this.generalPanel!.expanded = false;
  }

  editFicheEntreprise(): void {
    this.edit = true;
    this.editEnt = true;
    this.generalPanel!.expanded = false;
  }

  cancelEdit(): void {
    this.edit = false;
    this.editEtu = false;
    this.editEns = false;
    this.editEnt = false;
    this.generalPanel!.expanded = true;
  }

  compare(option: any, value: any): boolean {
    if (option && value) {
      return option.id === value.id;
    }
    return false;
  }

  saveReponse(typeFiche: number): void {

    let reponseForm = this.fb.group({});
    let reponseSupplementaireForm = this.fb.group({});
    let questionsSupplementaires = [];

    if(typeFiche == 0){
      reponseForm = this.reponseEtudiantForm;
      reponseSupplementaireForm = this.reponseSupplementaireEtudiantForm;
      questionsSupplementaires = this.questionsSupplementaires[0].concat(this.questionsSupplementaires[1]).concat(this.questionsSupplementaires[2]);
    }

    if(typeFiche == 1){
      reponseForm = this.reponseEnseignantForm;
      reponseSupplementaireForm = this.reponseSupplementaireEnseignantForm;
      questionsSupplementaires = this.questionsSupplementaires[3].concat(this.questionsSupplementaires[4]);
    }

    if(typeFiche == 2){
      reponseForm = this.reponseEntrepriseForm;
      reponseSupplementaireForm = this.reponseSupplementaireEntrepriseForm;
      questionsSupplementaires = this.questionsSupplementaires[5].concat(this.questionsSupplementaires[6]).concat(this.questionsSupplementaires[7]);
    }

    for (const name in reponseForm.controls) {
        if (reponseForm.controls[name].invalid) {
        }
    }
    const valid = reponseForm.valid && reponseSupplementaireForm.valid

    const data = {...reponseForm.value};

    for(let questionSupplementaire of questionsSupplementaires){
      let reponseSupplementaireData = {'reponseTxt':null,'reponseInt':null,'reponseBool':null,};
      if(questionSupplementaire.typeQuestion == 'txt'){
        reponseSupplementaireData.reponseTxt = reponseSupplementaireForm.get(questionSupplementaire.formControlName)!.value;
      }
      if(questionSupplementaire.typeQuestion == 'not'){
        reponseSupplementaireData.reponseInt = reponseSupplementaireForm.get(questionSupplementaire.formControlName)!.value;
      }
      if(questionSupplementaire.typeQuestion == 'yn'){
        reponseSupplementaireData.reponseBool = reponseSupplementaireForm.get(questionSupplementaire.formControlName)!.value;
      }
      if(questionSupplementaire.reponse){
        this.reponseEvaluationService.updateReponseSupplementaire(this.convention.id, questionSupplementaire.id, reponseSupplementaireData).subscribe((response: any) => {
        });
      }else{
        this.reponseEvaluationService.createReponseSupplementaire(this.convention.id, questionSupplementaire.id, reponseSupplementaireData).subscribe((response: any) => {
        });
      }
    }

    if(typeFiche == 0){
      if(this.reponseEvaluation){
        this.reponseEvaluationService.updateReponseEtudiant(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }else{
        this.reponseEvaluationService.createReponseEtudiant(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }
    }

    if(typeFiche == 1){
      if(this.reponseEvaluation){
        this.reponseEvaluationService.updateReponseEnseignant(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }else{
        this.reponseEvaluationService.createReponseEnseignant(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }
    }

    if(typeFiche == 2){
      if(this.reponseEvaluation){
        this.reponseEvaluationService.updateReponseEntreprise(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }else{
        this.reponseEvaluationService.createReponseEntreprise(this.convention.id,valid, data).subscribe((response: any) => {
          this.reponseEvaluation = response;
          if(valid){
            this.messageService.setSuccess('Evaluation enregistrée avec succès');
          }else{
            this.messageService.setWarning('Evaluation enregistrée avec succès, mais certains champs restent à remplir');
          }
        });
      }
    }
  }

  getQestionTexte(question: any): string {

    let htmlTexte = "";

    htmlTexte += "<p style=\"margin-left: 16px\"><span class=\"text-small\"><strong> - "+question.title+"</strong></span></p>";

    if(question.type == 'boolean'){
      if(this.reponseEvaluation['reponse' + question.controlName]){
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Oui</span></p>";
      }else{
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Non</span></p>";
      }
    }
    if(question.type == 'multiple-choice'){
      let line = question.texte[this.reponseEvaluation['reponse' + question.controlName]];
      htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";

      if(question.bisQuestionLowNotation &&
      (this.reponseEtudiantForm.get('reponse' + question.controlName)?.value !== null) &&
      (this.reponseEtudiantForm.get('reponse' + question.controlName)?.value >= 3)){
        let line = question.bisQuestionLowNotation;
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong> - "+line+"</strong>";
        let formControlName = "reponse" + question.controlName + 'bis';
        line = this.reponseEvaluation[formControlName];
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
      }
      if(question.bisQuestion){
        let line = question.bisQuestion;
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong> - "+line+"</strong>";
        let formControlName = "reponse" + question.controlName + 'bis';
        line = this.reponseEvaluation[formControlName];
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line??'/'+"</span></p>";
      }
    }
    if(question.type == 'multiple-boolean'){
      for (var i = 0; i < question.texte.length; i++) {
        let line = question.texte[i];
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong> - "+line+" : </strong>";
        let formControlName = "reponse" + question.controlName + this.controlsIndexToLetter[i];
        if(this.reponseEvaluation[formControlName]){
          htmlTexte += "Oui</span></p>";
        }else{
          htmlTexte += "Non</span></p>";
        }
      }
    }
    if(question.type == 'texte'){
      let line = question.texte[this.reponseEvaluation['reponse' + question.controlName]];
      htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
    }
    if(question.type == 'EtuI5'){
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+this.convention.origineStage.libelle+"</span></p>";
    }
    if(question.type == 'EtuI7'){
      if(this.reponseEvaluation['reponse' + question.controlName]){
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Oui</span></p>";
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong>- Si oui, par qui ?</strong></span></p>";
        let line = question.texte[this.reponseEvaluation['reponseEtuI7bis1']];
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
      }else{
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Non</span></p>";
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong>- Si non, pourquoi ?</strong></span></p>";
        let line = question.texte[this.reponseEvaluation['reponseEtuI7bis2']];
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
      }
    }
    if(question.type == 'EtuII5'){
      if(this.reponseEvaluation['reponse' + question.controlName]){
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Oui</span></p>";
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong>- Si oui : a) De quel ordre ?</strong></span></p>";
        let line = question.texte[this.reponseEvaluation['reponseEtuII5a']];
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+line+"</span></p>";
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\"><strong>b) Avec autonomie ?</strong></span></p>";
        if(this.reponseEvaluation['reponseEtuII5b']){
          htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Oui</span></p>";
        }else{
          htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">Non</span></p>";
        }
      }
    }
    if(question.type == 'EtuIII1'){
        htmlTexte += "<p style=\"margin-left: 32px\"><span class=\"text-small\">"+this.convention.sujetStage+"</span></p>";
    }

    return htmlTexte;
  }

  printFiche(typeFiche: number): void {

    let htmlTexte = "";

    if (typeFiche==0){
      htmlTexte += "<p style=\"text-align:center;\"><span class=\"text-huge\"><strong>Evaluation du stage par le tuteur pédagogique</strong></span></p>";
      htmlTexte += "<p style=\"text-align:center;\"><span class=\"text-small\"><strong>Convention de stage n°"+this.convention.id+"</strong></span></p>";
      htmlTexte += "<p><span class=\"text-small\"><strong>I. <u>Avant le départ en stage</u></strong></span></p>";
      for(let question of this.FicheEtudiantIQuestions){
        if(this.ficheEvaluation['question' + question.controlName]){
          htmlTexte += this.getQestionTexte(question);
        }
      }
      htmlTexte += "<p><span class=\"text-small\"><strong>II. <u>Pendant le stage</u></strong></span></p>";
      for(let question of this.FicheEtudiantIIQuestions){
        if(this.ficheEvaluation['question' + question.controlName]){
          htmlTexte += this.getQestionTexte(question);
        }
      }
      htmlTexte += "<p><span class=\"text-small\"><strong>III. <u>Après le stage</u></strong></span></p>";
      for(let question of this.FicheEtudiantIIIQuestions){
        if(this.ficheEvaluation['question' + question.controlName]){
          htmlTexte += this.getQestionTexte(question);
        }
      }
    }

    this.reponseEvaluationService.getFichePDF(this.convention.id, typeFiche, htmlTexte).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: "application/pdf"});
      let filename;
      if (typeFiche==0){
        filename = 'FicheEtudiant_' + this.convention.id + '.pdf';
      }
      if (typeFiche==1){
        filename = 'FicheEnseignant_' + this.convention.id + '.pdf';
      }
      if (typeFiche==2){
        filename = 'FicheEntreprise_' + this.convention.id + '.pdf';
      }
      FileSaver.saveAs(blob, filename);
    });
  }

  envoiMailEvaluation(typeFiche: number): void {
    this.reponseEvaluationService.sendMailEvaluation(this.convention.id, typeFiche).subscribe((response: any) => {
      this.messageService.setSuccess('Mail envoyé avec succès');
    });
  }

}

