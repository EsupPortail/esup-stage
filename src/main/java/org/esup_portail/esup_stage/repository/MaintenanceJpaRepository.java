package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceJpaRepository extends JpaRepository<Maintenance, Long> {

    @Query("""
            select m
            from Maintenance m
            where m.datDebMaint <= :now
              and (m.datFinMaint is null or m.datFinMaint > :now)
            order by m.datDebMaint desc
            """)
    List<Maintenance> findActiveAt(@Param("now") LocalDateTime now);

    @Query("""
            select m
            from Maintenance m
            where m.datDebMaint > :now
            order by m.datDebMaint asc
            """)
    List<Maintenance> findUpcomingAt(@Param("now") LocalDateTime now);

    @Query("""
            select m
            from Maintenance m
            where m.datDebMaint > :now
              and m.datAlertMaint is not null
              and m.datAlertMaint <= :now
            order by m.datDebMaint asc
            """)
    List<Maintenance> findAlertActiveUpcomingAt(@Param("now") LocalDateTime now);
}
