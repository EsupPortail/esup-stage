package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.FAP_QualificationSimplifiee;

@Repository
public interface FAP_QualificationSimplifieeRepository extends JpaRepository<FAP_QualificationSimplifiee, Integer> {
}
