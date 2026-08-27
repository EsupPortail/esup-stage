package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de la construction dynamique de JPQL (filtres typés, tri whitelisté)
 * et des exports Excel/CSV de PaginationRepository, au travers du repository
 * concret LangueConventionRepository (alias "lc", jointure templates).
 */
class PaginationRepositoryFiltersExportsTest {

    private EntityManager entityManager;
    private TypedQuery<Long> countQuery;
    private TypedQuery<LangueConvention> listQuery;
    private LangueConventionRepository repository;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        entityManager = mock(EntityManager.class);
        countQuery = mock(TypedQuery.class);
        listQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(entityManager.createQuery(anyString(), eq(LangueConvention.class))).thenReturn(listQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(listQuery.setParameter(anyString(), any())).thenReturn(listQuery);
        when(countQuery.getSingleResult()).thenReturn(2L);
        when(listQuery.getResultList()).thenReturn(List.of(langue("FR", "Français"), langue("EN", "Anglais")));

        // métamodèle permissif : tout attribut demandé existe
        Metamodel metamodel = mock(Metamodel.class, RETURNS_DEEP_STUBS);
        ManagedType<?> managedType = mock(ManagedType.class);
        Attribute<?, ?> attribute = mock(Attribute.class);
        when(metamodel.managedType(any(Class.class))).thenAnswer(inv -> managedType);
        when(managedType.getAttribute(anyString())).thenAnswer(inv -> attribute);
        when(entityManager.getMetamodel()).thenReturn(metamodel);

        repository = new LangueConventionRepository(entityManager);
    }

    private LangueConvention langue(String code, String libelle) {
        LangueConvention langueConvention = new LangueConvention();
        langueConvention.setCode(code);
        langueConvention.setLibelle(libelle);
        langueConvention.setTemEnServ("O");
        return langueConvention;
    }

