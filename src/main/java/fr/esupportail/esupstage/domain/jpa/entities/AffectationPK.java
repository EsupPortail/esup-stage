package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The primary key class for the Affectation database table.
 *
 */
@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class AffectationPK implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(unique = true, nullable = false, length = 10)
	private String codeAffectation;

	@Column(unique = true, nullable = false, length = 50)
	private String codeUniversite;

}