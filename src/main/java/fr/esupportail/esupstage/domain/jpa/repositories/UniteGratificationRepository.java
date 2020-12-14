package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.UniteGratification;

@Repository
public interface UniteGratificationRepository extends JpaRepository<UniteGratification, Integer> {
}
