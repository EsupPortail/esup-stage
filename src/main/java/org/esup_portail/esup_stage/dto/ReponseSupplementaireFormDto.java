package org.esup_portail.esup_stage.dto;

public class ReponseSupplementaireFormDto {

    private String reponseTxt;
    private Integer reponseInt;
    private Boolean reponseBool;

    public String getReponseTxt() {
        return reponseTxt;
    }

    public void setReponseTxt(String reponseTxt) {
        this.reponseTxt = reponseTxt;
    }

    public Integer getReponseInt() {
        return reponseInt;
    }

    public void setReponseInt(Integer reponseInt) {
        this.reponseInt = reponseInt;
    }

    public Boolean getReponseBool() {
        return reponseBool;
    }

    public void setReponseBool(Boolean reponseBool) {
        this.reponseBool = reponseBool;
    }
}