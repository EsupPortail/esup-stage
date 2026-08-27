package org.esup_portail.esup_stage.controller.admin;

import org.esup_portail.esup_stage.dto.FileContentDto;
import org.esup_portail.esup_stage.dto.FileElementDto;
import org.esup_portail.esup_stage.dto.LoggerLevelDto;
import org.esup_portail.esup_stage.dto.LoggerUpdateRequest;
import org.esup_portail.esup_stage.service.AdminService;
import org.esup_portail.esup_stage.service.logs.ExportService;
import org.esup_portail.esup_stage.service.logs.LogFileService;
import org.esup_portail.esup_stage.service.logs.LogReaderService;
import org.esup_portail.esup_stage.service.logs.LogTailerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLogsControllerTest {

    private AdminLogsController controller;
    private LogTailerService logTailerService;
    private LoggingSystem loggingSystem;
    private LogFileService logFileService;
    private LogReaderService logReaderService;
    private ExportService exportService;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        controller = new AdminLogsController();
        logTailerService = mock(LogTailerService.class);
        loggingSystem = mock(LoggingSystem.class);
        logFileService = mock(LogFileService.class);
        logReaderService = mock(LogReaderService.class);
        exportService = mock(ExportService.class);
        adminService = mock(AdminService.class);
        ReflectionTestUtils.setField(controller, "logTailerService", logTailerService);
        ReflectionTestUtils.setField(controller, "loggingSystem", loggingSystem);
        ReflectionTestUtils.setField(controller, "logFileService", logFileService);
        ReflectionTestUtils.setField(controller, "logReaderService", logReaderService);
        ReflectionTestUtils.setField(controller, "exportService", exportService);
        ReflectionTestUtils.setField(controller, "adminService", adminService);
    }

    @Test
    void leStreamDemarreLeSuiviDesLogs() {
        when(logTailerService.startStreaming(any(), anyInt())).thenReturn(UUID.randomUUID());

        assertThat(controller.stream()).isNotNull();
        verify(logTailerService).startStreaming(any(), anyInt());
        verify(adminService).requireAdmin();
    }

    @Test
    void lesLoggersSontListesAvecLeursNiveaux() {
        when(loggingSystem.getLoggerConfigurations()).thenReturn(List.of(
                new LoggerConfiguration("org.esup", LogLevel.DEBUG, LogLevel.DEBUG),
                new LoggerConfiguration("root", null, LogLevel.INFO)
        ));

        ResponseEntity<List<LoggerLevelDto>> reponse = controller.getLoggers();

        assertThat(reponse.getBody()).hasSize(2);
    }

    @Test
    void laMiseAJourDesLoggersValideSesEntrees() {
        LoggerUpdateRequest sansPackages = new LoggerUpdateRequest();
        sansPackages.setLevel("INFO");
        assertThat(controller.updateLoggers(sansPackages).getStatusCode().value()).isEqualTo(400);

        LoggerUpdateRequest niveauInvalide = new LoggerUpdateRequest();
        niveauInvalide.setPackageNames(List.of("org.esup"));
        niveauInvalide.setLevel("FARFELU");
        assertThat(controller.updateLoggers(niveauInvalide).getStatusCode().value()).isEqualTo(400);

        LoggerUpdateRequest valide = new LoggerUpdateRequest();
        valide.setPackageNames(List.of("org.esup", "org.spring"));
        valide.setLevel("debug");
        assertThat(controller.updateLoggers(valide).getStatusCode().value()).isEqualTo(204);
        verify(loggingSystem).setLogLevel("org.esup", LogLevel.DEBUG);
        verify(loggingSystem).setLogLevel("org.spring", LogLevel.DEBUG);
    }

    @Test
    void lExplorateurDeFichiersDelegueAuxServices() throws Exception {
        when(logFileService.listFolder("logs")).thenReturn(List.of(mock(FileElementDto.class)));
        assertThat(controller.listFolder("logs").getBody()).hasSize(1);

        FileContentDto contenu = mock(FileContentDto.class);
        when(logReaderService.readPage("logs/app.log", 0, 500)).thenReturn(contenu);
        assertThat(controller.getContent("logs/app.log", 0, 500).getBody()).isSameAs(contenu);
    }

    @Test
    void lesExportsPosentLesEntetesDeTelechargement() throws Exception {
        when(exportService.exportSingle("logs/app.log")).thenReturn(new ByteArrayResource("contenu".getBytes()));
        var single = controller.exportSingle("logs/app.log");
        assertThat(single.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("app.log");

        when(exportService.exportLogAsCsv("logs/app.log")).thenReturn("a;b".getBytes());
        var csv = controller.exportAsCsv("logs/app.log");
        assertThat(csv.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("app.csv");
        assertThat(csv.getBody()).isEqualTo("a;b".getBytes());

        when(logFileService.getResource("logs/app.log")).thenReturn(new ByteArrayResource("contenu".getBytes()));
        var download = controller.downloadFile("logs/app.log");
        assertThat(download.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("app.log");
    }
}
