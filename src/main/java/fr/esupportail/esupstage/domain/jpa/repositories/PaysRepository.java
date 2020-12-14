package fr.esupportail.esupstage.domain.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.esupportail.esupstage.domain.jpa.entities.Pays;

public interface PaysRepository extends JpaRepository<Pays, Integer> {

	public List<Pays> findByIso2ContainingIgnoreCase(String iso2);
}
