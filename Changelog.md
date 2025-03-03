## Version stable
[Esup-Stage v2.1.9](https://github.com/EsupPortail/esup-stage/releases/tag/2.1.9)

##  ESUP-Stage v3.0.1 (A venir)
- Evolution de la gestion des établissements d'accueil (API Sirene)
- Créer la variable convention.horaireIrregulier #219
- Permettre de saisir manuellement la durée en mois, jour(s) et heure(s) #212

## ESUP-Stage v3.0.0 (A venir)
### Nouveauté
- Ajout d'un témoin de suivi de la signature électronique dans le tableau de bord #173

### Améliorations
- Permettre aux étudiants et aux gestionnaires d'accéder au suivi des signatures #231
- L'impression du récapitulatif et de la convention en mode brouillon n'est plus possible
- Ne plus requêter sur Apogée après création de la convention #190

### Corrections
- Correction des actions du dashboard qui outrepassaient le processus de validation #230
- Correction du problème d'initialisation de valeurs dans la table CentreGestion #191
- Correction de l'affichage erroné du message d'erreur "Ce rôle ne donne pas accès..." quand le paramétrage du centre de gestion est activé #185
- Correction de l'erreur de calcul des horaires de travail irréguliers #219
- Correction du problème de notification des enseignants #193
- En mode brouillon, initialisation des données dans la table ConventionNomenclature
- Correction du problème de mise à jour des infos de signature de signature électronique #22
