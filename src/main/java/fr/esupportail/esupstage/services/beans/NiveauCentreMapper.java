package fr.esupportail.esupstage.services.beans;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import fr.esupportail.esupstage.domain.jpa.entities.NiveauCentre;

@Mapper
public interface NiveauCentreMapper {

	NiveauCentreMapper INSTANCE = Mappers.getMapper(NiveauCentreMapper.class);

	NiveauCentreBean convert(NiveauCentre niveauCentre);

}
