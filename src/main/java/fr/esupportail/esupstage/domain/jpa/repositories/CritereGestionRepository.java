package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.CritereGestion;
import fr.esupportail.esupstage.domain.jpa.entities.CritereGestionPK;

@Repository
public interface CritereGestionRepository extends JpaRepository<CritereGestion, CritereGestionPK> {
}
