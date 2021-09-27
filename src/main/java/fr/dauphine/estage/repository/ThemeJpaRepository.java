package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeJpaRepository extends JpaRepository<Theme, Integer> {

    Theme findById(int id);
}
