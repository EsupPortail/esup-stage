package org.esup_portail.esup_stage.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;

@Entity
@Table(name = "NAF_N5")
@Data
public class NafN5 {

    @JsonView(Views.List.class)
    @Id
    @Column(name = "codeNAF_N5", nullable = false)
    private String code;

    @JsonView(Views.List.class)
    @Column(name = "libelleNAF_N5")
    private String libelle;

    @Column(name = "temEnServNAF_N5", length = 1)
    private Boolean temEnServ;

    @JsonView(Views.List.class)
    @ManyToOne
    @JoinColumn(name = "codeNAF_N1", nullable = false)
    private NafN1 nafN1;

}
