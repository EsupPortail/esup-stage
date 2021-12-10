package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import javax.persistence.*;

@Entity
@Table(name = "Pays")
public class Pays implements Exportable {

    @JsonView(Views.List.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPays", nullable = false)
    private int id;

    @Column(name = "COG", nullable = false)
    private int cog;

    /**
     * Code actualité du pays ou du territoire.
     1	actuel
     2	périmé (dont le code a disparu du C.O.G.)
     3	territoire n'ayant pas son propre code officiel géographique
     4	territoire ayant son propre code officiel géographique
     */
    @Column(nullable = false)
    private Integer actual;

    @Column(name = "CRPAY")
    private Integer crpay;

    @JsonView(Views.List.class)
    @Column(nullable = false, length = 75)
    private String lib;

    @JsonView(Views.List.class)
    @Column(name = "ISO2", length = 2)
    private String iso2;

    @Column(nullable = false)
    private boolean siretObligatoire;

    @Column(nullable = false, length = 1)
    private String temEnServPays;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCog() {
        return cog;
    }

    public void setCog(int cog) {
        this.cog = cog;
    }

    public Integer getActual() {
        return actual;
    }

    public void setActual(Integer actual) {
        this.actual = actual;
    }

    public Integer getCrpay() {
        return crpay;
    }

    public void setCrpay(Integer crpay) {
        this.crpay = crpay;
    }

    public String getLib() {
        return lib;
    }

    public void setLib(String lib) {
        this.lib = lib;
    }

    public String getIso2() {
        return iso2;
    }

    public void setIso2(String iso2) {
        this.iso2 = iso2;
    }

    public boolean isSiretObligatoire() {
        return siretObligatoire;
    }

    public void setSiretObligatoire(boolean siretObligatoire) {
        this.siretObligatoire = siretObligatoire;
    }

    public String getTemEnServPays() {
        return temEnServPays;
    }

    public void setTemEnServPays(String temEnServPays) {
        this.temEnServPays = temEnServPays;
    }

    @Override
    public String getExportValue(String key) {
        return null;
    }
}
