package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Ufr;
import org.esup_portail.esup_stage.model.UfrId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UfrJpaRepository extends JpaRepository<Ufr, UfrId> {

    @Query("SELECT u FROM Ufr u WHERE u.id.code = :code AND u.id.codeUniversite = :codeUniv")
    Ufr findById(String code, String codeUniv);
}
