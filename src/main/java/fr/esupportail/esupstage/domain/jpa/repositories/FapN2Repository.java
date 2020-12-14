package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.FapN2;

@Repository
public interface FapN2Repository extends JpaRepository<FapN2, String> {
}
