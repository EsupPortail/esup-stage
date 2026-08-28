package org.esup_portail.esup_stage.service.apogee;

import org.esup_portail.esup_stage.config.properties.ReferentielProperties;
import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.dto.RegimeInscriptionDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.TypeConvention;
import org.esup_portail.esup_stage.repository.TypeConventionJpaRepository;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.service.apogee.model.ApogeeMap;
import org.esup_portail.esup_stage.service.apogee.model.Composante;
import org.esup_portail.esup_stage.service.apogee.model.EtapeApogee;
import org.esup_portail.esup_stage.service.apogee.model.EtapeInscription;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantDiplomeEtapeSearch;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.esup_portail.esup_stage.service.apogee.model.RegimeInscription;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Les appels HTTP vers le référentiel Apogée sont simulés par une ExchangeFunction :
 * la table {@code reponses} associe un chemin d'API à son corps JSON.
 */
class ApogeeServiceTest {

    private ApogeeService service;
    private final Map<String, String> reponses = new HashMap<>();
    private final AtomicReference<String> derniereUrl = new AtomicReference<>();
    private HttpStatus statutForce;
    private ReferentielProperties referentielProperties;
    private AppConfigService appConfigService;
    private TypeConventionJpaRepository typeConventionJpaRepository;
    private LdapService ldapService;

    @BeforeEach
    void setUp() {
        statutForce = null;
        ExchangeFunction exchange = request -> {
            derniereUrl.set(request.url().toString());
            HttpStatus statut = statutForce != null ? statutForce : HttpStatus.OK;
            String corps = reponses.getOrDefault(request.url().getPath(), "");
            return Mono.just(ClientResponse.create(statut)
                    .header("Content-Type", "application/json")
                    .body(corps)
                    .build());
        };
        service = new ApogeeService(WebClient.builder().exchangeFunction(exchange));

        referentielProperties = mock(ReferentielProperties.class);
        when(referentielProperties.getApogeeUrl()).thenReturn("http://apogee.test");
        when(referentielProperties.getLogin()).thenReturn("user");
        when(referentielProperties.getPassword()).thenReturn("secret");
        appConfigService = mock(AppConfigService.class);
        typeConventionJpaRepository = mock(TypeConventionJpaRepository.class);
        ldapService = mock(LdapService.class);
        service.referentielProperties = referentielProperties;
        service.appConfigService = appConfigService;
        service.typeConventionJpaRepository = typeConventionJpaRepository;
        service.ldapService = ldapService;
    }

    @Test
    void getInfoApogeeCompleteLeMailDepuisLeLdap() {
        reponses.put("/etudiantRef", "{\"mail\":\"\"}");
        LdapUser ldapUser = mock(LdapUser.class);
        when(ldapUser.getMail()).thenReturn("etu@univ.fr");
        when(ldapService.search(eq("/etudiant"), any(LdapSearchDto.class))).thenReturn(List.of(ldapUser));

        EtudiantRef etudiantRef = service.getInfoApogee("123", "2025");

        assertThat(etudiantRef.getMail()).isEqualTo("etu@univ.fr");
        assertThat(derniereUrl.get()).contains("codEtud=123").contains("annee=2025");
    }

    @Test
    void getInfoApogeeConserveLeMailApogee() {
        reponses.put("/etudiantRef", "{\"mail\":\"apogee@univ.fr\"}");

        assertThat(service.getInfoApogee("123", "2025").getMail()).isEqualTo("apogee@univ.fr");
    }

