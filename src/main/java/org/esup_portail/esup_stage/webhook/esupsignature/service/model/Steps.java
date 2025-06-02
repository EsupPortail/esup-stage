package org.esup_portail.esup_stage.webhook.esupsignature.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Steps {
    private int stepNumber;
    private String signType;
    private boolean allSignToComplete;
    private List<RecipientAction> recipientsActions;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    static class RecipientAction {
        private String userEppn;
        private String userEmail;
        private String userName;
        private String userFirstname;
        private int stepNumber;
        private String actionDate;
        private String actionType;
        private String refuseComment;
        private Integer signPageNumber;
        private Integer signPosX;
        private Integer signPosY;
        private String signType;
    }

}

