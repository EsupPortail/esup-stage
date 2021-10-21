package org.esup_portail.esup_stage.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class EtapeId implements Serializable {

    @Column(name = "codeEtape", nullable = false)
    private String code;

    @Column(nullable = false, length = 50)
    private String codeUniversite;

    @Column(nullable = false, length = 10)
    private String codeVersionEtape;

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

    public String getCodeVersionEtape() {
        return codeVersionEtape;
    }

    public void setCodeVersionEtape(String codeVersionEtape) {
        this.codeVersionEtape = codeVersionEtape;
    }
}
