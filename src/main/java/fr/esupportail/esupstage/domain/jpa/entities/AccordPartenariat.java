package fr.esupportail.esupstage.domain.jpa.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the AccordPartenariat database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "AccordPartenariat")
public class AccordPartenariat extends Auditable<String> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "idAccordPartenariat")
	@GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE")
	private Integer id;

	@Column(nullable = false)
	private boolean comptesSupprimes;

	private LocalDateTime dateSuppressionComptes;

	private LocalDateTime dateValidation;

	@Column(nullable = false)
	private boolean estValide;

	@Column(length = 50)
	private String loginSuppressionComptes;

	@Column(length = 50)
	private String loginValidation;

	@Lob
	private String raisonSuppressionComptes;

	@ManyToOne
	@JoinColumn(name = "idContact", nullable = false)
	private Contact contact;

	@ManyToOne
	@JoinColumn(name = "idStructure", nullable = false)
	private Structure structure;

}