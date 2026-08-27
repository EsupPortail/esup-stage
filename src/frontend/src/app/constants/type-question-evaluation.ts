export enum TypeQuestionEvaluation {
  YES_NO= 'YES_NO',                         // Oui / Non
  SINGLE_CHOICE = 'SINGLE_CHOICE',          // un seul choix dans une liste
  MULTI_CHOICE = 'MULTI_CHOICE',            // plusieurs choix (checkbox)
  TEXT = 'TEXT',                            // texte libre
  BOOLEAN_GROUP = 'BOOLEAN_GROUP',          // ex-“multiple-boolean” => N sous-items oui/non
  SCALE_LIKERT_5 = 'SCALE_LIKERT_5',        // Excellent → Insuffisant (indices 0..4)
  SCALE_AGREEMENT_5 = 'SCALE_AGREEMENT_5',  // Tout à fait d'accord → Pas du tout d'accord (0..4)
  AUTO = 'AUTO'                             // Récupération automatique depuis la convention
}
