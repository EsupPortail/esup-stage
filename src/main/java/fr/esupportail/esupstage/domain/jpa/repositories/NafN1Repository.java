package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.NafN1;

@Repository
public interface NafN1Repository extends JpaRepository<NafN1, String> {
}
