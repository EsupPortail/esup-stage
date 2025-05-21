package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilisateurJpaRepository extends JpaRepository<Utilisateur, Integer> {

    Utilisateur findById(int id);

    @Query("SELECT u FROM Utilisateur u WHERE u.id = :id AND u.actif = true")
    Utilisateur findByIdActif(@Param("id") int id);

    @Query("SELECT u FROM Utilisateur u WHERE u.login = :login AND u.actif = true")
    Utilisateur findOneByLoginAcitf(@Param("login") String login);

    @Query("SELECT u FROM Utilisateur u WHERE u.login = ?1")
    Utilisateur findOneByLogin(@Param("login") String login);

    @Query("SELECT u FROM Utilisateur u WHERE u.uid = :uid")
    Utilisateur findOneByUid(@Param("uid") String uid);

    @Query("SELECT COUNT(u.id) FROM Utilisateur u JOIN u.roles r WHERE r.id = :idRole")
    Long countUserWithRole(@Param("idRole") int idRole);

    @Query("SELECT u FROM Utilisateur u WHERE u.login IN :logins")
    List<Utilisateur> findByLogins(@Param("logins") List<String> logins);

    @Query("SELECT u FROM Utilisateur u WHERE u.uid IN :uids")
    List<Utilisateur> findByUids(@Param("uids") List<String> uids);

    @Query("SELECT u FROM Utilisateur u WHERE u.uid IS NULL AND u.actif = TRUE")
    List<Utilisateur> findNoUid();

    Utilisateur findByLogin(String loginEnvoiSignature);
}
