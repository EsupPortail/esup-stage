package org.esup_portail.esup_stage.webhook.esupsignature.service.model;

public class Recipient {
    private Integer step = 1;
    private String email;
    private String phone;
    private String name;
    private String firstName;
    private Boolean forceSms = false;

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Boolean getForceSms() {
        return forceSms;
    }

    public void setForceSms(Boolean forceSms) {
        this.forceSms = forceSms;
    }
}
