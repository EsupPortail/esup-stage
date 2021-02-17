package fr.esupportail.esupstage.utils.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fr.esupportail.esupstage.domain.jpa.entities.NbJourHebdo;

@Converter(autoApply = true)
public class NbJourHebdoConverter implements AttributeConverter<NbJourHebdo, String> {

	@Override
	public String convertToDatabaseColumn(final NbJourHebdo attribute) {
		if (null == attribute) {
			return null;
		}
		return attribute.getValue();
	}

	@Override
	public NbJourHebdo convertToEntityAttribute(final String dbData) {
		return NbJourHebdo.findBy(dbData);
	}

}
