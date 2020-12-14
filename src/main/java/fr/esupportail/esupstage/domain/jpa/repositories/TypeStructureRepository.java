package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.TypeStructure;

@Repository
public interface TypeStructureRepository extends JpaRepository<TypeStructure, Integer> {
}
