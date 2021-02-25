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
 * The persistent class for the pstagedata_user database table.
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "pstagedata_user")
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