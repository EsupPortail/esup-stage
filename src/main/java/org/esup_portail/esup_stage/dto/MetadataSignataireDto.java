package org.esup_portail.esup_stage.dto;

import org.apache.logging.log4j.util.Strings;

import javax.validation.constraints.NotNull;

public class MetadataSignataireDto {
    @NotNull
    private String name;

    @NotNull
    private String givenname;

    @NotNull
    private String mail;

    private String phone;

    @NotNull
    private int order;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGivenname() {
        return givenname;
    }

    public void setGivenname(String givenname) {
        this.givenname = givenname;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return Strings.isEmpty(phone) ? null : phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
