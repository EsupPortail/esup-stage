package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.ConventionDocumentEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConventionDocumentEtudiantJpaRepository extends JpaRepository<ConventionDocumentEtudiant, Integer> {
    ConventionDocumentEtudiant findById(int id);
    List<ConventionDocumentEtudiant> findByConventionIdOrderByDateCreationDesc(int idConvention);
    List<ConventionDocumentEtudiant> findByConventionIdAndNomReelOrderByDateCreationDesc(int idConvention, String nomReel);
}