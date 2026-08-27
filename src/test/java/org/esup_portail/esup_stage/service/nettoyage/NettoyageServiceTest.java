package org.esup_portail.esup_stage.service.nettoyage;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.ArchivageProgressionDto;
import org.esup_portail.esup_stage.repository.ContactJpaRepository;
import org.esup_portail.esup_stage.repository.EvaluationTuteurTokenJpaRepository;
import org.esup_portail.esup_stage.repository.ServiceJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NettoyageServiceTest {

    private NettoyageService service;
    private ContactJpaRepository contactJpaRepository;
    private ServiceJpaRepository serviceJpaRepository;
    private EvaluationTuteurTokenJpaRepository evaluationTuteurTokenJpaRepository;

    @BeforeEach
    void setUp() {
        service = new NettoyageService();
        contactJpaRepository = mock(ContactJpaRepository.class);
        serviceJpaRepository = mock(ServiceJpaRepository.class);
        evaluationTuteurTokenJpaRepository = mock(EvaluationTuteurTokenJpaRepository.class);
        AppliProperties appliProperties = mock(AppliProperties.class);
        ReflectionTestUtils.setField(service, "contactJpaRepository", contactJpaRepository);
        ReflectionTestUtils.setField(service, "serviceJpaRepository", serviceJpaRepository);
        ReflectionTestUtils.setField(service, "evaluationTuteurTokenJpaRepository", evaluationTuteurTokenJpaRepository);
        ReflectionTestUtils.setField(service, "appliProperties", appliProperties);

        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        ReflectionTestUtils.setField(service, "transactionTemplate", new TransactionTemplate(transactionManager));
    }

    private void attendreFin() throws InterruptedException {
        BooleanSupplier fini = () -> !service.getProgression().isEnCours() && service.getProgression().getDateFin() != null;
        long fin = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < fin && !fini.getAsBoolean()) {
            Thread.sleep(20);
        }
        assertThat(fini.getAsBoolean()).isTrue();
    }

    @Test
    void leNettoyageManuelDesContactsSupprimeEtAlimenteLeRapportExportable() throws InterruptedException {
        // Projection : [id, nom, prenom, mail, tel, fonction, service, structure, loginCreation, dateCreation]
        List<Object[]> lignes = List.<Object[]>of(
                new Object[]{1, "DUPONT", "Marie", "m@x.fr", "0600", "Tuteur", "Service A", "ACME", "etu1", new Date()},
                new Object[]{2, "MARTIN", "Paul", "p@x.fr", "0601", "Tuteur", "Service B", "ACME", "etu2", new Date()}
        );
        when(contactJpaRepository.findInutilisesPourNettoyage(any(Date.class))).thenReturn(lignes);
        when(contactJpaRepository.deleteByIdIn(anyList())).thenReturn(2);

        service.demarrerNettoyageManuel("contacts");
        attendreFin();

        ArchivageProgressionDto progression = service.getProgression();
        assertThat(progression.isErreur()).isFalse();
        assertThat(progression.getMessage()).contains("2 contact(s) supprimé(s)");
        assertThat(progression.isRapportDisponible()).isTrue();
        assertThat(progression.getRapportNbLignes()).isEqualTo(2);
        // Les tokens expirés sont supprimés avant les contacts
        verify(evaluationTuteurTokenJpaRepository).deleteByContactIdIn(anyList());
        verify(contactJpaRepository).deleteByIdIn(anyList());

        // Rapport exportable en xlsx (signature ZIP OOXML)
        byte[] excel = service.exportRapportExcel();
        assertThat(excel).isNotEmpty();
        assertThat(excel[0]).isEqualTo((byte) 'P');
        assertThat(excel[1]).isEqualTo((byte) 'K');
    }

    @Test
    void leNettoyageManuelDesServicesSupprimeLesServicesInutilises() throws InterruptedException {
        List<Object[]> lignes = List.<Object[]>of(
                new Object[]{10, "Service X", "1 rue A", "75001", "Paris", "ACME", "etu1", new Date()}
        );
        when(serviceJpaRepository.findInutilisesPourNettoyage()).thenReturn(lignes);
        when(serviceJpaRepository.deleteByIdIn(anyList())).thenReturn(1);

        service.demarrerNettoyageManuel("services");
        attendreFin();

        ArchivageProgressionDto progression = service.getProgression();
        assertThat(progression.getMessage()).contains("1 service(s) supprimé(s)");
        assertThat(progression.getRapportNbLignes()).isEqualTo(1);
        verify(serviceJpaRepository).deleteByIdIn(anyList());
    }
}
