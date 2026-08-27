package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.PaysDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Pays;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.PaysJpaRepository;
import org.esup_portail.esup_stage.repository.PaysRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur des pays (nomenclature). Le contrôleur est instancié à
 * la main et ses repositories sont mockés ; les annotations {@code @Secure} ne sont pas
 * évaluées hors contexte, on teste donc la logique métier pure.
 */
class PaysControllerTest {

    private PaysController controller;
    private PaysRepository paysRepository;
    private PaysJpaRepository paysJpaRepository;
    private ConventionJpaRepository conventionJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new PaysController();
        paysRepository = mock(PaysRepository.class);
        paysJpaRepository = mock(PaysJpaRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        controller.paysRepository = paysRepository;
        controller.paysJpaRepository = paysJpaRepository;
        controller.conventionJpaRepository = conventionJpaRepository;
    }

    private Pays pays(int id, String lib, String iso2) {
        Pays pays = new Pays();
        pays.setId(id);
        pays.setLib(lib);
        pays.setIso2(iso2);
        pays.setTemEnServPays("O");
        return pays;
    }

    @Test
    void searchRemonteLaFranceEnTeteDeListe() {
        when(paysRepository.count("{}")).thenReturn(2L);
        when(paysRepository.findPaginated(1, 50, "lib", "asc", "{}"))
                .thenReturn(List.of(pays(1, "Allemagne", "DE"), pays(2, "France", "FR")));

        PaginatedResponse<PaysDto> reponse =
                controller.search(1, 50, "lib", "asc", "{}", null);

        assertThat(reponse.getTotal()).isEqualTo(2L);
        assertThat(reponse.getData()).extracting(PaysDto::getLibelle)
                .containsExactly("France", "Allemagne");
    }

    @Test
    void createRefuseUnLibelleDejaExistant() {
        Pays pays = pays(0, "France", "FR");
        when(paysRepository.exists(pays)).thenReturn(true);

        assertThatThrownBy(() -> controller.create(pays))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createPositionneLeTemoinEnServiceEtEnregistre() {
        Pays pays = pays(0, "Belgique", "BE");
        pays.setTemEnServPays(null);
        when(paysRepository.exists(pays)).thenReturn(false);
        when(paysJpaRepository.saveAndFlush(any(Pays.class))).thenAnswer(inv -> inv.getArgument(0));

        Pays resultat = controller.create(pays);

        assertThat(resultat.getTemEnServPays()).isEqualTo("O");
        verify(paysJpaRepository).saveAndFlush(pays);
    }

    @Test
    void updateModifieLibelleEtTemoin() {
        Pays existant = pays(5, "Espagn", "ES");
        when(paysJpaRepository.findById(5)).thenReturn(existant);
        when(paysJpaRepository.saveAndFlush(any(Pays.class))).thenAnswer(inv -> inv.getArgument(0));

        PaysDto requete = new PaysDto(5, "Espagne", "N");
        PaysDto resultat = controller.update(5, requete);

        assertThat(resultat.getLibelle()).isEqualTo("Espagne");
        assertThat(resultat.getTemEnServ()).isEqualTo("N");
    }

    @Test
    void updateLaisseLeTemoinInchangeQuandNonFourni() {
        Pays existant = pays(5, "Italie", "IT");
        when(paysJpaRepository.findById(5)).thenReturn(existant);
        when(paysJpaRepository.saveAndFlush(any(Pays.class))).thenAnswer(inv -> inv.getArgument(0));

        PaysDto requete = new PaysDto(5, "Italie", null);
        PaysDto resultat = controller.update(5, requete);

        assertThat(resultat.getTemEnServ()).isEqualTo("O");
    }

    @Test
    void deleteRefuseSiDesConventionsUtilisentLePays() {
        when(conventionJpaRepository.countConventionWithPays(9)).thenReturn(3L);

        assertThatThrownBy(() -> controller.delete(9))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(paysJpaRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteSupprimeQuandAucuneConventionNeLUtilise() {
        when(conventionJpaRepository.countConventionWithPays(9)).thenReturn(0L);

        controller.delete(9);

        verify(paysJpaRepository).deleteById(9);
        verify(paysJpaRepository).flush();
    }
}
