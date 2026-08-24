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
     *
     * Performance : un seul critère par sous-requête, jamais de OR entre deux colonnes — un OR
     * (ex. « cv.contact = c OR cv.signataire = c ») empêche MySQL d'utiliser les index de clé
     * étrangère et dégénère en balayage complet pour chaque contact.
     */
    @Query("""
            SELECT c.id, c.nom, c.prenom, c.mail, c.tel, c.fonction, s.nom, st.raisonSociale, c.loginCreation, c.dateCreation
            FROM Contact c
            JOIN c.service s
            JOIN s.structure st
            WHERE NOT EXISTS (SELECT 1 FROM Convention cv1 WHERE cv1.contact = c)
              AND NOT EXISTS (SELECT 1 FROM Convention cv2 WHERE cv2.signataire = c)
              AND NOT EXISTS (SELECT 1 FROM Avenant a WHERE a.contact = c)
              AND NOT EXISTS (SELECT 1 FROM EvaluationTuteurToken et WHERE et.contact = c AND et.expiresAt >= :now)
              AND NOT EXISTS (SELECT 1 FROM Offre o1 WHERE o1.referent = c)
              AND NOT EXISTS (SELECT 1 FROM Offre o2 WHERE o2.contactCand = c)
              AND NOT EXISTS (SELECT 1 FROM Offre o3 WHERE o3.contactInfo = c)
              AND NOT EXISTS (SELECT 1 FROM Offre o4 WHERE o4.contactProprio = c)
              AND NOT EXISTS (SELECT 1 FROM AccordPartenariat ap WHERE ap.contact = c)
            """)
    List<Object[]> findInutilisesPourNettoyage(@Param("now") Date now);

    @Modifying
    @Query("DELETE FROM Contact c WHERE c.id IN :ids")
    int deleteByIdIn(@Param("ids") List<Integer> ids);
}
