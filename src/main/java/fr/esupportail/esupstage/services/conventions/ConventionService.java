package fr.esupportail.esupstage.services.conventions;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;

import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.domain.jpa.repositories.ConventionRepository;

@Named
@ApplicationScoped
public class ConventionService {

    List<Convention> conventions;

    @Autowired
    private ConventionRepository conventionRepository;


    public List<Convention> getConventions() {
        return this.conventionRepository.findAll();
    }

    public List<Convention> getConventions(int size) {
        return this.conventionRepository.findAll().subList(0, size);
    }

	public List<Convention> getClonedProducts(int size) throws CloneNotSupportedException {
		List<Convention> results = new ArrayList<>();
		List<Convention> originals = this.getConventions(size);
		for (Convention original : originals) {
			results.add((Convention) original.clone());
		}
		return results;
	}
}