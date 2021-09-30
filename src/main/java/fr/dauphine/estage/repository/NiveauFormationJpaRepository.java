package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.NiveauFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NiveauFormationJpaRepository extends JpaRepository<NiveauFormation, Integer> {

    NiveauFormation findById(int id);
}
