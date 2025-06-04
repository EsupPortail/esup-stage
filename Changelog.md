## Version stable  
[Esup-Stage v2.1.9](https://github.com/EsupPortail/esup-stage/releases/tag/2.1.9)  

---

## ESUP-Stage v3.1.0 (À venir)  
### Nouveautés  
- Évolution de la gestion des établissements d'accueil (API Sirene).  
- Création de la variable `convention.horaireIrregulier` (#219).
- Ajout d'une bulle d'aide près du bouton d'import des organismes d'accueil.
- Intégration de la nouvelle API de consultation des étapes de signature dans Esup-Signature.
- Alerte des gestionnaire lorsqu'une convention est signée électroniquement.

### Améliorations  
- Possibilité de saisir manuellement la durée en mois, jour(s) et heure(s) (#212).
- Amélioration de l'export Excel (#233).
- Tri des thématiques de stage par ordre alphabétique.

---

## ESUP-Stage v3.0.0 (À venir)  
### Nouveautés  
- Ajout d'un indicateur de suivi de la signature électronique dans le tableau de bord (#173).
- Ajout d'un filtre de recherche par état de signature électronique dans le tableau de bord (#213).

### Améliorations  
- Accès au suivi des signatures pour les étudiants et les gestionnaires (#231).  
- Suppression de l'impression du récapitulatif et de la convention en mode brouillon.  
- Suppression des requêtes sur Apogée après la création d'une convention (#190).  

### Corrections  
- Correction des actions du tableau de bord qui contournaient le processus de validation (#230).  
- Correction d'un problème d'initialisation des valeurs dans la table `CentreGestion` (#191).  
- Correction de l'affichage erroné du message d'erreur "Ce rôle ne donne pas accès..." lorsque le paramétrage du centre de gestion est activé (#185).  
- Correction de l'erreur de calcul des horaires de travail irréguliers (#219).  
- Correction d'un problème de notification des enseignants (#193).  
- Initialisation des données dans la table `ConventionNomenclature` en mode brouillon.  
- Correction du problème de mise à jour des informations de signature électronique (#227).
- Le département n'était pas affiché lors de la recherche d'un enseignant référent.
