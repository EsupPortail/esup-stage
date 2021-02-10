package fr.esupportail.esupstage.controllers.jsf.beans.conventions;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.services.conventions.ConventionService;

@Named("conventionListView")
@ViewScoped
public class ConventionListView implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7987062852304528092L;

    private List<Convention> conventions;

    @Inject
    private ConventionService service;

    @PostConstruct
    public void init() {
        conventions = service.getConventions(10);
    }

    public List<Convention> getConventions() {
        return conventions;
    }

    public void setService(ConventionService service) {
        this.service = service;
    }
}
