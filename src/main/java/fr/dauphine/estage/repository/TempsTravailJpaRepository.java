package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.TempsTravail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempsTravailJpaRepository extends JpaRepository<TempsTravail, Integer> {

    TempsTravail findById(int id);
}
