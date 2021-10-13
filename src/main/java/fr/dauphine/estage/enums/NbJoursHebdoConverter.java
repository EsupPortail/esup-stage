package fr.dauphine.estage.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class NbJoursHebdoConverter implements AttributeConverter<NbJoursHebdoEnum, String> {
    @Override
    public String convertToDatabaseColumn(NbJoursHebdoEnum attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public NbJoursHebdoEnum convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        return Stream.of(NbJoursHebdoEnum.values())
                .filter(c -> c.getValue().equals(dbData))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
