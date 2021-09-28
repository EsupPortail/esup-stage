package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.TypeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeStructureJpaRepository extends JpaRepository<TypeStructure, Integer> {

    TypeStructure findById(int id);
}
