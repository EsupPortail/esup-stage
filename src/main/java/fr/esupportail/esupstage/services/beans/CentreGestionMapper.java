package fr.esupportail.esupstage.services.beans;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import fr.esupportail.esupstage.domain.jpa.entities.CentreGestion;

@Mapper
public interface CentreGestionMapper {

	CentreGestionMapper INSTANCE = Mappers.getMapper(CentreGestionMapper.class);

	CentreGestionBean convert(CentreGestion centreGestion);

}
