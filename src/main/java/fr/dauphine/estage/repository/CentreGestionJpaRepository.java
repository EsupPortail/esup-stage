package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.CentreGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentreGestionJpaRepository extends JpaRepository<CentreGestion, Integer> {

    CentreGestion findById(int id);
}
