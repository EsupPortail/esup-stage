package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.ReponseSupplementaire;
import fr.esupportail.esupstage.domain.jpa.entities.ReponseSupplementairePK;

public interface ReponseSupplementaireRepository extends JpaRepository<ReponseSupplementaire, ReponseSupplementairePK> {
}
