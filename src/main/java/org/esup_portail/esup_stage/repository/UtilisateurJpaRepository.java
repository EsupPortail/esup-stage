package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilisateurJpaRepository extends JpaRepository<Utilisateur, Integer> {

    Utilisateur findById(int id);

    @Query("SELECT u FROM Utilisateur u WHERE u.id = :id AND u.actif = true")
    Utilisateur findByIdActif(int id);

    @Query("SELECT u FROM Utilisateur u WHERE u.login = :login AND u.actif = true")
    Utilisateur findOneByLoginAcitf(String login);

    @Query("SELECT u FROM Utilisateur u WHERE u.login = :login")
    Utilisateur findOneByLogin(String login);

    @Query("SELECT COUNT(u.id) FROM Utilisateur u JOIN u.roles r WHERE r.id = :idRole")
    Long countUserWithRole(int idRole);

    @Query("SELECT u FROM Utilisateur u WHERE u.login IN :logins")
    List<Utilisateur> findByLogins(List<String> logins);
}
