package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiTokenJpaRepository extends JpaRepository<ApiToken, Integer> {

    List<ApiToken> findByActifTrue();

    List<ApiToken> findByNomApplication(String nomApplication);

    List<ApiToken> findByNomApplicationAndActifTrue(String nomApplication);
}
