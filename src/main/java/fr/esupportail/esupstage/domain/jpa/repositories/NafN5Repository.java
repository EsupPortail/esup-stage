package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.NafN5;

@Repository
public interface NafN5Repository extends JpaRepository<NafN5, String> {
}
