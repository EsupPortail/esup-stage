package fr.esupportail.esupstage.domain.jpa.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the pstagedata_vers_mana database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "pstagedata_vers_mana")
public class PstagedataVersMana implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(unique = true, nullable = false)
	private Long id;

	@Column(length = 255)
	private String vers;

}