    @Test
    void uneReponseVideEstUneDonneeIntrouvable() {
        reponses.put("/etudiantRef", "");

        assertThatThrownBy(() -> service.getInfoApogee("123", "2025"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void uneErreurServeurEstUneErreurTechnique() {
        statutForce = HttpStatus.INTERNAL_SERVER_ERROR;

        assertThatThrownBy(() -> service.getInfoApogee("123", "2025"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void lesComposantesEtEtapesSontParsees() {
        reponses.put("/composantesPrincipalesRef", "{\"UFR1\":\"Sciences\",\"UFR2\":\"Droit\"}");
        List<Composante> composantes = service.getListComposante();
        assertThat(composantes).hasSize(2);
        assertThat(composantes).extracting(Composante::getCode).contains("UFR1", "UFR2");

        reponses.put("/etapesReference", "{\"M1INFO;100\":\"Master 1 Informatique\"}");
        List<EtapeApogee> etapes = service.getListEtape();
        assertThat(etapes).hasSize(1);
        assertThat(etapes.get(0).getCode()).isEqualTo("M1INFO");
        assertThat(etapes.get(0).getCodeVrsEtp()).isEqualTo("100");
        assertThat(etapes.get(0).getLibelle()).isEqualTo("Master 1 Informatique");
    }

    @Test
    void lesAnneesEtLesListesSontParsees() {
        reponses.put("/anneesIa", "[\"2024\",\"2025\"]");
        assertThat(service.getAnneeInscriptions("123")).containsExactly("2024", "2025");

        reponses.put("/diplomesReferenceParComposanteEtAnnee", "[]");
        assertThat(service.getListDiplomeEtape("UFR1", "2025")).isEmpty();

        reponses.put("/etapesByEtudiantAndAnnee", "{}");
        ApogeeMap apogeeMap = service.getEtudiantEtapesInscription("123", "2025");
        assertThat(apogeeMap).isNotNull();

        reponses.put("/anneesIa", "pas du json");
        assertThatThrownBy(() -> service.getAnneeInscriptions("123")).isInstanceOf(AppException.class);
    }

    @Test
    void lesInfosAdministrativesAbsentesRetournentNull() {
        reponses.put("/infosAdmEtu", "");
        assertThat(service.getInfosAdmEtudiant("123")).isNull();

        reponses.put("/infosAdmEtu", "{}");
        assertThat(service.getInfosAdmEtudiant("123")).isNotNull();
    }

    @Test
    void lesRegimesDInscriptionSontMappes() {
        reponses.put("/regimesInscriptions", "{\"FI\":\"Formation initiale\"}");

        List<RegimeInscriptionDto> regimes = service.getRegimesInscriptions();

        assertThat(regimes).hasSize(1);
        assertThat(regimes.get(0).getCode()).isEqualTo("FI");
    }

    @Test
    void lesRegimesDInscriptionSontMappesDepuisLeFormatListe() {
        reponses.put("/regimesInscriptions", """
                [
                  {"codeRegimeInscription":"1","libelleRegimeInscription":"Formation initiale","libelleRegimeInscriptionCourt":"FI"},
                  {"codeRegimeInscription":"2","libelleRegimeInscription":"Formation continue","libelleRegimeInscriptionCourt":"FC"}
                ]""");

        List<RegimeInscriptionDto> regimes = service.getRegimesInscriptions();

        assertThat(regimes).hasSize(2);
        assertThat(regimes.get(0).getCode()).isEqualTo("1");
        assertThat(regimes.get(0).getLibelle()).isEqualTo("Formation initiale");
        assertThat(regimes.get(1).getCode()).isEqualTo("2");
        assertThat(regimes.get(0).getLibelleCourt()).isEqualTo("FI");
        assertThat(regimes.get(1).getLibelleCourt()).isEqualTo("FC");
        assertThat(regimes.get(1).getLibelle()).isEqualTo("Formation continue");
    }

    @Test
    void leLibelleCourtSertDeSecoursEtLesEntreesSansCodeSontIgnorees() {
        reponses.put("/regimesInscriptions", """
                [
                  {"codeRegimeInscription":"1","libelleRegimeInscriptionCourt":"FI"},
                  {"codeRegimeInscription":"2"},
                  {"codeRegimeInscription":"  ","libelleRegimeInscription":"Sans code"},
                  {"libelleRegimeInscription":"Code absent"}
                ]""");

        List<RegimeInscriptionDto> regimes = service.getRegimesInscriptions();

        assertThat(regimes).hasSize(2);
        assertThat(regimes.get(0).getLibelle()).isEqualTo("FI");
        assertThat(regimes.get(1).getLibelle()).isEqualTo("2");
    }

    @Test
    void unFormatInattenduDeRegimesDInscriptionRenvoieUneListeVide() {
        reponses.put("/regimesInscriptions", "\"inattendu\"");

        assertThat(service.getRegimesInscriptions()).isEmpty();
    }

    @Test
    void laRechercheDEtudiantsParDiplomeEtapePasseTousLesFiltres() {
        reponses.put("/listEtuParEtapeEtDiplome", "[]");
        EtudiantDiplomeEtapeSearch recherche = mock(EtudiantDiplomeEtapeSearch.class);
        when(recherche.getAnnee()).thenReturn("2025");
        when(recherche.getCodeEtape()).thenReturn("M1INFO");

        assertThat(service.getEtudiantsParDiplomeEtape(recherche)).isEmpty();
        assertThat(derniereUrl.get()).contains("annee=2025").contains("codeEtape=M1INFO");
    }

    @Test
    void leTypeConventionCesureEstResoluDepuisLaConfiguration() {
        ConfigGeneraleDto config = mock(ConfigGeneraleDto.class);
        when(config.getCodeCesure()).thenReturn("CES1;CES2");
        when(appConfigService.getConfigGenerale()).thenReturn(config);
        TypeConvention cesure = new TypeConvention();
        when(typeConventionJpaRepository.findByCodeCtrl("CESURE")).thenReturn(cesure);

        assertThat(service.changeTypeConventionByCodeCursus("CES1")).isSameAs(cesure);
        assertThat(service.changeTypeConventionByCodeCursus("AUTRE")).isNull();
        assertThat(service.changeTypeConventionByCodeCursus(null)).isNull();
        assertThat(service.changeTypeConventionByCodeCursus("")).isNull();
    }

    @Test
    void resolveTypeConventionNeChoisitQueSiUnSeulCandidat() {
        ConfigGeneraleDto config = mock(ConfigGeneraleDto.class);
        when(appConfigService.getConfigGenerale()).thenReturn(config);
        EtapeInscription etape = mock(EtapeInscription.class);
        CentreGestion centreGestion = new CentreGestion();

        RegimeInscription regime = mock(RegimeInscription.class);
        when(regime.getCodRegIns()).thenReturn("FI");
        TypeConvention unique = new TypeConvention();
        when(typeConventionJpaRepository.findAllActiveByCodeRegimeInscription("FI")).thenReturn(List.of(unique));
        assertThat(service.resolveTypeConvention(regime, etape, centreGestion)).isSameAs(unique);

        // plusieurs candidats : pas de sélection automatique
        when(typeConventionJpaRepository.findAllActiveByCodeRegimeInscription("FI"))
                .thenReturn(List.of(new TypeConvention(), new TypeConvention()));
        assertThat(service.resolveTypeConvention(regime, etape, centreGestion)).isNull();

        // pas de régime : aucune proposition
        assertThat(service.resolveTypeConvention(null, etape, centreGestion)).isNull();

        // sélection automatique désactivée sur le centre
        centreGestion.setDesactiverSelectionAutomatiqueTypeConvention(true);
        when(typeConventionJpaRepository.findAllActiveByCodeRegimeInscription("FI")).thenReturn(List.of(unique));
        assertThat(service.resolveTypeConvention(regime, etape, centreGestion)).isNull();
    }
}
