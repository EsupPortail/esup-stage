package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the TicketStructure database table.
 *
 */
@Entity
@Table(name = "TicketStructure")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "TicketStructure.findAll", query = "SELECT t FROM TicketStructure t")
public class TicketStructure implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(unique = true, nullable = false, length = 50)
    private String ticket;
    @Column(nullable = false)
    private Integer idStructure;

}