package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Etape;
import fr.esupportail.esupstage.domain.jpa.entities.EtapePK;

public interface EtapeRepository extends JpaRepository<Etape, EtapePK> {
}
