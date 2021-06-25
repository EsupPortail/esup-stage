package fr.esupportail.esupstage.services.beans;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import fr.esupportail.esupstage.domain.jpa.entities.CentreGestion;

@Mapper(uses = { NiveauCentreMapper.class, ConfidentialiteMapper.class })
public interface CentreGestionMapper {

	CentreGestionMapper INSTANCE = Mappers.getMapper(CentreGestionMapper.class);

	CentreGestionBean convert(CentreGestion centreGestion);

	CentreGestion convert(CentreGestionBean centreGestion);

}
