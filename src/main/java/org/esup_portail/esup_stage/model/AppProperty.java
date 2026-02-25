package org.esup_portail.esup_stage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "AppProperty")
@Entity
public class AppProperty {

    @Column(name = "`key`")
    @Id
    private String key;

    @Column
    private String value;

    @Column
    private String updatedAt;

    @Column
    private String createdAt;

}
