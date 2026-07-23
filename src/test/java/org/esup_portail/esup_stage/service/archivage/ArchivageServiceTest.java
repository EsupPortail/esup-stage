package org.esup_portail.esup_stage.service.archivage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.Etudiant;
import org.esup_portail.esup_stage.model.EtudiantGroupeEtudiant;
import org.esup_portail.esup_stage.model.GroupeEtudiant;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionDocumentEtudiantService;
import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArchivageServiceTest {

    private ArchivageService service;
    private AppConfigService appConfigService;
    private ConventionJpaRepository conventionJpaRepository;
    private StructureJpaRepository structureJpaRepository;
    private ConventionDocumentEtudiantService conventionDocumentEtudiantService;
    private ConventionDocumentEtudiantHistoriqueJpaRepository historiqueDocumentRepository;
    private EvaluationTuteurTokenJpaRepository evaluationTuteurTokenJpaRepository;
    private PeriodeInterruptionAvenantJpaRepository periodeInterruptionAvenantJpaRepository;
    private EtudiantGroupeEtudiantJpaRepository etudiantGroupeEtudiantJpaRepository;
    private GroupeEtudiantJpaRepository groupeEtudiantJpaRepository;
    private HistoriqueStructureJpaRepository historiqueStructureJpaRepository;
    private ServiceJpaRepository serviceJpaRepository;
    private ContactJpaRepository contactJpaRepository;
    private SignatureService signatureService;
    private AppliProperties appliProperties;
    private EntityManager em;
    private ConfigGeneraleDto configGenerale;

    private TypedQuery<Long> countQuery;
    private Query deleteQuery;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new ArchivageService();
        appConfigService = mock(AppConfigService.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        structureJpaRepository = mock(StructureJpaRepository.class);
        conventionDocumentEtudiantService = mock(ConventionDocumentEtudiantService.class);
        historiqueDocumentRepository = mock(ConventionDocumentEtudiantHistoriqueJpaRepository.class);
        evaluationTuteurTokenJpaRepository = mock(EvaluationTuteurTokenJpaRepository.class);
        periodeInterruptionAvenantJpaRepository = mock(PeriodeInterruptionAvenantJpaRepository.class);
        etudiantGroupeEtudiantJpaRepository = mock(EtudiantGroupeEtudiantJpaRepository.class);
        groupeEtudiantJpaRepository = mock(GroupeEtudiantJpaRepository.class);
        historiqueStructureJpaRepository = mock(HistoriqueStructureJpaRepository.class);
        serviceJpaRepository = mock(ServiceJpaRepository.class);
        contactJpaRepository = mock(ContactJpaRepository.class);
        signatureService = mock(SignatureService.class);
        appliProperties = mock(AppliProperties.class);
        em = mock(EntityManager.class);

        ReflectionTestUtils.setField(service, "appConfigService", appConfigService);
        ReflectionTestUtils.setField(service, "conventionJpaRepository", conventionJpaRepository);
        ReflectionTestUtils.setField(service, "structureJpaRepository", structureJpaRepository);
        ReflectionTestUtils.setField(service, "conventionDocumentEtudiantService", conventionDocumentEtudiantService);
        ReflectionTestUtils.setField(service, "conventionDocumentEtudiantHistoriqueJpaRepository", historiqueDocumentRepository);
        ReflectionTestUtils.setField(service, "evaluationTuteurTokenJpaRepository", evaluationTuteurTokenJpaRepository);
        ReflectionTestUtils.setField(service, "periodeInterruptionAvenantJpaRepository", periodeInterruptionAvenantJpaRepository);
        ReflectionTestUtils.setField(service, "etudiantGroupeEtudiantJpaRepository", etudiantGroupeEtudiantJpaRepository);
        ReflectionTestUtils.setField(service, "groupeEtudiantJpaRepository", groupeEtudiantJpaRepository);
        ReflectionTestUtils.setField(service, "historiqueStructureJpaRepository", historiqueStructureJpaRepository);
        ReflectionTestUtils.setField(service, "serviceJpaRepository", serviceJpaRepository);
        ReflectionTestUtils.setField(service, "contactJpaRepository", contactJpaRepository);
        ReflectionTestUtils.setField(service, "signatureService", signatureService);
        ReflectionTestUtils.setField(service, "appliProperties", appliProperties);
        ReflectionTestUtils.setField(service, "em", em);

        when(appliProperties.getDataDir()).thenReturn(tempDir.toString());
        when(signatureService.getSignatureFilePath(anyString()))
                .thenAnswer(inv -> tempDir.resolve("signatures").resolve(inv.getArgument(0) + "_signe.pdf").toString());

        // TransactionTemplate réel adossé à un gestionnaire mocké : les callbacks s'exécutent en ligne
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        ReflectionTestUtils.setField(service, "transactionTemplate", new TransactionTemplate(transactionManager));

        configGenerale = new ConfigGeneraleDto();
        when(appConfigService.getConfigGenerale()).thenReturn(configGenerale);

        // Requêtes EntityManager : comptes à zéro et deletes silencieux par défaut
        countQuery = mock(TypedQuery.class);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);

        deleteQuery = mock(Query.class);
        when(deleteQuery.setParameter(anyString(), any())).thenReturn(deleteQuery);
        when(deleteQuery.executeUpdate()).thenReturn(0);
        when(em.createQuery(anyString())).thenReturn(deleteQuery);

        // Comptes repositories à zéro par défaut
        when(conventionJpaRepository.countByStructure(anyInt())).thenReturn(0L);
        when(evaluationTuteurTokenJpaRepository.countByContactStructure(anyInt())).thenReturn(0L);
    }

    private static int anneeDe(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    // ------------------------------------------------------------------
    // archiver
    // ------------------------------------------------------------------

    @Test
    void lArchivageUtiliseUnSeuilDeCinqAnsParDefaut() {
        when(structureJpaRepository.desarchiverStructuresReutilisees()).thenReturn(0);
        when(structureJpaRepository.archiverStructuresSansConventionActive(any())).thenReturn(2);

        service.archiver();

        ArgumentCaptor<Date> seuilSansGratification = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> seuilAvecGratification = ArgumentCaptor.forClass(Date.class);
        verify(conventionJpaRepository).findIdsConventionsAArchiver(seuilSansGratification.capture(), seuilAvecGratification.capture());
        assertThat(anneeDe(seuilSansGratification.getValue())).isEqualTo(anneeDe(new Date()) - 5);
        assertThat(anneeDe(seuilAvecGratification.getValue())).isEqualTo(anneeDe(new Date()) - 5);
        verify(structureJpaRepository).desarchiverStructuresReutilisees();
        verify(structureJpaRepository).archiverStructuresSansConventionActive(any());
    }

    @Test
    void lesDureesDArchivageAvecEtSansGratificationSontDistinctes() {
        configGenerale.setDureeArchivageConventionAnnees(3);
        configGenerale.setDureeArchivageConventionGratifieeAnnees(7);

        service.archiver();

        ArgumentCaptor<Date> seuilSansGratification = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> seuilAvecGratification = ArgumentCaptor.forClass(Date.class);
        verify(conventionJpaRepository).findIdsConventionsAArchiver(seuilSansGratification.capture(), seuilAvecGratification.capture());
        assertThat(anneeDe(seuilSansGratification.getValue())).isEqualTo(anneeDe(new Date()) - 3);
        assertThat(anneeDe(seuilAvecGratification.getValue())).isEqualTo(anneeDe(new Date()) - 7);
    }

    @Test
    void uneDureeInvalideEstRameneeAUnAnMinimum() {
        configGenerale.setDureeArchivageConventionAnnees(0);
        configGenerale.setDureeArchivageConventionGratifieeAnnees(-2);

        service.archiver();

        ArgumentCaptor<Date> seuilSansGratification = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> seuilAvecGratification = ArgumentCaptor.forClass(Date.class);
        verify(conventionJpaRepository).findIdsConventionsAArchiver(seuilSansGratification.capture(), seuilAvecGratification.capture());
        assertThat(anneeDe(seuilSansGratification.getValue())).isEqualTo(anneeDe(new Date()) - 1);
        assertThat(anneeDe(seuilAvecGratification.getValue())).isEqualTo(anneeDe(new Date()) - 1);
    }

    @Test
    void lArchivageMarqueLaConventionDansLaMemeTransactionQueSesFichiers() throws IOException {
        Convention convention = new Convention();
        convention.setId(7);
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("DUPONT");
        etudiant.setPrenom("Marie");
        convention.setEtudiant(etudiant);
        when(conventionJpaRepository.findIdsConventionsAArchiver(any(), any())).thenReturn(List.of(7));
        when(conventionJpaRepository.findById(7)).thenReturn(convention);
        when(conventionDocumentEtudiantService.archiverFichiers(eq(convention), any(Path.class))).thenReturn(1);
        Files.createDirectories(tempDir.resolve("signatures"));
        Files.write(tempDir.resolve("signatures").resolve("Convention_7_DUPONT_Marie_signe.pdf"), "PDF".getBytes(StandardCharsets.UTF_8));

        service.archiver();

        // Fichiers déplacés ET convention marquée archivée dans le même traitement
        assertThat(tempDir.resolve("archives").resolve("Convention_7_DUPONT_Marie_signe.pdf")).exists();
        assertThat(convention.getDateArchivage()).isNotNull();
        assertThat(convention.getDateArchivageFichiers()).isNotNull();
        verify(conventionJpaRepository).save(convention);

        // La convention traitée alimente le rapport exportable en Excel
        byte[] excel = service.exportRapportExcel();
        assertThat(excel).isNotEmpty();
        // Signature ZIP d'un fichier xlsx (OOXML)
        assertThat(excel[0]).isEqualTo((byte) 'P');
        assertThat(excel[1]).isEqualTo((byte) 'K');
    }

    @Test
    void unEchecSurLesFichiersEmpecheLArchivageDeLaConvention() throws IOException {
        Convention convention = new Convention();
        convention.setId(8);
        when(conventionJpaRepository.findIdsConventionsAArchiver(any(), any())).thenReturn(List.of(8));
        when(conventionJpaRepository.findById(8)).thenReturn(convention);
        when(conventionDocumentEtudiantService.archiverFichiers(eq(convention), any(Path.class))).thenThrow(new IOException("disque en lecture seule"));

        service.archiver();

        // La convention reste active : elle n'est jamais considérée archivée si ses fichiers n'ont pas été traités
        assertThat(convention.getDateArchivage()).isNull();
        assertThat(convention.getDateArchivageFichiers()).isNull();
        verify(conventionJpaRepository, never()).save(convention);
    }

    @Test
    void lesEtatsIncoherentsSontReparesAvantLArchivage() {
        when(conventionJpaRepository.reactiverConventionsSansFichiersTraites()).thenReturn(3);

        String bilan = service.archiver();

        // Les conventions marquées archivées sans fichiers traités sont réactivées avant la
        // sélection : elles repassent par le circuit d'archivage atomique
        verify(conventionJpaRepository).reactiverConventionsSansFichiersTraites();
        assertThat(bilan).contains("3 convention(s) à l'état incohérent réactivée(s)");
    }

    // ------------------------------------------------------------------
    // purge des conventions
    // ------------------------------------------------------------------

    @Test
    void laPurgeSupprimeLaConventionEtSesDonneesLiees() {
        Convention convention = new Convention();
        convention.setId(7);
        convention.setDateArchivage(new Date());
        when(conventionJpaRepository.findIdsConventionsAPurger(any())).thenReturn(List.of(7));
        when(conventionJpaRepository.findById(7)).thenReturn(convention);
        when(etudiantGroupeEtudiantJpaRepository.findByConventionOrMergedConvention(7)).thenReturn(List.of());
        when(groupeEtudiantJpaRepository.findByConventionId(7)).thenReturn(List.of());

        int nb = service.purgerConventions(new Date());

        assertThat(nb).isEqualTo(1);
        verify(conventionDocumentEtudiantService).deleteAllForConvention(convention);
        verify(historiqueDocumentRepository).deleteByConventionId(7);
        verify(evaluationTuteurTokenJpaRepository).deleteByConventionId(7);
        verify(periodeInterruptionAvenantJpaRepository).deleteByConventionId(7);
        verify(conventionJpaRepository).delete(convention);
    }

    @Test
    void laPurgeSupprimeLesLiensDeGroupeEtLeGroupeSupport() {
        Convention convention = new Convention();
        convention.setId(8);
        EtudiantGroupeEtudiant lien = new EtudiantGroupeEtudiant();
        lien.setId(4);
        GroupeEtudiant groupe = new GroupeEtudiant();
        groupe.setId(2);
        when(conventionJpaRepository.findIdsConventionsAPurger(any())).thenReturn(List.of(8));
        when(conventionJpaRepository.findById(8)).thenReturn(convention);
        when(etudiantGroupeEtudiantJpaRepository.findByConventionOrMergedConvention(8)).thenReturn(List.of(lien));
        when(groupeEtudiantJpaRepository.findByConventionId(8)).thenReturn(List.of(groupe));

        int nb = service.purgerConventions(new Date());

        assertThat(nb).isEqualTo(1);
        verify(etudiantGroupeEtudiantJpaRepository).deleteAll(List.of(lien));
        // La suppression du groupe supprime la convention support par cascade : pas de delete direct
        verify(groupeEtudiantJpaRepository).deleteAll(List.of(groupe));
        verify(conventionJpaRepository, never()).delete(convention);
    }

    @Test
    void unEchecSurUneConventionNInterromptPasLaPurgeDesAutres() {
        Convention c1 = new Convention();
        c1.setId(1);
        Convention c2 = new Convention();
        c2.setId(2);
        when(conventionJpaRepository.findIdsConventionsAPurger(any())).thenReturn(List.of(1, 2));
        when(conventionJpaRepository.findById(1)).thenReturn(c1);
        when(conventionJpaRepository.findById(2)).thenReturn(c2);
        when(etudiantGroupeEtudiantJpaRepository.findByConventionOrMergedConvention(anyInt())).thenReturn(List.of());
        when(groupeEtudiantJpaRepository.findByConventionId(anyInt())).thenReturn(List.of());
        doThrow(new RuntimeException("disque plein")).when(conventionDocumentEtudiantService).deleteAllForConvention(c1);

        int nb = service.purgerConventions(new Date());

        assertThat(nb).isEqualTo(1);
        verify(conventionJpaRepository, never()).delete(c1);
        verify(conventionJpaRepository).delete(c2);
    }

    // ------------------------------------------------------------------
    // purge des structures
    // ------------------------------------------------------------------

    private Structure structureArchivee(int id) {
        Structure structure = new Structure();
        structure.setId(id);
        structure.setRaisonSociale("ACME");
        structure.setDateArchivage(new Date());
        when(structureJpaRepository.findIdsStructuresAPurger(any())).thenReturn(List.of(id));
        when(structureJpaRepository.findById(id)).thenReturn(structure);
        return structure;
    }

    @Test
    void uneStructureSansReferenceEstPurgeeAvecSesServicesEtContacts() {
        Structure structure = structureArchivee(5);
        when(historiqueStructureJpaRepository.findByStructure(structure)).thenReturn(List.of());

        int nb = service.purgerStructures(new Date());

        assertThat(nb).isEqualTo(1);
        verify(structureJpaRepository).delete(structure);
        verify(em, org.mockito.Mockito.times(2)).createQuery(anyString()); // delete contacts + delete services
    }

    @Test
    void uneStructureEncoreRattacheeAUneConventionNestPasPurgee() {
        Structure structure = structureArchivee(6);
        when(conventionJpaRepository.countByStructure(6)).thenReturn(2L);

        int nb = service.purgerStructures(new Date());

        assertThat(nb).isZero();
        verify(structureJpaRepository, never()).delete(structure);
    }

    @Test
    void uneStructureReferenceeParUneOffreNestPasPurgee() {
        Structure structure = structureArchivee(9);
        TypedQuery<Long> countOffres = mock(TypedQuery.class);
        when(countOffres.setParameter(anyString(), any())).thenReturn(countOffres);
        when(countOffres.getSingleResult()).thenReturn(1L);
        when(em.createQuery(contains("Offre"), eq(Long.class))).thenReturn(countOffres);

        int nb = service.purgerStructures(new Date());

        assertThat(nb).isZero();
        verify(structureJpaRepository, never()).delete(structure);
    }
}
