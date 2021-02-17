package fr.esupportail.esupstage.domain.jpa.entities;

import lombok.Getter;

@Getter
public enum NbJourHebdo {

	// @formatter:off
	NB_JOURS_0_5("0.5"), 
	NB_JOURS_1_0("1"), 
	NB_JOURS_1_5("1.5"), 
	NB_JOURS_2_0("2"), 
	NB_JOURS_2_5("2.5"),
	NB_JOURS_3_0("3"), 
	NB_JOURS_3_5("3.5"), 
	NB_JOURS_4_0("4"), 
	NB_JOURS_4_5("4.5"), 
	NB_JOURS_5_0("5"),
	NB_JOURS_5_5("5.5"), 
	NB_JOURS_6_0("6"),
	;
	// @formatter:on

	private final String value;

	private NbJourHebdo(final String value) {
		this.value = value;
	}

	public static NbJourHebdo findBy(String value) {
		NbJourHebdo result = null;
		for (final NbJourHebdo tmp : values()) {
			if (tmp.value.equals(value)) {
				result = tmp;
				break;
			}
		}
		return result;
	}
}
