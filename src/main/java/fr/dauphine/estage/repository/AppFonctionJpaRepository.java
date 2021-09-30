package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.AppFonction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppFonctionJpaRepository extends JpaRepository<AppFonction, Integer> {
}
