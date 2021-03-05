package fr.esupportail.esupstage.services.conventions;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;

import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.domain.jpa.repositories.ConventionRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.EtudiantRepository;
import fr.esupportail.esupstage.services.beans.EtudiantBean;

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

    public List<Convention> getConventions(int size) {
        return this.conventionRepository.findTopN(size);
    }

	public List<Convention> getClonedProducts(int size) throws CloneNotSupportedException {
		List<Convention> results = new ArrayList<>();
		List<Convention> originals = this.getConventions(size);
		for (Convention original : originals) {
			results.add((Convention) original.clone());
		}
		return results;
	}

	public EtudiantBean getStudentByIdAndCodeUniv(int id, String codeUniv) throws NoSuchElementException {
        return EtudiantBean.fromEtudiant(this.studentRepository.findEtudiantByIdAndCodeUniversite(id, codeUniv).orElseThrow());
	}

	public EtudiantBean getStudentByIdentAndCodeUniv(String username, String codeUniv) throws NoSuchElementException {
        return EtudiantBean.fromEtudiant(this.studentRepository.findEtudiantByIdentEtudiantAndCodeUniversite(username, codeUniv).orElseThrow());
	}
}
