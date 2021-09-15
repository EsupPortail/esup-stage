package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Utilisateur;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class UtilisateurRepository extends PaginationRepository<Utilisateur> {

    public UtilisateurRepository(EntityManager em) {
        super(em, Utilisateur.class, "u");
        this.predicateWhitelist = Arrays.asList("login", "nom", "prenom", "actif");
    }
}
