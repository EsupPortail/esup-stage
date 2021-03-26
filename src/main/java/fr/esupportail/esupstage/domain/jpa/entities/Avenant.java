package fr.esupportail.esupstage.domain.jpa.entities;

import java.time.LocalDate;

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
 * The persistent class for the Avenant database table.
 */
@Entity @Getter @Setter @NoArgsConstructor @Table(name = "Avenant") public class Avenant extends Auditable<String> {

		private static final long serialVersionUID = 1L;

		@Id @Column(name = "idAvenant") @GenericGenerator(name = "HIBERNATE_SEQUENCE", strategy = "native") @GeneratedValue(strategy = GenerationType.AUTO, generator = "HIBERNATE_SEQUENCE") private Integer id;

		@Lob private String commentaireRupture;

		private LocalDate dateDebutInterruption;

		private LocalDate dateDebutStage;

		private LocalDate dateFinInterruption;

		private LocalDate dateFinStage;

		private LocalDate dateRupture;

		private Integer idEnseignant;

		private Integer idUniteDureeGratification;

		private Integer idUniteGratification;

		@Column(nullable = false) private boolean interruptionStage;

		@Column(nullable = false) private boolean modificationEnseignant;

		private boolean modificationLieu;

		@Column(nullable = false) private boolean modificationMontantGratification;

		@Column(nullable = false) private boolean modificationPeriode;

		@Column(nullable = false) private boolean modificationSalarie;

		@Column(nullable = false) private boolean modificationSujet;

		@Column(length = 50) private String monnaieGratification;

		@Column(length = 7) private String montantGratification;

		@Lob private String motifAvenant;

		@Column(nullable = false) private boolean rupture;

		@Lob private String sujetStage;

		@Lob private String titreAvenant;

		@Column(nullable = false) private boolean validationAvenant;

		@ManyToOne @JoinColumn(name = "idContact") private Contact contact;

		@ManyToOne @JoinColumn(name = "idConvention", nullable = false) private Convention convention;

		@ManyToOne @JoinColumn(name = "idService") private Service service;

}