package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Structure;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class StructureRepository extends PaginationRepository<Structure> {
    public StructureRepository(EntityManager em) {
        super(em, Structure.class, "s");
        this.predicateWhitelist = Arrays.asList("raisonSociale", "numeroSiret", "nafN5.nafN1.libelle", "pays.lib", "commune", "typeStructure.libelle", "statutJuridique.libelle");
    }
}
