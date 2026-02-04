package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactJpaRepository extends JpaRepository<Contact, Integer> {

    @Query("SELECT c FROM Contact c WHERE c.id = :id")
    Contact findById(@Param("id") int id);

    @Query("SELECT c FROM Contact c WHERE c.service.id = :idService")
    List<Contact> findByService(@Param("idService") int idService);

    @Query("SELECT COUNT(c.id) FROM Contact c WHERE c.service.id = :idService")
    Long countContactWithService(@Param("idService") int idService);

    @Query("SELECT COUNT(c.id) FROM Contact c WHERE c.centreGestion.id = :idCentreGestion")
    Long countContactWithCentreGestion(@Param("idCentreGestion") int idCentreGestion);

    @Query("""
    SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
    FROM Contact c, Utilisateur u
    WHERE c.id = :id
      AND u.id = :userId
      AND c.loginCreation = u.login
      AND (c.loginModif IS NULL OR c.loginModif = c.loginCreation)
""")
    boolean isOwner(@Param("id") Integer id, @Param("userId") int userId);
}
