package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.esup_portail.esup_stage.enums.TypeSignatureEnum;

import javax.persistence.*;

@Entity
@Table(name = "CentreGestionSignataire")
public class CentreGestionSignataire {

    @EmbeddedId
    private CentreGestionSignataireId id;

    @Column(nullable = false)
    private int ordre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeSignatureEnum type;

    @ManyToOne
    @MapsId("idCentreGestion")
    @JoinColumn(name = "idCentreGestion", nullable = false)
    @JsonBackReference
    private CentreGestion centreGestion;

    public CentreGestionSignataireId getId() {
        return id;
    }

    public void setId(CentreGestionSignataireId id) {
        this.id = id;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public TypeSignatureEnum getType() {
        return type;
    }

    public void setType(TypeSignatureEnum type) {
        this.type = type;
    }

    public CentreGestion getCentreGestion() {
        return centreGestion;
    }

    public void setCentreGestion(CentreGestion centreGestion) {
        this.centreGestion = centreGestion;
    }
}
