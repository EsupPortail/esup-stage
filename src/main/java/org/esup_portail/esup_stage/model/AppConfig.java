package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.enums.AppConfigCodeEnum;

@Entity
@Data
@Table(name = "AppConfig")
public class AppConfig {

    @Enumerated(EnumType.STRING)
    @Id
    @Column(name = "codeAppConfig", nullable = false)
    private AppConfigCodeEnum code;

    @Lob
    @Column(nullable = false)
    private String parametres;

}
