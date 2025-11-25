package org.esup_portail.esup_stage.enums;

public enum TypeQuestionEvaluation {
    YES_NO,            // Oui / Non
    SINGLE_CHOICE,     // un seul choix dans une liste
    MULTI_CHOICE,      // plusieurs choix (checkbox)
    TEXT,              // texte libre
    BOOLEAN_GROUP,     // ex-“multiple-boolean” => N sous-items oui/non
    SCALE_LIKERT_5,    // Excellent → Insuffisant (indices 0..4)
    SCALE_AGREEMENT_5, // Tout à fait d'accord → Pas du tout d'accord (0..4)
    AUTO               // Récupération automatique depuis la convention
}
