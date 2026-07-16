package org.esup_portail.esup_stage.service.session;

import org.esup_portail.esup_stage.dto.ActiveSessionDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ActiveSessionService {

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private SessionSseService sessionSseService;

    public List<ActiveSessionDto> listActiveSessions(String currentSessionId) {
        List<ActiveSessionDto> sessions = new ArrayList<>();
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (!(principal instanceof CasUserDetailsImpl userDetails)) {
                continue;
            }
            Utilisateur utilisateur = userDetails.getUtilisateur();
            for (SessionInformation sessionInformation : sessionRegistry.getAllSessions(principal, false)) {
                List<String> roles = utilisateur.getRoles() == null
                        ? List.of()
                        : utilisateur.getRoles().stream()
                                .map(role -> role.getLibelle() != null ? role.getLibelle() : role.getCode())
                                .toList();
                sessions.add(new ActiveSessionDto(
                        sessionInformation.getSessionId(),
                        utilisateur.getLogin(),
                        utilisateur.getNom(),
                        utilisateur.getPrenom(),
                        roles,
                        sessionInformation.getLastRequest(),
                        sessionInformation.getSessionId().equals(currentSessionId)
                ));
            }
        }
        sessions.sort(Comparator.comparing(ActiveSessionDto::getLogin, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(ActiveSessionDto::getLastRequest, Comparator.nullsLast(Comparator.reverseOrder())));
        return sessions;
    }

    public void closeSession(String sessionId, String currentSessionId) {
        if (sessionId.equals(currentSessionId)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas fermer votre propre session");
        }
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionId);
        if (sessionInformation == null || sessionInformation.isExpired()) {
            throw new AppException(HttpStatus.NOT_FOUND, "Session non trouvée");
        }
        sessionInformation.expireNow();
        sessionSseService.sendForceLogout(sessionId, null);
    }

    /**
     * Ferme toutes les sessions actives sauf celle de l'administrateur courant.
     * Le message optionnel est affiché aux utilisateurs déconnectés.
     *
     * @return le nombre de sessions fermées
     */
    public int closeAllSessions(String currentSessionId, String message) {
        int closed = 0;
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            for (SessionInformation sessionInformation : sessionRegistry.getAllSessions(principal, false)) {
                if (sessionInformation.getSessionId().equals(currentSessionId)) {
                    continue;
                }
                sessionInformation.expireNow();
                sessionSseService.sendForceLogout(sessionInformation.getSessionId(), message);
                closed++;
            }
        }
        return closed;
    }
}