    private String jpqlDeComptage() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(captor.capture(), eq(Long.class));
        return captor.getValue();
    }

    @Test
    void countSansFiltreNePoseAucuneClause() {
        assertThat(repository.count("{}")).isEqualTo(2L);
        assertThat(jpqlDeComptage())
                .startsWith("SELECT COUNT(DISTINCT lc) FROM")
                .doesNotContain("WHERE");
    }

    @Test
    void filtreTexteDevientLikeInsensibleALaCasse() {
        repository.count("{\"libelle\":{\"type\":\"text\",\"value\":\"Fra\"}}");

        assertThat(jpqlDeComptage()).contains("LOWER(lc.libelle) LIKE :filter0");
        verify(countQuery).setParameter("filter0", "%fra%");
    }

    @Test
    void filtreEntierDevientEgalite() {
        repository.count("{\"id\":{\"type\":\"int\",\"value\":7}}");

        assertThat(jpqlDeComptage()).contains("lc.id = :filter0");
        verify(countQuery).setParameter("filter0", 7);
    }

    @Test
    void filtreBooleenDevientEgalite() {
        repository.count("{\"actif\":{\"type\":\"boolean\",\"value\":true}}");

        assertThat(jpqlDeComptage()).contains("lc.actif = :filter0");
        verify(countQuery).setParameter("filter0", true);
    }

    @Test
    void filtresDeDatesDeviennentBornes() {
        repository.count("{\"dateDebut\":{\"type\":\"date-min\",\"value\":1700000000000},"
                + "\"dateFin\":{\"type\":\"date-max\",\"value\":1800000000000}}");

        String jpql = jpqlDeComptage();
        assertThat(jpql).contains("lc.dateDebut >= :filter0").contains("lc.dateFin <= :filter1");
        verify(countQuery).setParameter("filter0", new Date(1700000000000L));
        verify(countQuery).setParameter("filter1", new Date(1800000000000L));
    }

    @Test
    void filtreListeDevientIn() {
        repository.count("{\"code\":{\"type\":\"list\",\"value\":[\"FR\",\"EN\"]}}");

        assertThat(jpqlDeComptage()).contains("lc.code IN :filter0");
        verify(countQuery).setParameter("filter0", List.of("FR", "EN"));
    }

    @Test
    void filtreJsonInvalideEstRejete() {
        assertThatThrownBy(() -> repository.count("pas du json"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Filtres invalides");
        assertThatThrownBy(() -> repository.count("[1,2]"))
                .isInstanceOf(AppException.class);
    }

    @Test
    void filtreDeTypeInconnuEstRejete() {
        assertThatThrownBy(() -> repository.count("{\"libelle\":{\"type\":\"sql\",\"value\":\"x\"}}"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Type de filtre invalide");
    }

    @Test
    void filtreSansValeurEstRejete() {
        assertThatThrownBy(() -> repository.count("{\"libelle\":{\"type\":\"text\"}}"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Valeur de filtre manquante");
    }

    @Test
    void cleDeFiltreMalformeeEstRejetee() {
        assertThatThrownBy(() -> repository.count("{\"libelle; DROP\":{\"type\":\"text\",\"value\":\"x\"}}"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Filtre invalide");
    }

    @Test
    void filtreSpecifiqueNonAutoriseEstRejete() {
        assertThatThrownBy(() -> repository.count("{\"autre\":{\"specific\":true,\"value\":1}}"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Filtre spécifique non autorisé");
    }

    @Test
    void triWhitelisteEstAppliqueEtPagine() {
        List<LangueConvention> resultat = repository.findPaginated(2, 10, "libelle", "desc", "{}");

        assertThat(resultat).hasSize(2);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(captor.capture(), eq(LangueConvention.class));
        assertThat(captor.getValue()).contains("ORDER BY lc.libelle DESC");
        verify(listQuery).setFirstResult(10);
        verify(listQuery).setMaxResults(10);
    }

    @Test
    void triHorsWhitelisteEstIgnore() {
        repository.findPaginated(1, 10, "code; DROP TABLE", "asc", "{}");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(captor.capture(), eq(LangueConvention.class));
        assertThat(captor.getValue()).doesNotContain("ORDER BY");
    }

    @Test
    void exportExcelMonoOngletContientEntetesEtValeurs() throws IOException {
        byte[] bytes = repository.exportExcel(
                "{\"singleExcelSheet\":{\"title\":\"Langues\",\"columns\":{"
                        + "\"libelle\":{\"title\":\"Libellé\"},\"actif\":{\"title\":\"Actif\"}}}}",
                null, "asc", "{}");

        try (Workbook workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Langues");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Libellé");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Français");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("Oui");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("Anglais");
        }
    }

    @Test
    void exportExcelMultiOngletsCreeChaqueFeuille() throws IOException {
        byte[] bytes = repository.exportExcel(
                "{\"multipleExcelSheets\":["
                        + "{\"title\":\"Feuille1\",\"columns\":{\"libelle\":{\"title\":\"Libellé\"}}},"
                        + "{\"title\":\"Feuille2\",\"columns\":{\"actif\":{\"title\":\"Actif\"}}}]}",
                null, "asc", "{}");

        try (Workbook workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(workbook.getSheet("Feuille1")).isNotNull();
            assertThat(workbook.getSheet("Feuille2")).isNotNull();
        }
    }

    @Test
    void exportExcelAvecEnteteInvalideEstRejete() {
        assertThatThrownBy(() -> repository.exportExcel("", null, "asc", "{}"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Entête invalide");
    }

    @Test
    void exportCsvProduitEntetesEtLignes() {
        StringBuilder csv = repository.exportCsv(
                "{\"libelle\":{\"title\":\"Libellé\"},\"actif\":{\"title\":\"Actif\"}}",
                null, "asc", "{}");

        String[] lignes = csv.toString().split(System.lineSeparator());
        assertThat(lignes[0]).isEqualTo("\"Libellé\";\"Actif\";");
        assertThat(lignes[1]).isEqualTo("\"Français\";\"Oui\";");
        assertThat(lignes[2]).isEqualTo("\"Anglais\";\"Oui\";");
    }

    @Test
    void exportCsvFusionneLesColonnesDesOngletsMultiples() {
        StringBuilder csv = repository.exportCsv(
                "{\"multipleExcelSheets\":["
                        + "{\"columns\":{\"libelle\":{\"title\":\"Libellé\"}}},"
                        + "{\"columns\":{\"actif\":{\"title\":\"Actif\"}}}]}",
                null, "asc", "{}");

        assertThat(csv.toString()).startsWith("\"Libellé\";\"Actif\";");
    }

    @Test
    void filtreSpecifiqueWhitelisteEstDelegueAuRepositoryConcret() {
        repository.count("{\"typeConventionTemplate\":{\"specific\":true,\"value\":4}}");

        assertThat(jpqlDeComptage()).contains("template.typeConvention.id = :typeConventionTemplate");
        verify(countQuery).setParameter("typeConventionTemplate", 4);
    }
}
