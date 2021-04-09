package fr.esupportail.esupstage.controllers.jsf.beans.conventions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import fr.esupportail.esupstage.services.conventions.ConventionBean;
import fr.esupportail.esupstage.services.conventions.ConventionService;
import lombok.Getter;

@Named("conventionListView")
@ViewScoped
public class ConventionListView implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private List<ConventionBean> conventions;

    @Inject
    private ConventionService service;

    @PostConstruct
    private void init() {
        this.conventions = this.service.getConventions();
    }

    public List<ConventionBean> getConventions() {
        return this.conventions;
    }

    public void setService(ConventionService service) {
        this.service = service;
    }
}
