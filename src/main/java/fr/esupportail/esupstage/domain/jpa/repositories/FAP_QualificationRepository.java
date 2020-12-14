package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.FAP_Qualification;

@Repository
public interface FAP_QualificationRepository extends JpaRepository<FAP_Qualification, Integer> {
}
