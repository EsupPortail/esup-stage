package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.FapN3;

@Repository
public interface FapN3Repository extends JpaRepository<FapN3, String> {
}
