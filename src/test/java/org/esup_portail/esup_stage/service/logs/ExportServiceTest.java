package org.esup_portail.esup_stage.service.logs;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.AdminService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de l'export CSV des logs et du contrôle d'accès administrateur.
 */
class ExportServiceTest {

    @TempDir
    Path racine;

    private ExportService exportService;

    @BeforeEach
    void setUp() {
        LogFileService logFileService = new LogFileService();
        ReflectionTestUtils.setField(logFileService, "rootPath", racine.toString());
        exportService = new ExportService();
        ReflectionTestUtils.setField(exportService, "fileService", logFileService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void exportSingleRenvoieLaRessource() throws IOException {
        Files.writeString(racine.resolve("app.log"), "contenu");

        assertThat(exportService.exportSingle("app.log").exists()).isTrue();
    }

    @Test
    void lExportCsvParseLesLignesDeLog() throws IOException {
        Files.writeString(racine.resolve("app.log"), String.join("\n",
                "2024-01-15 10:23:45.123 INFO [main] Démarrage de l'application",
                "2024-01-15 10:23:46.000 ERROR [worker-1] Échec avec \"guillemets\"",
                "ligne brute sans format"), StandardCharsets.UTF_8);

        byte[] csv = exportService.exportLogAsCsv("app.log");
        String[] lignes = new String(csv, StandardCharsets.UTF_8).split("\\R");

        assertThat(lignes[0]).isEqualTo("line_number,timestamp,level,thread,message");
        assertThat(lignes[1]).contains("2024-01-15 10:23:45.123")
                .contains("INFO").contains("main").contains("Démarrage de l'application");
        assertThat(lignes[2]).contains("ERROR").contains("worker-1").contains("\"\"guillemets\"\"");
        // ligne non parsable : tout dans la colonne message
        assertThat(lignes[3]).startsWith("3,,,,").contains("ligne brute sans format");
    }

    @Test
    void adminServiceExigeUnAdministrateur() {
        AdminService adminService = new AdminService();

        assertThatThrownBy(adminService::requireAdmin).isInstanceOf(AppException.class);

        Utilisateur etudiant = new Utilisateur();
        Role roleEtu = new Role();
        roleEtu.setCode(Role.ETU);
        etudiant.setRoles(List.of(roleEtu));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CasUserDetailsImpl(etudiant, List.of()), null));
        assertThatThrownBy(adminService::requireAdmin).isInstanceOf(AppException.class);

        Utilisateur admin = new Utilisateur();
        Role roleAdm = new Role();
        roleAdm.setCode(Role.ADM);
        admin.setRoles(List.of(roleAdm));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CasUserDetailsImpl(admin, List.of()), null));
        assertThatCode(adminService::requireAdmin).doesNotThrowAnyException();
    }
}
