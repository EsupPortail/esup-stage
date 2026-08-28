package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * Services qui n'ont plus aucun contact et qui ne sont référencés ni par une convention ni par
     * un avenant. Utilisé par le nettoyage automatique.
     *
     * Projection (et non entités) pour éviter tout N+1 et ne pas peupler le contexte de persistance :
     * colonnes id, nom, voie, codePostal, commune, raison sociale de la structure, loginCreation,
     * dateCreation.
     */
    @Query("""
            SELECT s.id, s.nom, s.voie, s.codePostal, s.commune, st.raisonSociale, s.loginCreation, s.dateCreation
            FROM Service s
            JOIN s.structure st
            WHERE NOT EXISTS (SELECT 1 FROM Contact c WHERE c.service = s)
              AND NOT EXISTS (SELECT 1 FROM Convention cv WHERE cv.service = s)
              AND NOT EXISTS (SELECT 1 FROM Avenant a WHERE a.service = s)
            """)
    List<Object[]> findInutilisesPourNettoyage();

    @Modifying
    @Query("DELETE FROM Service s WHERE s.id IN :ids")
    int deleteByIdIn(@Param("ids") List<Integer> ids);
}
