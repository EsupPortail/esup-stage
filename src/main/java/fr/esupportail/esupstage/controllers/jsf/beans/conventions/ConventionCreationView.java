package fr.esupportail.esupstage.controllers.jsf.beans.conventions;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import fr.esupportail.esupstage.services.conventions.ConventionService;
import lombok.Getter;
import lombok.Setter;

@Named("conventionCreationView")
@ViewScoped
@Getter @Setter
public class ConventionCreationView implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7987062852304528092L;

    private StudentBean student = null;

    private String guidelineTitle = "Indications";
    private boolean showGuidelineTab = false;
    private String[] guidelines = new String[]{"guidelines_test"};


    private String studentTitle = "Étudiant";
    private boolean showStudentTab = true;

    private String institutionTitle = "Établissement d'accueil'";
    private boolean showInstitutionTab = true;

    private String serviceTitle = "Service d'accueil";
    private boolean showServiceTab = true;

    private String tutorTitle = "Tuteur Professionnel";
    private boolean showTutorTab = true;

    private String internshipTitle = "Stage";
    private boolean showInternshipTab = true;

    private String teacherTitle = "Enseignant Référent";
    private boolean showTeacherTab = true;

    private String signerTitle = "Signataire";
    private boolean showSignerTab = true;

    private StudentBean etudiant;

    @Inject
    private ConventionService service;


    public void setService(ConventionService service) {
        this.service = service;
    }

    public void nextTab() {
        if(this.student == null){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                UserDetails detail = (UserDetails) authentication.getPrincipal();
                try{
                this.student = service.getStudentByIdentAndCodeUniv(detail.getUsername(), null);
                }catch(NoSuchElementException e){
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Student can't be found using provided info", e.getLocalizedMessage()));
                }
            }else
                this.student = null;
        }else{

        }
    }
}
