package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.UtilisateurCentreGestionRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface UtilisateurCentreGestionRoleJpaRepository extends JpaRepository<UtilisateurCentreGestionRole, Integer> {

    List<UtilisateurCentreGestionRole> findByUtilisateurId(int idUtilisateur);

    List<UtilisateurCentreGestionRole> findByUtilisateurIdAndCentreGestionId(int idUtilisateur, int idCentreGestion);

    @Query("SELECT ucr FROM UtilisateurCentreGestionRole ucr WHERE ucr.utilisateur.id = :idUtilisateur AND ucr.centreGestion.id IN :idsCentreGestion")
    List<UtilisateurCentreGestionRole> findByUtilisateurIdAndCentreGestionIds(@Param("idUtilisateur") int idUtilisateur, @Param("idsCentreGestion") Collection<Integer> idsCentreGestion);

    @Query("SELECT ucr FROM UtilisateurCentreGestionRole ucr WHERE ucr.utilisateur.uid = :uid AND ucr.centreGestion.id = :idCentreGestion")
    List<UtilisateurCentreGestionRole> findByUtilisateurUidAndCentreGestionId(@Param("uid") String uid, @Param("idCentreGestion") int idCentreGestion);

    @Query("SELECT ucr FROM UtilisateurCentreGestionRole ucr WHERE ucr.utilisateur.uid IN :uids AND ucr.centreGestion.id = :idCentreGestion")
    List<UtilisateurCentreGestionRole> findByUtilisateurUidsAndCentreGestionId(@Param("uids") Collection<String> uids, @Param("idCentreGestion") int idCentreGestion);

    @Query("SELECT COUNT(ucr.id) FROM UtilisateurCentreGestionRole ucr WHERE ucr.role.id = :idRole")
    Long countUserCentreWithRole(@Param("idRole") int idRole);

    @Transactional
    @Modifying
    @Query("DELETE FROM UtilisateurCentreGestionRole ucr WHERE ucr.utilisateur.id = :idUtilisateur AND ucr.centreGestion.id = :idCentreGestion")
    void deleteByUtilisateurIdAndCentreGestionId(@Param("idUtilisateur") int idUtilisateur, @Param("idCentreGestion") int idCentreGestion);

    @Transactional
    @Modifying
    @Query("DELETE FROM UtilisateurCentreGestionRole ucr WHERE ucr.centreGestion.id = :idCentreGestion")
    void deleteByCentreGestionId(@Param("idCentreGestion") int idCentreGestion);
}
