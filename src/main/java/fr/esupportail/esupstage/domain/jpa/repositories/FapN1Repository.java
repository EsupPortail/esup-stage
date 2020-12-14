package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.FapN1;

@Repository
public interface FapN1Repository extends JpaRepository<FapN1, String> {
}
