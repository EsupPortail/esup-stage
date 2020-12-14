package fr.esupportail.esupstage.domain.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.esupportail.esupstage.domain.jpa.entities.Convention;
import fr.esupportail.esupstage.domain.jpa.entities.Enseignant;

public interface ConventionRepository extends JpaRepository<Convention, Integer> {

	@Query("FROM Convention c LEFT JOIN FETCH c.centreGestion cg WHERE cg.codeUniversite=?1")
	public List<Convention> findConventionBycodeUniversite(String codeUniversite);

	@Query("SELECT DISTINCT c.annee FROM Convention c LEFT JOIN  c.centreGestion cg WHERE cg.codeUniversite=?1 ORDER BY c.annee DESC")
	public List<String> getAnneesConvention(String codeUniversite);

	@Query("SELECT c FROM Convention c INNER JOIN FETCH c.etudiant etu "
			+ "WHERE etu.idEtudiant=?1 and (?2 is null or etu.codeUniversite=?2) ORDER BY c.dateCreation DESC")
	public List<Convention> findConventionsByIdEtudiantAndCodeUniversite(Integer identEtudiant, String codeUniversite);

	public List<Convention> findConventionsByEnseignant(Enseignant enseignant);

	public List<Convention> findConventionsByEnseignantAndAnnee(Enseignant enseignant, String annee);

	@Query("SELECT COUNT(c) FROM Convention c INNER JOIN c.etape et INNER JOIN c.centreGestion cg WHERE"
			+ " cg.idCentreGestion=?1 and et.id.codeUniversite=?2 ")
	public int getNombreConventionByCentreGestion(int idCentreGestion, String codeUniversite);

	@Query("SELECT c FROM Convention c WHERE c.etape.id.codeEtape=?1 and c.etape.id.codeUniversite =?2 ")
	public List<Convention> getCodeUFRFromCodeEtape(String codeEtape, String codeUniversite);

	
	//public List<Convention> findConventionsByEtapeAndCodeUniversite(Etape etape, String codeUniversite);
	//public List<Convention> findConventionsByCodeEtapeAndCodeVersionEtape(String codeEtape,String codeVersionEtape);
	/**
	 * @param codeEtape
	 * @param codeVersionEtape
	 */
	//@Query("UPDATE" )
	//public void updateConventionSetCodeVersionEtape(String codeEtape, String codeVersionEtape);

	//
	// @Query("SELECT c FROM Convention c INNER JOIN FETCH c.enseignant ens
	// INNER JOIN FETCH c.avenat av "
	// + "WHERE (ens=?1 OR av.enseignant=?1) AND c.annee LIKE CONCAT('%',?2,'%')
	// ")
	// public List<Convention> getConventionsByEnseignantCustom(Enseignant
	// enseignant, String annee);

	// @Query("SELECT c FROM Convention c INNER JOIN FETCH c.enseignant ens
	// INNER JOIN FETCH c.avenat av "
	// + "WHERE (ens=?1 OR av.enseignant=?1) AND c.annee LIKE CONCAT('%',?2,'%')
	// ")
	// public List<Convention> getConventionsFromCriteres(List<Integer>
	// idsCentreGestion, String idConvention,
	// String numeroEtudiant, String nomEtudiant, String prenomEtudiant, String
	// sujetStage,
	// TypeConvention typeConvention, Theme theme, String nbJoursHebdo,
	// Indemnisation indemnisation,
	// TempsTravail tempsTravail, LangueConvention langueConvention, Boolean
	// estVerifiee, Boolean estValidee,
	// String dateStage, String anneeUniversitaire, String idOffre, String
	// nomEnseignant, String prenomEnseignant,
	// java.util.List<String> idsUfrs, List<String> idsEtapes, String
	// raisonSociale, String numeroSiret,
	// String commune, String codePostal, Pays pays, TypeStructure
	// typeStructure, StatutJuridique statutJuridique,
	// Effectif effectif, NafN1 nafN1, boolean isLimit, String nbExportMaxi,
	// String nbRechercheMaxi,
	// boolean estEtrangere);
	//

	// public List<Convention>
	// findConventionByIdConventionNotNullOrByEtudiantNotNull(List<Integer>
	// idConventions,List<Etudiant> etudiants);
}
