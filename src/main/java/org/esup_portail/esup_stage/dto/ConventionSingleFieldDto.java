package org.esup_portail.esup_stage.dto;

import javax.validation.constraints.NotNull;

public class ConventionSingleFieldDto {

    @NotNull
    private String field;

    private Object value;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
