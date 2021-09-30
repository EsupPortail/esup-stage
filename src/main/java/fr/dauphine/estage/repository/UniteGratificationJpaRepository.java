package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.UniteGratification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniteGratificationJpaRepository extends JpaRepository<UniteGratification, Integer> {

    UniteGratification findById(int id);
}
