package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.LangueConvention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LangueConventionJpaRepository extends JpaRepository<LangueConvention, Integer> {

    LangueConvention findByCode(String code);
}
