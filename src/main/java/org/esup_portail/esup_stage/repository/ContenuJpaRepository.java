package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Contenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContenuJpaRepository extends JpaRepository<Contenu, String> {

    @Query("SELECT c FROM Contenu c WHERE c.libelle = TRUE")
    List<Contenu> getLibelle();

    @Query("SELECT c FROM Contenu c WHERE c.code = :code")
    Contenu findByCode(String code);
}
