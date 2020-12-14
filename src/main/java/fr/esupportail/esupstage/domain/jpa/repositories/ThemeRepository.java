package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Integer> {
}
