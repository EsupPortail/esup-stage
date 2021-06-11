package fr.esupportail.esupstage.services.beans;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import fr.esupportail.esupstage.domain.jpa.entities.Confidentialite;

@Mapper
public interface ConfidentialiteMapper {

	ConfidentialiteMapper INSTANCE = Mappers.getMapper(ConfidentialiteMapper.class);

	ConfidentialiteBean convert(Confidentialite confidentialite);

	Confidentialite convert(ConfidentialiteBean confidentialite);

}
