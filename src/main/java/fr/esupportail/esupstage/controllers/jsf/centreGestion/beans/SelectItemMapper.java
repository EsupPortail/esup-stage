package fr.esupportail.esupstage.controllers.jsf.centreGestion.beans;

import javax.faces.model.SelectItem;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import fr.esupportail.esupstage.services.beans.ConfidentialiteBean;
import fr.esupportail.esupstage.services.beans.NiveauCentreBean;

@Mapper
public interface SelectItemMapper {

	SelectItemMapper INSTANCE = Mappers.getMapper(SelectItemMapper.class);

	// @formatter:off
	@Mappings({
		@Mapping(target = "value", source = "id")
	})
	// @formatter:on
	SelectItem convert(NiveauCentreBean from);

	// @formatter:off
	@Mappings({
		@Mapping(target = "value", source = "code")
	})
	// @formatter:on
	SelectItem convert(ConfidentialiteBean from);

}
