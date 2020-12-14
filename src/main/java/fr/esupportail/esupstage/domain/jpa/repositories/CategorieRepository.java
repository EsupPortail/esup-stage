package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Categorie;

public interface CategorieRepository extends JpaRepository<Categorie, Integer> {
}
