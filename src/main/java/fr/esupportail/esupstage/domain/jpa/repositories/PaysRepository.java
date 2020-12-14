package fr.esupportail.esupstage.domain.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.Pays;

@Repository
public interface PaysRepository extends JpaRepository<Pays, Integer> {

	public List<Pays> findByIso2ContainingIgnoreCase(String iso2);
}
