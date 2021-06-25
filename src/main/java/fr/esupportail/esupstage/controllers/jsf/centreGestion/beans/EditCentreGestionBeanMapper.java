package fr.esupportail.esupstage.controllers.jsf.centreGestion.beans;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import fr.esupportail.esupstage.services.beans.CentreGestionBean;

@Mapper
public interface EditCentreGestionBeanMapper {

	EditCentreGestionBeanMapper INSTANCE = Mappers.getMapper(EditCentreGestionBeanMapper.class);

	// @formatter:off
	@Mappings({
		@Mapping(target = "niveauCentre", source = "niveauCentre.id"),
		@Mapping(target = "confidentialite", source = "confidentialite.code")
	})
	// @formatter:on
	EditCentreGestionBean convert(CentreGestionBean from);

	// @formatter:off
	@Mappings({
		@Mapping(target = "niveauCentre.id", source = "niveauCentre"),
		@Mapping(target = "confidentialite.code", source = "confidentialite")
	})
	// @formatter:on
	CentreGestionBean convert(EditCentreGestionBean from);

}
