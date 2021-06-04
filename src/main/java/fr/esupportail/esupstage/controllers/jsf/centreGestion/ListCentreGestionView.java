package fr.esupportail.esupstage.controllers.jsf.centreGestion;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fr.esupportail.esupstage.services.CentreGestionService;
import fr.esupportail.esupstage.services.beans.CentreGestionBean;
import lombok.Getter;

@Getter
@ViewScoped
@Controller
@Named("listCentreGestionView")
public class ListCentreGestionView {

	private final CentreGestionService centreGestionService;

	private List<CentreGestionBean> centreGestions;

	@Autowired
	public ListCentreGestionView(final CentreGestionService centreGestionService) {
		super();
		this.centreGestionService = centreGestionService;
	}

	@PostConstruct
	public void init() {
		centreGestions = centreGestionService.findAll();
	}

}
