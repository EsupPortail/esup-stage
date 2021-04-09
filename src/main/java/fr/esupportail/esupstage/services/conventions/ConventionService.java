package fr.esupportail.esupstage.services.conventions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import javassist.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;

import fr.esupportail.esupstage.controllers.jsf.beans.conventions.StudentBean;
import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.domain.jpa.entities.Etudiant;
import fr.esupportail.esupstage.domain.jpa.repositories.ConventionRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.EtudiantRepository;

@Named
@ApplicationScoped
public class ConventionService {

    List<Convention> conventions;

    @Autowired
    private ConventionRepository conventionRepository;
    @Autowired
    private EtudiantRepository studentRepository;

    public List<ConventionBean> getConventions() {
        return ConventionBean.getAll();
    }

    // public List<ConventionBean> getConventions() {
    // return this.conventionRepository.findAll();
    // }

    public Page<ConventionBean> getConventions(Pageable pageable) {
        return this.conventionRepository.findAll(pageable).map(ConventionBean::map);
    }

    public ConventionBean getConvention(Integer integer) {
        return ConventionBean.get(integer);
    }

    public StudentBean getStudentByIdAndCodeUniv(int id, String codeUniv) throws NoSuchElementException {
        return StudentBean.map(this.studentRepository.findEtudiantByIdAndCodeUniversite(id, codeUniv).orElseThrow());
    }

    public StudentBean getStudentByIdentAndCodeUniv(String username, String codeUniv) throws NoSuchElementException {
        return StudentBean.map(
                this.studentRepository.findEtudiantByIdentEtudiantAndCodeUniversite(username, codeUniv).orElseThrow());
    }

    public StudentBean createStudentFromUser(UserDetails detail, String codeUniversite) {
        Etudiant student = new Etudiant();
        student.setIdentEtudiant(detail.getUsername());
        student.setNom(detail.getUsername().substring(1, detail.getUsername().length()));
        student.setPrenom(detail.getUsername().substring(0, 1));
        student.setCodeSexe("M");
        student.setMail(detail.getUsername() + "@etudiant.univ.fr");
        student.setDateNais(LocalDate.of(1978, 03, 28));
        student.setCreatedDate(LocalDateTime.now());
        student.setCreatedBy("root");
        student.setNumEtudiant("65299292");
        student.setNumSS("178033684913953");
        student.setCodeUniversite(codeUniversite);

        return StudentBean.map(studentRepository.saveAndFlush(student));
    }

    public ConventionBean updateConvention(ConventionBean convention) throws NotFoundException {
        // TODO: save ConventionBean into convention
        return convention;
    }

    public StudentBean updateStudent(StudentBean student) throws NotFoundException {
        Etudiant etudiant = studentRepository
                .findEtudiantByIdAndCodeUniversite(student.getId(), student.getCodeUniversite())
                .orElseThrow(() -> new NotFoundException("Student not found"));
        this.studentRepository.save(etudiant);
        return student;
    }
}
