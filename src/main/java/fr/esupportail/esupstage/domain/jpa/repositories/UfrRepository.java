package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.Ufr;
import fr.esupportail.esupstage.domain.jpa.entities.UfrPK;

@Repository
public interface UfrRepository extends JpaRepository<Ufr, UfrPK> {
}
