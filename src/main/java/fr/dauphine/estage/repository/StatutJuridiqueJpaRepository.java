package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.StatutJuridique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatutJuridiqueJpaRepository extends JpaRepository<StatutJuridique, Integer> {

    StatutJuridique findById(int id);
}
