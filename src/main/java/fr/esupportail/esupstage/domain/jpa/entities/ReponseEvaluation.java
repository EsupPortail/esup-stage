package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the ReponseEvaluation database table.
 *
 */
@Entity
@Table(name = "ReponseEvaluation")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "ReponseEvaluation.findAll", query = "SELECT r FROM ReponseEvaluation r")
public class ReponseEvaluation implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    private ReponseEvaluationPK id;
    private boolean impressionEnseignant;
    private boolean impressionEntreprise;
    private boolean impressionEtudiant;
    private boolean reponseEnsI1a;
    private boolean reponseEnsI1b;
    private boolean reponseEnsI1c;
    private boolean reponseEnsI2a;
    private boolean reponseEnsI2b;
    private boolean reponseEnsI2c;
    @Lob
    private String reponseEnsI3;
    private Integer reponseEnsII1;
    private Integer reponseEnsII10;
    @Lob
    private String reponseEnsII11;
    private Integer reponseEnsII2;
    private Integer reponseEnsII3;
    private Integer reponseEnsII4;
    private Integer reponseEnsII5;
    private Integer reponseEnsII6;
    private Integer reponseEnsII7;
    private Integer reponseEnsII8;
    private Integer reponseEnsII9;
    private Integer reponseEnt1;
    private boolean reponseEnt10;
    @Lob
    private String reponseEnt10bis;
    private Integer reponseEnt11;
    @Lob
    private String reponseEnt11bis;
    private Integer reponseEnt12;
    @Lob
    private String reponseEnt12bis;
    private Integer reponseEnt13;
    @Lob
    private String reponseEnt13bis;
    private Integer reponseEnt14;
    @Lob
    private String reponseEnt14bis;
    private Integer reponseEnt15;
    @Lob
    private String reponseEnt15bis;
    private Integer reponseEnt16;
    @Lob
    private String reponseEnt16bis;
    private Integer reponseEnt17;
    @Lob
    private String reponseEnt17bis;
    private boolean reponseEnt18;
    @Lob
    private String reponseEnt18bis;
    @Lob
    private String reponseEnt19;
    @Lob
    private String reponseEnt1bis;
    private Integer reponseEnt2;
    @Lob
    private String reponseEnt2bis;
    private Integer reponseEnt3;
    private Integer reponseEnt4;
    @Lob
    private String reponseEnt4bis;
    private Integer reponseEnt5;
    @Lob
    private String reponseEnt5bis;
    private Integer reponseEnt6;
    @Lob
    private String reponseEnt6bis;
    private Integer reponseEnt7;
    @Lob
    private String reponseEnt7bis;
    private Integer reponseEnt8;
    @Lob
    private String reponseEnt8bis;
    private Integer reponseEnt9;
    @Lob
    private String reponseEnt9bis;
    private Integer reponseEtuI1;
    @Lob
    private String reponseEtuI1bis;
    private Integer reponseEtuI2;
    private Integer reponseEtuI3;
    private boolean reponseEtuI4a;
    private boolean reponseEtuI4b;
    private boolean reponseEtuI4c;
    private boolean reponseEtuI4d;
    private Integer reponseEtuI5;
    private Integer reponseEtuI6;
    private boolean reponseEtuI7;
    private Integer reponseEtuI7bis1;
    private boolean reponseEtuI7bis1a;
    @Lob
    private String reponseEtuI7bis1b;
    private Integer reponseEtuI7bis2;
    private boolean reponseEtuI8;
    private Integer reponseEtuII1;
    @Lob
    private String reponseEtuII1bis;
    private Integer reponseEtuII2;
    @Lob
    private String reponseEtuII2bis;
    private Integer reponseEtuII3;
    @Lob
    private String reponseEtuII3bis;
    private Integer reponseEtuII4;
    private boolean reponseEtuII5;
    private Integer reponseEtuII5a;
    private boolean reponseEtuII5b;
    private boolean reponseEtuII6;
    private boolean reponseEtuIII1;
    private boolean reponseEtuIII10;
    private boolean reponseEtuIII11;
    private boolean reponseEtuIII12;
    private boolean reponseEtuIII13;
    private boolean reponseEtuIII14;
    private Integer reponseEtuIII15;
    @Lob
    private String reponseEtuIII15bis;
    private Integer reponseEtuIII16;
    @Lob
    private String reponseEtuIII16bis;
    @Lob
    private String reponseEtuIII1bis;
    private boolean reponseEtuIII2;
    @Lob
    private String reponseEtuIII2bis;
    private boolean reponseEtuIII3;
    @Lob
    private String reponseEtuIII3bis;
    private Integer reponseEtuIII4;
    private boolean reponseEtuIII5a;
    private boolean reponseEtuIII5b;
    @Lob
    private String reponseEtuIII5bis;
    private boolean reponseEtuIII5c;
    private Integer reponseEtuIII6;
    @Lob
    private String reponseEtuIII6bis;
    private Integer reponseEtuIII7;
    @Lob
    private String reponseEtuIII7bis;
    private boolean reponseEtuIII8;
    @Lob
    private String reponseEtuIII8bis;
    private boolean reponseEtuIII9;
    @Lob
    private String reponseEtuIII9bis;
    private boolean validationEnseignant;
    private boolean validationEntreprise;
    private boolean validationEtudiant;
    // bi-directional many-to-one association to Convention
    @ManyToOne
    @JoinColumn(name = "idConvention", nullable = false, insertable = false, updatable = false)
    private Convention convention;
    // bi-directional many-to-one association to FicheEvaluation
    @ManyToOne
    @JoinColumn(name = "idFicheEvaluation", nullable = false, insertable = false, updatable = false)
    private FicheEvaluation ficheEvaluation;


}