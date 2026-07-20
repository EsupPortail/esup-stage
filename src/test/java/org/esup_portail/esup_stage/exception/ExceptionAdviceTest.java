package org.esup_portail.esup_stage.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du gestionnaire global d'exceptions REST : on invoque directement
 * chaque handler et on inspecte le {@link ResponseEntity} produit (statut + corps).
 * Le point sensible est la détection des déconnexions client, qui doivent renvoyer un
 * 204 silencieux plutôt qu'une 500 bruyante.
 */
class ExceptionAdviceTest {

    private final ExceptionAdvice advice = new ExceptionAdvice();

    @SuppressWarnings("unchecked")
    private Map<String, Object> body(ResponseEntity<Object> response) {
        return (Map<String, Object>) response.getBody();
    }

    @Test
    void handleAppExceptionRenvoieLeStatutEtLeMessagePortesParLException() {
        AppException e = new AppException(HttpStatus.NOT_FOUND, "convention introuvable");

        ResponseEntity<Object> response = advice.handleAppException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(body(response))
                .containsEntry("code", HttpStatus.NOT_FOUND)
                .containsEntry("message", "convention introuvable");
    }

    @Test
    void handleAsyncRequestNotUsableExceptionRenvoieUn204SansCorps() {
        ResponseEntity<Object> response =
                advice.handleAsyncRequestNotUsableException(new AsyncRequestNotUsableException("client parti"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void handleIOExceptionSurErreurReelleRenvoieUne500Detaillee() {
        ResponseEntity<Object> response = advice.handleIOException(new IOException("disque plein"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(body(response))
                .containsEntry("code", HttpStatus.INTERNAL_SERVER_ERROR)
                .containsEntry("message", "disque plein");
    }

    @Test
    void handleIOExceptionSurDeconnexionClientRenvoieUn204Silencieux() {
        ResponseEntity<Object> response =
                advice.handleIOException(new IOException("Broken pipe"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void handleIOExceptionDetecteLaDeconnexionMemeAccentueeEtDansUneCause() {
        // message français accentué, porté par une cause imbriquée
        IOException racine = new IOException(
                "Une connexion établie a été abandonnée par un logiciel de votre ordinateur hôte");
        IOException e = new IOException("échec d'écriture", racine);

        ResponseEntity<Object> response = advice.handleIOException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void handleIOExceptionDetecteLaDeconnexionParLeNomDeClasse() {
        // une exception dont le nom de classe contient "clientabort" est traitée comme déconnexion
        class ClientAbortException extends IOException {
            ClientAbortException() { super("peu importe le message"); }
        }

        ResponseEntity<Object> response = advice.handleIOException(new ClientAbortException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
