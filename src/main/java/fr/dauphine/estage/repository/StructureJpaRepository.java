package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StructureJpaRepository extends JpaRepository<Structure, Integer> {

    @Query("SELECT s FROM Structure s WHERE s.id = :id")
    Structure findById(int id);
}
