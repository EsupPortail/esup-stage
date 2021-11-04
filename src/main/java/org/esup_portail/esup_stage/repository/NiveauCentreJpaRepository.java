package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.NiveauCentre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NiveauCentreJpaRepository extends JpaRepository<NiveauCentre, Integer> {

    List<NiveauCentre> findAll();

    @Query("SELECT nc FROM NiveauCentre nc WHERE nc.libelle = 'ETABLISSEMENT'")
    List<NiveauCentre> getListVide();

    @Query("SELECT nc FROM NiveauCentre nc WHERE nc.libelle <> 'ETAPE' AND nc.libelle <> 'ENTREPRISE'")
    List<NiveauCentre> getListComposante();

    @Query("SELECT nc FROM NiveauCentre nc WHERE nc.libelle <> 'UFR' AND nc.libelle <> 'ENTREPRISE'")
    List<NiveauCentre> getListEtape();

    @Query("SELECT nc FROM NiveauCentre nc WHERE nc.libelle <> 'ENTREPRISE'")
    List<NiveauCentre> getListMixte();
}
