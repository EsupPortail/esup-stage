package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.ApiToken;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class ApiTokenRepository extends PaginationRepository<ApiToken> {

    public ApiTokenRepository(EntityManager em) {
        super(em, ApiToken.class, "at");
        // tokenEncrypted volontairement absent : ni triable, ni filtrable
        this.predicateWhitelist = Arrays.asList("id", "nom", "nomApplication", "actif", "dateCreation", "loginCreation", "dateModification", "loginModification");
    }
}
