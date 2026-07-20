package org.esup_portail.esup_stage.service.sirene;

import org.esup_portail.esup_stage.config.properties.SireneProperties;
import org.esup_portail.esup_stage.events.StructureUpdatedEvent;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.esup_portail.esup_stage.service.sirene.model.ListStructureSireneDTO;
import org.esup_portail.esup_stage.service.sirene.model.SirenResponse;
import org.esup_portail.esup_stage.service.sirene.utils.SireneMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SireneServiceTest {

    private SireneService service;
    private RestTemplate restTemplate;
    private SireneMapper sireneMapper;
    private StructureJpaRepository structureJpaRepository;
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        service = new SireneService();
        restTemplate = mock(RestTemplate.class);
        sireneMapper = mock(SireneMapper.class);
        structureJpaRepository = mock(StructureJpaRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        SireneProperties sireneProperties = mock(SireneProperties.class);
        when(sireneProperties.getUrl()).thenReturn("http://sirene.test");
        when(sireneProperties.getToken()).thenReturn("token");
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "sirenMapper", sireneMapper);
        ReflectionTestUtils.setField(service, "sireneProperties", sireneProperties);
        ReflectionTestUtils.setField(service, "structureJpaRepository", structureJpaRepository);
        ReflectionTestUtils.setField(service, "eventPublisher", eventPublisher);
    }

    private ResponseEntity<SirenResponse> reponseOk(SirenResponse corps) {
        return new ResponseEntity<>(corps, HttpStatus.OK);
    }

    @Test
    void getEtablissementMappeLaReponseSirene() {
        SirenResponse corps = mock(SirenResponse.class);
        Structure structure = new Structure();
        when(sireneMapper.toStructureList(corps)).thenReturn(List.of(structure));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenReturn(reponseOk(corps));

        assertThat(service.getEtablissement("12345678901234")).isSameAs(structure);
    }

    @Test
    void getEtablissementRetourneNullEnCasDErreur() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenThrow(new RuntimeException("réseau indisponible"));
        assertThat(service.getEtablissement("12345678901234")).isNull();

        org.mockito.Mockito.reset(restTemplate);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
        assertThat(service.getEtablissement("12345678901234")).isNull();
    }

    @Test
    void laRechercheFiltreeRetourneLeTotalEtLesStructures() {
        SirenResponse corps = mock(SirenResponse.class, RETURNS_DEEP_STUBS);
        when(corps.getHeader().getTotal()).thenReturn(5);
        Structure structure = new Structure();
        when(sireneMapper.toStructureList(corps)).thenReturn(List.of(structure));
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenReturn(reponseOk(corps));

        ListStructureSireneDTO dto = service.getEtablissementFiltered(1, 20, "{}");

        assertThat(dto.getTotal()).isEqualTo(5);
        assertThat(dto.getStructures()).containsExactly(structure);
    }

    @Test
    void laRechercheFiltreeTraduitLesErreursHttp() {
        // 404 : liste vide
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "nf", HttpHeaders.EMPTY, new byte[0], null));
        assertThat(service.getEtablissementFiltered(1, 20, "{}").getTotal()).isZero();

        // 400 : filtres invalides
        org.mockito.Mockito.reset(restTemplate);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "bad", HttpHeaders.EMPTY, new byte[0], null));
        assertThatThrownBy(() -> service.getEtablissementFiltered(1, 20, "{}"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("filtres");

        // 401 : avalée, liste vide
        org.mockito.Mockito.reset(restTemplate);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "ko", HttpHeaders.EMPTY, new byte[0], null));
        assertThat(service.getEtablissementFiltered(1, 20, "{}").getStructures()).isEmpty();

        // erreur générique : avalée, liste vide
        org.mockito.Mockito.reset(restTemplate);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenThrow(new RuntimeException("boom"));
        assertThat(service.getEtablissementFiltered(1, 20, "{}").getStructures()).isEmpty();
    }

    @Test
    void getAllEtablissementsRetourneLaListeOuUneListeVide() {
        SirenResponse corps = mock(SirenResponse.class);
        when(sireneMapper.toStructureList(corps)).thenReturn(List.of(new Structure()));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenReturn(reponseOk(corps));
        assertThat(service.getAllEtablissements()).hasSize(1);

        org.mockito.Mockito.reset(restTemplate);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenThrow(new RuntimeException("réseau"));
        assertThat(service.getAllEtablissements()).isEmpty();
    }

    private SirenResponse reponseDiffusion(String statut) {
        SirenResponse corps = mock(SirenResponse.class, RETURNS_DEEP_STUBS);
        when(corps.getEtablissement().getUniteLegale().getStatutDiffusionUniteLegale()).thenReturn(statut);
        return corps;
    }

    @Test
    void updateSynchroniseUneStructureDiffusable() {
        Structure structure = new Structure();
        structure.setNumeroSiret("12345678901234");
        SirenResponse corps = reponseDiffusion("O");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenReturn(reponseOk(corps));
        when(sireneMapper.updateStructure(corps, structure)).thenReturn(structure);

        service.update("{}", structure);

        assertThat(structure.isTemSiren()).isTrue();
        verify(structureJpaRepository).save(structure);
        verify(eventPublisher).publishEvent(any(StructureUpdatedEvent.class));
    }

    @Test
    void updateVerrouilleUneStructureNonDiffusable() {
        Structure structure = new Structure();
        structure.setNumeroSiret("12345678901234");
        structure.setTemDiffusibleSirene(true);
        SirenResponse nonDiffusable = reponseDiffusion("P");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenReturn(reponseOk(nonDiffusable));

        service.update("{}", structure);

        assertThat(structure.isTemDiffusibleSirene()).isFalse();
        assertThat(structure.isVerrouillageSynchroStructureSirene()).isTrue();
        verify(structureJpaRepository).save(structure);

        // déjà marquée non diffusable : aucune écriture supplémentaire
        Structure deja = new Structure();
        deja.setNumeroSiret("98765432109876");
        deja.setTemDiffusibleSirene(false);
        service.update("{}", deja);
        verify(structureJpaRepository, org.mockito.Mockito.times(1)).save(any(Structure.class));
    }

    @Test
    void updateAvaleLesErreursTechniques() {
        Structure structure = new Structure();
        structure.setNumeroSiret("12345678901234");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SirenResponse.class)))
                .thenThrow(new RuntimeException("réseau"));

        service.update("{}", structure);

        verify(structureJpaRepository, never()).save(any(Structure.class));
    }
}
