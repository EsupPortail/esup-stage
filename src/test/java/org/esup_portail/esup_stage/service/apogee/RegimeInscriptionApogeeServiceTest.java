package org.esup_portail.esup_stage.service.apogee;

import org.esup_portail.esup_stage.dto.RegimeInscriptionDto;
import org.esup_portail.esup_stage.model.RegimeInscriptionApogee;
import org.esup_portail.esup_stage.repository.RegimeInscriptionApogeeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegimeInscriptionApogeeServiceTest {

    private RegimeInscriptionApogeeJpaRepository repository;
    private RegimeInscriptionApogeeService service;

    @BeforeEach
    void setUp() {
        repository = mock(RegimeInscriptionApogeeJpaRepository.class);
        service = new RegimeInscriptionApogeeService();
        service.regimeInscriptionApogeeJpaRepository = repository;
        service.apogeeService = mock(ApogeeService.class);
        when(repository.findAll()).thenReturn(List.of());
        when(repository.findByTemEnServOrderByLibelle(anyString())).thenReturn(List.of());
    }

    @SuppressWarnings("unchecked")
    private List<RegimeInscriptionApogee> capturerSauvegarde() {
        ArgumentCaptor<List<RegimeInscriptionApogee>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    void leLibelleCourtEstEnregistre() {
        service.synchroniserDepuisApogee(List.of(
                new RegimeInscriptionDto("1", "Formation initiale", "FI"),
                new RegimeInscriptionDto("2", "Formation continue", "FC")
        ));

        List<RegimeInscriptionApogee> sauvegardes = capturerSauvegarde();
        assertThat(sauvegardes).hasSize(2);
        assertThat(sauvegardes.get(0).getCode()).isEqualTo("1");
        assertThat(sauvegardes.get(0).getLibelle()).isEqualTo("Formation initiale");
        assertThat(sauvegardes.get(0).getLibelleCourt()).isEqualTo("FI");
        assertThat(sauvegardes.get(1).getLibelleCourt()).isEqualTo("FC");
    }

    @Test
    void leLibelleCourtExistantEstConserveQuandApogeeNeLeFournitPas() {
        RegimeInscriptionApogee existant = new RegimeInscriptionApogee();
        existant.setCode("1");
        existant.setLibelle("Ancien libellé");
        existant.setLibelleCourt("FI");
        when(repository.findAll()).thenReturn(List.of(existant));

        // format historique d'ESUP-SISCOL : Map code/libellé, sans libellé court
        service.synchroniserDepuisApogee(List.of(new RegimeInscriptionDto("1", "Formation initiale")));

        List<RegimeInscriptionApogee> sauvegardes = capturerSauvegarde();
        assertThat(sauvegardes).hasSize(1);
        assertThat(sauvegardes.get(0).getLibelle()).isEqualTo("Formation initiale");
        assertThat(sauvegardes.get(0).getLibelleCourt()).isEqualTo("FI");
        assertThat(sauvegardes.get(0).getTemEnServ()).isEqualTo("O");
    }

    @Test
    void unRegimeAbsentDApogeeEstDesactive() {
        RegimeInscriptionApogee obsolete = new RegimeInscriptionApogee();
        obsolete.setCode("9");
        obsolete.setLibelle("Obsolète");
        when(repository.findAll()).thenReturn(List.of(obsolete));

        service.synchroniserDepuisApogee(List.of(new RegimeInscriptionDto("1", "Formation initiale", "FI")));

        List<RegimeInscriptionApogee> sauvegardes = capturerSauvegarde();
        assertThat(sauvegardes).hasSize(2);
        assertThat(sauvegardes.get(1).getCode()).isEqualTo("9");
        assertThat(sauvegardes.get(1).getTemEnServ()).isEqualTo("N");
    }

    @Test
    void uneReponseApogeeVideNeDesactiveRien() {
        service.synchroniserDepuisApogee(List.of());

        verify(repository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void leLibelleCourtEstRemonteDansLeDto() {
        RegimeInscriptionApogee regime = new RegimeInscriptionApogee();
        regime.setCode("1");
        regime.setLibelle("Formation initiale");
        regime.setLibelleCourt("FI");
        when(repository.findByTemEnServOrderByLibelle("O")).thenReturn(List.of(regime));

        List<RegimeInscriptionDto> dtos = service.getRegimesInscriptions();

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getLibelleCourt()).isEqualTo("FI");
    }
}
