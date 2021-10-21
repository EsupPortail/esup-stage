package org.esup_portail.esup_stage.exception;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ApplicationClientInterruption {
    public static SimpleDateFormat parseDateToJson = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final int SSTYPE_NON_DEFINI = 0;
    public static final int SSTYPE_DB = 1;
    public static final int SSTYPE_PRECONDITION = 2;
    public static final int SSTYPE_AUTHORISATION = 3;
    public static final int SSTYPE_METIER = 4;
    private Date dateInterruption;
    private int codeHttp;
    private String clientMessage;
    private String internMessage;
    private String dateInterrupt;

    public ApplicationClientInterruption(int codeHttp, String clientMessage, String internMessage) {
        this.dateInterruption = null;
        this.codeHttp = 0;
        this.clientMessage = null;
        this.internMessage = null;
        this.dateInterrupt = null;
        this.setDateInterruption(new Date());
        this.setDateInterrupt(parseDateToJson.format(this.getDateInterruption()));
        this.setCodeHttp(codeHttp);
        this.setInternMessage(internMessage);
        this.setClientMessage(clientMessage);
    }

    public ApplicationClientInterruption(int codeHttp, String clientMessage) {
        this(codeHttp, clientMessage, "N/A");
    }

    public ApplicationClientInterruption(int codeHttp) {
        this(codeHttp, "interruption du service", "erreur inconnue");
    }

    public ApplicationClientInterruption() {
        this.dateInterruption = null;
        this.codeHttp = 0;
        this.clientMessage = null;
        this.internMessage = null;
        this.dateInterrupt = null;
    }

    public int getCodeHttp() {
        return this.codeHttp;
    }

    public void setCodeHttp(int codeHttp) {
        this.codeHttp = codeHttp;
    }

    public String getClientMessage() {
        return this.clientMessage;
    }

    public void setClientMessage(String clientMessage) {
        this.clientMessage = clientMessage;
    }

    public String getInternMessage() {
        return this.internMessage;
    }

    public void setInternMessage(String internMessage) {
        this.internMessage = internMessage;
    }

    public Date getDateInterruption() {
        return this.dateInterruption;
    }

    public void setDateInterruption(Date dateInterruption) {
        this.dateInterruption = dateInterruption;
    }

    public String getDateInterrupt() {
        return this.dateInterrupt;
    }

    public void setDateInterrupt(String dateInterrupt) {
        this.dateInterrupt = dateInterrupt;
    }

    public String toString() {
        return "[http=" + this.getCodeHttp() + "][client-message='" + this.getClientMessage() + "'][intern-message='" + this.getInternMessage() + "']";
    }
}
