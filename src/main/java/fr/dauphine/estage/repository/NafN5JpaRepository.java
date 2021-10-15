package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.NafN5;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NafN5JpaRepository extends JpaRepository<NafN5, String> {

    @Query("SELECT n FROM NafN5 n WHERE n.code = :code")
    NafN5 findByCode(String code);
}
