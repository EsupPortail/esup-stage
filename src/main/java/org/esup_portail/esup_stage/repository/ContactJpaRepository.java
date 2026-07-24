package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ContactJpaRepository extends JpaRepository<Contact, Integer> {

    @Query("SELECT c FROM Contact c WHERE c.id = :id")
    Contact findById(@Param("id") int id);

    @Query("""
            SELECT c
            FROM Contact c
            WHERE c.id = :id
              AND c.centreGestion.id IN :centreIds
            """)
    Contact findVisibleByIdForCentres(@Param("id") int id, @Param("centreIds") List<Integer> centreIds);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Contact c
            WHERE c.id = :id
              AND c.centreGestion.id IN :centreIds
            """)
    boolean existsByIdAndCentreGestionIdIn(@Param("id") Integer id, @Param("centreIds") List<Integer> centreIds);

    @Query("SELECT c FROM Contact c WHERE c.service.id = :idService")
    List<Contact> findByService(@Param("idService") int idService);

    @Query("""
            SELECT c
            FROM Contact c
            WHERE c.service.id = :idService
              AND (
                c.centreGestion.id IN :centreIds
                OR c.centreGestion.codeConfidentialite IS NULL
                OR c.centreGestion.codeConfidentialite.code = '0'
                OR (c.centreGestion.codeConfidentialite.code = '2' AND c.centreGestion.codeConfidentialiteConventionOrpheline.code = '0')
              )
            """)
    List<Contact> findByServiceVisibleForCentres(@Param("idService") int idService, @Param("centreIds") List<Integer> centreIds);

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

    /**
     * Contacts qui ne sont plus référencés par aucune donnée active : ni comme tuteur ou signataire
     * d'une convention, ni comme tuteur d'un avenant, ni sur une offre, un accord de partenariat ou
     * un token d'évaluation encore valide (non expiré). Utilisé par le nettoyage automatique.
     *
     * Projection (et non entités) pour éviter tout N+1 et ne pas peupler le contexte de persistance :
     * colonnes id, nom, prenom, mail, tel, fonction, nom du service, raison sociale de la structure,
     * loginCreation, dateCreation.
     */
    @Query("""
            SELECT c.id, c.nom, c.prenom, c.mail, c.tel, c.fonction, s.nom, st.raisonSociale, c.loginCreation, c.dateCreation
            FROM Contact c
            JOIN c.service s
            JOIN s.structure st
            WHERE NOT EXISTS (SELECT 1 FROM Convention cv WHERE cv.contact = c OR cv.signataire = c)
              AND NOT EXISTS (SELECT 1 FROM Avenant a WHERE a.contact = c)
              AND NOT EXISTS (SELECT 1 FROM Offre o WHERE o.referent = c OR o.contactCand = c OR o.contactInfo = c OR o.contactProprio = c)
              AND NOT EXISTS (SELECT 1 FROM AccordPartenariat ap WHERE ap.contact = c)
              AND NOT EXISTS (SELECT 1 FROM EvaluationTuteurToken et WHERE et.contact = c AND et.expiresAt >= :now)
            """)
    List<Object[]> findInutilisesPourNettoyage(@Param("now") Date now);

    @Modifying
    @Query("DELETE FROM Contact c WHERE c.id IN :ids")
    int deleteByIdIn(@Param("ids") List<Integer> ids);
}
