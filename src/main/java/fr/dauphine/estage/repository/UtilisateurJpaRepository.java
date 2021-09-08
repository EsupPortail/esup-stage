package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurJpaRepository extends JpaRepository<Utilisateur, Integer> {

    Utilisateur findById(int id);

    @Query("SELECT u FROM Utilisateur u WHERE u.login = :login AND u.actif = true")
    Utilisateur findOneByLogin(String login);
}
