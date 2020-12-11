package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the pstagedata_user database table.
 *
 */
@Entity
@Table(name = "pstagedata_user")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "PstagedataUser.findAll", query = "SELECT p FROM PstagedataUser p")
public class PstagedataUser implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(unique = true, nullable = false, length = 255)
    private String id;
    @Column(nullable = false)
    private boolean admi;
    @Column(name = "disp_name", length = 255)
    private String dispName;
    @Column(length = 255)
    private String lang;
}