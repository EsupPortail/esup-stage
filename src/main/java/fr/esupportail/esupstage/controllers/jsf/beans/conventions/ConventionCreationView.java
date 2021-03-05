package fr.esupportail.esupstage.controllers.jsf.beans.conventions;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import fr.esupportail.esupstage.services.beans.EtudiantBean;
import fr.esupportail.esupstage.services.conventions.ConventionService;
import lombok.Getter;

@Named("conventionCreationView")
@ViewScoped
public class ConventionCreationView implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7987062852304528092L;

    private Integer student;

    @Getter private String guidelineTitle = "Indications";
    @Getter private boolean showGuidelineTab = false;
    @Getter private String[] guidelines = new String[]{"guidelines_test"};


    @Getter private String studentTitle = "Étudiant";
    @Getter private boolean showStudentTab = true;

    @Getter private String institutionTitle = "Établissement d'accueil'";
    @Getter private boolean showInstitutionTab = true;

    @Getter private String serviceTitle = "Service d'accueil";
    @Getter private boolean showServiceTab = true;

    @Getter private String tutorTitle = "Tuteur Professionnel";
    @Getter private boolean showTutorTab = true;

    @Getter private String internshipTitle = "Stage";
    @Getter private boolean showInternshipTab = true;

    @Getter private String teacherTitle = "Enseignant Référent";
    @Getter private boolean showTeacherTab = true;

    @Getter private String signerTitle = "Signataire";
    @Getter private boolean showSignerTab = true;

    @Getter private EtudiantBean etudiant;

    @Inject
    private ConventionService service;

    @PostConstruct
    public void init() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            UserDetails detail = (UserDetails) authentication.getPrincipal();
            this.student = service.getStudentByIdentAndCodeUniv(detail.getUsername(), null).getId();
        }else
            this.student = null;
    }

    public void setService(ConventionService service) {
        this.service = service;
    }

    public void nextTab() {
        this.showStudentTab=false;
    }
}
