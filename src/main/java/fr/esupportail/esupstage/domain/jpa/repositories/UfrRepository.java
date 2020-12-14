package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Ufr;
import fr.esupportail.esupstage.domain.jpa.entities.UfrPK;

public interface UfrRepository extends JpaRepository<Ufr, UfrPK> {
}
