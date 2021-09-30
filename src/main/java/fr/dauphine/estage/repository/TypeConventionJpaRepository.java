package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.TypeConvention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeConventionJpaRepository extends JpaRepository<TypeConvention, Integer> {

    TypeConvention findById(int id);
}
