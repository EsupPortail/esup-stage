package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the DureeDiffusion database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "DureeDiffusion")
public class DureeDiffusion implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idDureeDiffusion")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(nullable = false)
	private boolean adminSeulement;

	@Column(name = "libelleDureeDiffusion", nullable = false, length = 20)
	private String label;

}