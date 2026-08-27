package org.esup_portail.esup_stage.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsTest {

    @Test
    void appExceptionExposeStatutEtMessage() {
        AppException exception = new AppException(HttpStatus.BAD_REQUEST, "donnée invalide");

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("donnée invalide");
    }

    @Test
    void appExceptionNeRemplitPasLaStackTrace() {
        AppException exception = new AppException(HttpStatus.NOT_FOUND, "introuvable");

        assertThat(exception.fillInStackTrace()).isSameAs(exception);
        assertThat(exception.getStackTrace()).isEmpty();
    }

    @Test
    void interruptionCompleteInitialiseTousLesChamps() {
        ApplicationClientInterruption interruption =
                new ApplicationClientInterruption(500, "message client", "message interne");

        assertThat(interruption.getCodeHttp()).isEqualTo(500);
        assertThat(interruption.getClientMessage()).isEqualTo("message client");
        assertThat(interruption.getInternMessage()).isEqualTo("message interne");
        assertThat(interruption.getDateInterruption()).isBeforeOrEqualTo(new Date());
        assertThat(interruption.getDateInterrupt()).isNotBlank();
        assertThat(interruption.toString())
                .contains("http=500")
                .contains("message client")
                .contains("message interne");
    }

    @Test
    void interruptionAvecMessageClientSeulUtiliseLesValeursParDefaut() {
        ApplicationClientInterruption interruption = new ApplicationClientInterruption(404, "pas trouvé");

        assertThat(interruption.getCodeHttp()).isEqualTo(404);
        assertThat(interruption.getClientMessage()).isEqualTo("pas trouvé");
        assertThat(interruption.getInternMessage()).isEqualTo("N/A");
    }

    @Test
    void interruptionAvecCodeSeulUtiliseLesMessagesParDefaut() {
        ApplicationClientInterruption interruption = new ApplicationClientInterruption(503);

        assertThat(interruption.getCodeHttp()).isEqualTo(503);
        assertThat(interruption.getClientMessage()).isEqualTo("interruption du service");
        assertThat(interruption.getInternMessage()).isEqualTo("erreur inconnue");
    }

    @Test
    void interruptionVideEstNeutre() {
        ApplicationClientInterruption interruption = new ApplicationClientInterruption();

        assertThat(interruption.getCodeHttp()).isZero();
        assertThat(interruption.getClientMessage()).isNull();
        assertThat(interruption.getInternMessage()).isNull();
        assertThat(interruption.getDateInterruption()).isNull();
        assertThat(interruption.getDateInterrupt()).isNull();
    }

    @Test
    void interruptionAccepteLaModificationDesChamps() {
        ApplicationClientInterruption interruption = new ApplicationClientInterruption();
        Date date = new Date(1700000000000L);

        interruption.setCodeHttp(418);
        interruption.setClientMessage("client");
        interruption.setInternMessage("interne");
        interruption.setDateInterruption(date);
        interruption.setDateInterrupt("2026-01-01 00:00:00");

        assertThat(interruption.getCodeHttp()).isEqualTo(418);
        assertThat(interruption.getClientMessage()).isEqualTo("client");
        assertThat(interruption.getInternMessage()).isEqualTo("interne");
        assertThat(interruption.getDateInterruption()).isEqualTo(date);
        assertThat(interruption.getDateInterrupt()).isEqualTo("2026-01-01 00:00:00");
    }

    @Test
    void clientExceptionPorteSonInterruption() {
        ApplicationClientInterruption interruption = new ApplicationClientInterruption(400, "erreur métier");
        ApplicationClientException exception = new ApplicationClientException(interruption);

        assertThat(exception.getMessage()).isEqualTo("erreur métier");
        assertThat(exception.getApplicationInterruption()).isSameAs(interruption);

        ApplicationClientInterruption autre = new ApplicationClientInterruption(500);
        exception.setApplicationInterruption(autre);
        assertThat(exception.getApplicationInterruption()).isSameAs(autre);
    }
}
