package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceJpaRepository extends JpaRepository<Service, Integer> {

    @Query("SELECT s FROM Service s WHERE s.id = :id")
    Service findById(@Param("id") int id);

    @Query("SELECT s FROM Service s WHERE s.structure.id = :idStructure")
    List<Service> findByStructure(@Param("idStructure") int idStructure);

    @Query("""
    SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
    FROM Service s, Utilisateur u
    WHERE s.id = :id
      AND u.id = :userId
      AND s.loginCreation = u.login
      AND (s.loginModif IS NULL OR s.loginModif = s.loginCreation)
""")
    boolean isOwner(@Param("id") Integer id, @Param("userId") int userId);
}
