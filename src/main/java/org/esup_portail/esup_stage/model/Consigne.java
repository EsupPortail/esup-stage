package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Consigne")
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
    @Column(nullable = false)
    private String texte;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "consigne", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<ConsigneDocument> documents = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CentreGestion getCentreGestion() {
        return centreGestion;
    }

    public void setCentreGestion(CentreGestion centreGestion) {
        this.centreGestion = centreGestion;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public List<ConsigneDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<ConsigneDocument> documents) {
        this.documents = documents;
    }

    public void addDocument(ConsigneDocument document) {
        document.setConsigne(this);
        this.documents.add(document);
    }

    public void removeDoc(ConsigneDocument document) {
        this.documents.remove(document);
    }
}
