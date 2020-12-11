package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the pstagedata_vers_mana database table.
 *
 */
@Entity
@Table(name = "pstagedata_vers_mana")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "PstagedataVersMana.findAll", query = "SELECT p FROM PstagedataVersMana p")
public class PstagedataVersMana implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(unique = true, nullable = false)
    private String id;
    @Column(length = 255)
    private String vers;

}