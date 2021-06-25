package fr.esupportail.esupstage.controllers.jsf.centreGestion;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fr.esupportail.esupstage.controllers.jsf.centreGestion.beans.EditCentreGestionBean;
import fr.esupportail.esupstage.controllers.jsf.centreGestion.beans.EditCentreGestionBeanMapper;
import fr.esupportail.esupstage.controllers.jsf.centreGestion.beans.SelectItemMapper;
import fr.esupportail.esupstage.services.CentreGestionService;
import fr.esupportail.esupstage.services.ConfidentialiteService;
import fr.esupportail.esupstage.services.NiveauCentreService;
import lombok.Getter;

@Getter
@ViewScoped
@Controller
@Named("editCentreGestionView")
public class EditCentreGestionView {

	private final CentreGestionService centreGestionService;

	private final NiveauCentreService niveauCentreService;

	private final ConfidentialiteService confidentialiteService;

	private List<SelectItem> niveauCentreList;

	private List<SelectItem> confidentialiteList;

	private EditCentreGestionBean centreGestion;

	@Autowired
	public EditCentreGestionView(final CentreGestionService centreGestionService, final NiveauCentreService niveauCentreService,
			final ConfidentialiteService confidentialiteService) {
		this.centreGestionService = centreGestionService;
		this.niveauCentreService = niveauCentreService;
		this.confidentialiteService = confidentialiteService;
	}

	@PostConstruct
	public void init() {
		final SelectItemMapper selectItemMapper = SelectItemMapper.INSTANCE;
		this.niveauCentreList = this.niveauCentreService.findAll().stream().map(selectItemMapper::convert).collect(Collectors.toList());
		this.confidentialiteList = this.confidentialiteService.findAll().stream().map(selectItemMapper::convert).collect(Collectors.toList());
		this.centreGestion = new EditCentreGestionBean();
	}

	public void save() {
		final EditCentreGestionBeanMapper mapper = EditCentreGestionBeanMapper.INSTANCE;
		this.centreGestion = mapper.convert(this.centreGestionService.save(mapper.convert(centreGestion)));
	}

}
