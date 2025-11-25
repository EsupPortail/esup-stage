package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Consigne")
@Data
public class Consigne extends ObjetMetier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idConsigne", nullable = false)
    private int id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "idCentreGestion", unique = true, nullable = false)
    private CentreGestion centreGestion;

    @Lob
    @Column
    private String texte;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "consigne", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE,CascadeType.ALL,CascadeType.DETACH}, orphanRemoval = true)
    private List<ConsigneDocument> documents = new ArrayList<>();

    public void addDocument(ConsigneDocument document) {
        document.setConsigne(this);
        this.documents.add(document);
    }

    public void removeDoc(ConsigneDocument document) {
        this.documents.remove(document);
    }
}
