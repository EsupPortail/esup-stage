package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.ContratOffre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContratOffreJpaRepository extends JpaRepository<ContratOffre, Integer> {
    ContratOffre findById(int id);
}
