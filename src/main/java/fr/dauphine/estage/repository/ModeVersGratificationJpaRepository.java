package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.ModeVersGratification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModeVersGratificationJpaRepository extends JpaRepository<ModeVersGratification, Integer> {

    ModeVersGratification findById(int id);
}
