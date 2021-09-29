package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Devise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviseJpaRepository extends JpaRepository<Devise, Integer> {

    Devise findById(int id);
}
