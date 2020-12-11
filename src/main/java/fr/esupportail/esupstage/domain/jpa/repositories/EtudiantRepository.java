package fr.esupportail.esupstage.domain.jpa.repositories;

import java.util.List;

import fr.esupportail.esupstage.domain.jpa.entities.Etudiant;
import org.springframework.data.repository.CrudRepository;

public interface EtudiantRepository extends CrudRepository<Etudiant, Integer> {

	public List<Etudiant> findEtudiantsByNomContainsOrPrenomContains(String nom, String prenom);
	
	public Etudiant findEtudiantByIdEtudiantAndCodeUniversite(Integer idEtudiant,String codeUniveriste);

	public Etudiant findEtudiantByIdentEtudiantAndCodeUniversite(String identEtudiant, String codeUniveriste);
}
