package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.CritereGestion;
import fr.esupportail.esupstage.domain.jpa.entities.CritereGestionPK;

public interface CritereGestionRepository extends JpaRepository<CritereGestion, CritereGestionPK> {
}
