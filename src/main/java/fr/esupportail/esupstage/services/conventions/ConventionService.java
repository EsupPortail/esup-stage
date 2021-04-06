package fr.esupportail.esupstage.services.conventions;

import java.util.List;
import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;

import fr.esupportail.esupstage.controllers.jsf.beans.conventions.StudentBean;
import fr.esupportail.esupstage.domain.jpa.entities.Convention;
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

    public List<Convention> getConventions() {
        return this.conventionRepository.findAll();
    }

    public Page<ConventionBean> getConventions(Pageable pageable) {
        return this.conventionRepository.findAll(pageable).map(ConventionBean::map);
    }

	public StudentBean getStudentByIdAndCodeUniv(int id, String codeUniv) throws NoSuchElementException {
        return StudentBean.map(this.studentRepository.findEtudiantByIdAndCodeUniversite(id, codeUniv).orElseThrow());
	}

	public StudentBean getStudentByIdentAndCodeUniv(String username, String codeUniv) throws NoSuchElementException {
        return StudentBean.map(this.studentRepository.findEtudiantByIdentEtudiantAndCodeUniversite(username, codeUniv).orElseThrow());
	}
}
