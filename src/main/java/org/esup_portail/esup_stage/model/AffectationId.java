package org.esup_portail.esup_stage.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class AffectationId implements Serializable {

    @Column(name = "codeAffectation", nullable = false)
    private String code;

    @Column(nullable = false)
    private String codeUniversite;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeUniversite() {
        return codeUniversite;
    }

    public void setCodeUniversite(String codeUniversite) {
        this.codeUniversite = codeUniversite;
    }
}
