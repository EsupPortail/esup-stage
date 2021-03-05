package fr.esupportail.esupstage.services.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.inject.Named;

import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.domain.jpa.entities.Etudiant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Named(value = "etudiantBean")
@Getter @Setter
public class EtudiantBean implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 6785241519374702787L;

	private Etudiant student;

	@Setter(value = AccessLevel.PRIVATE) // Make it read-only
    private Integer id;
    private String codeSexe;
    private String codeUniversite;
    private Date dateNais;
    private String identEtudiant;
    private String mail;
    private String mailPro;
    private String nom;
    private String nomMarital;
    private String numEtudiant;
    private String numSS;
    private String prenom;
    private List<Convention> conventions;

	public Etudiant toEtudiant(Etudiant student) {
		student.setId(this.getId());
		student.setCodeSexe(this.getCodeSexe());
		student.setCodeUniversite(this.getCodeUniversite());
		student.setDateNais(this.getDateNais());
		student.setIdentEtudiant(this.getIdentEtudiant());
		student.setMail(this.getMail());
		student.setNom(this.getNom());
		student.setNomMarital(this.getNomMarital());
		student.setNumEtudiant(this.getNumEtudiant());
		student.setNumSS(this.getNumSS());
		student.setPrenom(this.getPrenom());
		student.setConventions(this.getConventions());

		return student;
	}
	public static EtudiantBean fromEtudiant(Etudiant instance) {

		EtudiantBean student = new EtudiantBean();
		student.setId(instance.getId());
		student.setCodeSexe(instance.getCodeSexe());
		student.setCodeUniversite(instance.getCodeUniversite());
		student.setDateNais(instance.getDateNais());
		student.setIdentEtudiant(instance.getIdentEtudiant());
		student.setMail(instance.getMail());
		student.setNom(instance.getNom());
		student.setNomMarital(instance.getNomMarital());
		student.setNumEtudiant(instance.getNumEtudiant());
		student.setNumSS(instance.getNumSS());
		student.setPrenom(instance.getPrenom());
		student.setConventions(instance.getConventions());

		return student;
	}
}
