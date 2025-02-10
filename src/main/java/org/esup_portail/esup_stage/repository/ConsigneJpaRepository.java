package org.esup_portail.esup_stage.repository;


import org.esup_portail.esup_stage.model.Consigne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ConsigneJpaRepository extends JpaRepository<Consigne, Integer> {

    Consigne findById(int id);

    @Query("SELECT c FROM Consigne c WHERE c.centreGestion.id = :idCentre")
    Consigne findByIdCentreGestion(@Param("idCentre") int idCentre);

}
