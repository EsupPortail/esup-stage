package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Pays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaysJpaRepository extends JpaRepository<Pays, Integer> {

    Pays findById(int id);
}
