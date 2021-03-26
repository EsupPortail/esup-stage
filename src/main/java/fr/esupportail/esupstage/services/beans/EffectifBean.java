package fr.esupportail.esupstage.services.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EffectifBean {

	private static final long serialVersionUID = 1L;

	private Integer idEffectif;

	private String libelleEffectif;

	private boolean modifiable;

	private String temEnServEffectif;

	@Override
	public String toString() {
		return this.libelleEffectif;
	}

}
