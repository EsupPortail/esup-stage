package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.RegimeInscriptionApogee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegimeInscriptionApogeeJpaRepository extends JpaRepository<RegimeInscriptionApogee, String> {

    List<RegimeInscriptionApogee> findByTemEnServOrderByLibelle(String temEnServ);
}
