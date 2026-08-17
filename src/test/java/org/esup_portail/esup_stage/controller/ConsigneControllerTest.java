package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.ConsigneFormDto;
import org.esup_portail.esup_stage.enums.FolderEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Consigne;
import org.esup_portail.esup_stage.model.ConsigneDocument;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.ConsigneDocumentJpaRepository;
import org.esup_portail.esup_stage.repository.ConsigneJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsigneControllerTest {

    @TempDir
    Path dataDir;

    private ConsigneController controller;
    private ConsigneJpaRepository consigneJpaRepository;
    private ConsigneDocumentJpaRepository consigneDocumentJpaRepository;
    private CentreGestionJpaRepository centreGestionJpaRepository;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ConsigneController();
        consigneJpaRepository = mock(ConsigneJpaRepository.class);
        consigneDocumentJpaRepository = mock(ConsigneDocumentJpaRepository.class);
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setDataDir(dataDir.toString());
        controller.appliProperties = appliProperties;
        controller.consigneJpaRepository = consigneJpaRepository;
        controller.consigneDocumentJpaRepository = consigneDocumentJpaRepository;
        ReflectionTestUtils.setField(controller, "centreGestionJpaRepository", centreGestionJpaRepository);

        Files.createDirectories(Path.of(dataDir.toString() + FolderEnum.CENTRE_GESTION_CONSIGNE_DOCS));

        when(consigneJpaRepository.saveAndFlush(any(Consigne.class))).thenAnswer(inv -> inv.getArgument(0));
        when(consigneDocumentJpaRepository.save(any(ConsigneDocument.class))).thenAnswer(inv -> {
            ConsigneDocument document = inv.getArgument(0);
            document.setId(5);
            return document;
        });
    }

    @Test
    void getByCentreGestionDelegueAuRepository() {
        Consigne consigne = new Consigne();
        when(consigneJpaRepository.findByIdCentreGestion(3)).thenReturn(consigne);

        assertThat(controller.getByCentreGestion(3)).isSameAs(consigne);
    }

    @Test
    void createConvertitLesCouleursHsl() {
        when(centreGestionJpaRepository.findById(3)).thenReturn(new CentreGestion());
        ConsigneFormDto dto = new ConsigneFormDto();
        dto.setIdCentreGestion(3);
        dto.setTexte("<span style=\"color:hsl(0, 100%, 50%)\">Consigne</span>");

        Consigne consigne = controller.create(dto);

        assertThat(consigne.getTexte()).contains("color:rgb(255, 0, 0)");
    }

    @Test
    void updateModifieLeTexte() {
        Consigne consigne = new Consigne();
        when(consigneJpaRepository.findById(7)).thenReturn(consigne);
        ConsigneFormDto dto = new ConsigneFormDto();
        dto.setTexte("Nouvelle consigne");

        assertThat(controller.update(7, dto).getTexte()).isEqualTo("Nouvelle consigne");

        when(consigneJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.update(99, dto)).isInstanceOf(AppException.class);
    }

    @Test
    void addDocumentAccepteUniquementPdfEtWord() {
        Consigne consigne = new Consigne();
        when(consigneJpaRepository.findById(7)).thenReturn(consigne);

        MockMultipartFile image = new MockMultipartFile("doc", "image.png", "image/png", "x".getBytes());
        assertThatThrownBy(() -> controller.addDocument(7, image))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        MockMultipartFile pdf = new MockMultipartFile("doc", "notice.pdf", "application/pdf", "%PDF".getBytes());
        Consigne resultat = controller.addDocument(7, pdf);

        assertThat(resultat.getDocuments()).hasSize(1);
        assertThat(resultat.getDocuments().get(0).getNomReel()).isEqualTo("notice.pdf");

        when(consigneJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.addDocument(99, pdf)).isInstanceOf(AppException.class);
    }

    @Test
    void lesDocumentsSontTelechargeablesApresUpload() {
        Consigne consigne = new Consigne();
        when(consigneJpaRepository.findById(7)).thenReturn(consigne);
        MockMultipartFile pdf = new MockMultipartFile("doc", "notice.pdf", "application/pdf", "%PDF-contenu".getBytes());
        controller.addDocument(7, pdf);
        ConsigneDocument document = consigne.getDocuments().get(0);
        when(consigneDocumentJpaRepository.findById(5)).thenReturn(document);

        var reponse = controller.downloadDoc(7, 5);

        assertThat(reponse.getBody()).isEqualTo("%PDF-contenu".getBytes());

        when(consigneDocumentJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.downloadDoc(7, 99)).isInstanceOf(AppException.class);
    }

    @Test
    void deleteDocRetireLeDocumentEtSonFichier() {
        Consigne consigne = new Consigne();
        when(consigneJpaRepository.findById(7)).thenReturn(consigne);
        MockMultipartFile pdf = new MockMultipartFile("doc", "notice.pdf", "application/pdf", "%PDF".getBytes());
        controller.addDocument(7, pdf);
        ConsigneDocument document = consigne.getDocuments().get(0);
        when(consigneDocumentJpaRepository.findById(5)).thenReturn(document);

        Consigne resultat = controller.deleteDoc(7, 5);

        assertThat(resultat.getDocuments()).isEmpty();

        when(consigneDocumentJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.deleteDoc(7, 99)).isInstanceOf(AppException.class);
    }

    @Test
    void deleteConsigneDetacheLeCentreEtSupprimeLesFichiers() {
        Consigne consigne = new Consigne();
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setConsigne(consigne);
        consigne.setCentreGestion(centreGestion);
        when(consigneJpaRepository.findById(7)).thenReturn(consigne);
        when(centreGestionJpaRepository.saveAndFlush(any(CentreGestion.class))).thenAnswer(inv -> inv.getArgument(0));

        controller.deleteConsigne(7);

        assertThat(centreGestion.getConsigne()).isNull();

        when(consigneJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.deleteConsigne(99)).isInstanceOf(AppException.class);

        Consigne sansCentre = new Consigne();
        when(consigneJpaRepository.findById(8)).thenReturn(sansCentre);
        assertThatThrownBy(() -> controller.deleteConsigne(8)).isInstanceOf(AppException.class);
    }
}
