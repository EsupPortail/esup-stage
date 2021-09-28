package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.TypeOffre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeOffreJpaRepository extends JpaRepository<TypeOffre, Integer> {

    TypeOffre findById(int id);
}
