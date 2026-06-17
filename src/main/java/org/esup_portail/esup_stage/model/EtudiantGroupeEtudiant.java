package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "EtudiantGroupeEtudiant", uniqueConstraints = {@UniqueConstraint(name = "uniq_EtudiantGroupeEtudiant_Etudiant_GroupeEtudiant", columnNames = {"idEtudiant", "idGroupeEtudiant"})})
public class EtudiantGroupeEtudiant implements Exportable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEtudiantGroupeEtudiant", nullable = false)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idEtudiant", nullable = false)
    private Etudiant etudiant;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idGroupeEtudiant", nullable = false)
    private GroupeEtudiant groupeEtudiant;

    @OneToOne
    @JoinColumn(name = "idConvention", nullable = false)
    private Convention convention;

    @OneToOne
    @JoinColumn(name = "idMergedConvention")
    private Convention mergedConvention;

    public int getEtudiantId() {
        return etudiant.getId();
    }

    public String getEtudiantNumEtudiant() {
        return etudiant.getNumEtudiant();
    }

    public String getExportValue(String key) {
        return null;
    }
}
