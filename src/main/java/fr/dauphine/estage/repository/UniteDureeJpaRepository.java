package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.UniteDuree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniteDureeJpaRepository extends JpaRepository<UniteDuree, Integer> {

    UniteDuree findById(int id);
}
