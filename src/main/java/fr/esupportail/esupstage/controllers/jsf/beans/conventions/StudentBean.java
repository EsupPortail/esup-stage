package fr.esupportail.esupstage.controllers.jsf.beans.conventions;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.inject.Named;

import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.domain.jpa.entities.Etudiant;
import lombok.Getter;
import lombok.Setter;

@Named(value = "etudiantBean")
@Getter @Setter
public class StudentBean implements Serializable{

	private static final long serialVersionUID = 6785241519374702787L;

	private Etudiant student;

	// This is automaticaly got by the logged user
	// (Probably set a id to the form and put the generated id in an hidden field of the form)
    private Integer id;
    private String codeUniversite;

	// This is the form fields for Student

    private String lastname;
    private String firstname;
    private String nomMarital;
    private String address;
    private String postcode;
    private String city;
    private String mail;
    private String mailPro;

    private String numEtudiant;
    private String numSS;
    private List<Convention> conventions;

	public static StudentBean fromEtudiant(Etudiant instance) {

		StudentBean student = new StudentBean();
		student.setId(instance.getId());
		student.setMail(instance.getMail());
		student.setLastname(instance.getNom());
		student.setNomMarital(instance.getNomMarital());
		student.setNumEtudiant(instance.getNumEtudiant());
		student.setNumSS(instance.getNumSS());
		student.setFirstname(instance.getPrenom());
		student.setConventions(instance.getConventions());

		return student;
	}
}
