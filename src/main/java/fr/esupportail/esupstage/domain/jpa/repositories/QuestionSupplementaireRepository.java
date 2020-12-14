package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.QuestionSupplementaire;

@Repository
public interface QuestionSupplementaireRepository extends JpaRepository<QuestionSupplementaire, Integer> {
}
