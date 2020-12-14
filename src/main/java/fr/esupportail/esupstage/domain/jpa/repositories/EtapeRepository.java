package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.Etape;
import fr.esupportail.esupstage.domain.jpa.entities.EtapePK;

@Repository
public interface EtapeRepository extends JpaRepository<Etape, EtapePK> {
}
