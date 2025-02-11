package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.enums.AppConfigCodeEnum;
import org.esup_portail.esup_stage.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppConfigJpaRepository extends JpaRepository<AppConfig, String> {

    @Query("SELECT ac FROM AppConfig ac WHERE ac.code = :code")
    AppConfig findByCode(@Param("code") AppConfigCodeEnum code);
}
