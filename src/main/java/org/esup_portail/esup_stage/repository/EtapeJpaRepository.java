package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Etape;
import org.esup_portail.esup_stage.model.EtapeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EtapeJpaRepository extends JpaRepository<Etape, EtapeId> {

    @Query("SELECT e FROM Etape e WHERE e.id.code = :code AND e.id.codeUniversite = :codeUniv AND e.id.codeVersionEtape = :version")
    Etape findById(@Param("code") String code, @Param("version") String version, @Param("codeUniv") String codeUniv);
}
