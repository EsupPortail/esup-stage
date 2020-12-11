package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the ReponseSupplementaire database table.
 *
 */
@Entity
@Table(name = "ReponseSupplementaire")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "ReponseSupplementaire.findAll", query = "SELECT r FROM ReponseSupplementaire r")
public class ReponseSupplementaire implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    private ReponseSupplementairePK id;
    private boolean reponseBool;
    private Integer reponseInt;
    @Lob
    private String reponseTxt;
    // bi-directional many-to-one association to Convention
    @ManyToOne
    @JoinColumn(name = "idConvention", nullable = false, insertable = false, updatable = false)
    private Convention convention;
    // bi-directional many-to-one association to QuestionSupplementaire
    @ManyToOne
    @JoinColumn(name = "idQuestionSupplementaire", nullable = false, insertable = false, updatable = false)
    private QuestionSupplementaire questionSupplementaire;

}