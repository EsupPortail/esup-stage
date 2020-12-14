package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.ReponseSupplementaire;
import fr.esupportail.esupstage.domain.jpa.entities.ReponseSupplementairePK;

@Repository
public interface ReponseSupplementaireRepository extends JpaRepository<ReponseSupplementaire, ReponseSupplementairePK> {
}
