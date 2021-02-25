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
 * The persistent class for the ModeVersGratification database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ModeVersGratification")
public class ModeVersGratification implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idModeVersGratification")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(name = "libelleModeVersGratification", nullable = false, length = 50)
	private String label;

	@Column(name = "temEnServModeVersGrat", nullable = false, length = 1)
	private String temEnServ;

}