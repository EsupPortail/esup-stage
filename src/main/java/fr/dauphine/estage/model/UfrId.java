package fr.dauphine.estage.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class UfrId implements Serializable {

    @Column(name = "codeUFR", nullable = false)
    private String code;

    @Column(nullable = false, length = 50)
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
