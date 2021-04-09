package fr.esupportail.esupstage.controllers.jsf.beans.conventions;

import java.io.Serializable;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import fr.esupportail.esupstage.domain.jpa.entities.Structure;
import fr.esupportail.esupstage.services.conventions.ConventionBean;
import fr.esupportail.esupstage.services.conventions.ConventionService;
import javassist.NotFoundException;
import lombok.Getter;
import lombok.Setter;

@Named("conventionCreationView")
@ViewScoped
@Getter
@Setter
public class ConventionCreationView implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7987062852304528092L;

	@Value("#{request.getParameter('id')}")
	private Integer id;

	private ConventionBean convention = null;

	private StudentBean student = null;

	// TODO: Transform it to StructureBean
	private Structure structure = null;

	private String guidelineTitle = "Indications";
	private boolean showGuidelineTab = false;
	private String[] guidelines = new String[] { "guidelines_test" };

	private String studentTitle = "Étudiant";

	private String structureTitle = "Établissement d'accueil'";

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

	@PostConstruct
	private void initView() {
		System.out.println("Init");

		if (this.id != null) {
			this.convention = this.service.getConvention(this.id);
			if (this.convention != null)
				this.student = this.convention.getStudent();
			else
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Student can't be found using provided info.", this.id.toString()));
		} else
			this.convention = new ConventionBean();
	}

	public void setService(ConventionService service) {
		this.service = service;
	}

	public void acceptGuidelines() throws NotFoundException {
		this.convention.acceptGuidlines();
		if (this.student == null) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (!(authentication instanceof AnonymousAuthenticationToken)) {
				UserDetails detail = (UserDetails) authentication.getPrincipal();
				try {
					this.student = service.getStudentByIdentAndCodeUniv(detail.getUsername(), "univ");
				} catch (NoSuchElementException e) {
					this.student = service.createStudentFromUser(detail, "univ");
					// FacesContext.getCurrentInstance().addMessage(null, new
					// FacesMessage(FacesMessage.SEVERITY_ERROR,
					// "Student can't be found using provided info.", e.getLocalizedMessage()));
				}
			} else
				this.student = null;
		}
		if (this.student != null) {
			this.convention.setStudent(this.student);
			this.convention = this.service.updateConvention(this.convention);
		}
	}

	public void saveStudent() throws NotFoundException {
		this.student = this.service.updateStudent(this.student);
		this.convention.setStudent(this.student);
		this.convention = this.service.updateConvention(this.convention);
		this.structure = new Structure();
	}

	public void saveStructure() throws NotFoundException {

	}
}
