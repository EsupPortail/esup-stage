package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;

@Data
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

    public String getPhone() {
        return Strings.isEmpty(phone) ? null : phone;
    }
}