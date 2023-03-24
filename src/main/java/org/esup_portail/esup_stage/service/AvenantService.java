package org.esup_portail.esup_stage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.docaposte.DocaposteClient;
import org.esup_portail.esup_stage.docaposte.gen.HistoryEntryVO;
import org.esup_portail.esup_stage.docaposte.gen.HistoryResponse;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AvenantService {
    private static final Logger logger = LogManager.getLogger(AvenantService.class);
    @Autowired
    DocaposteClient docaposteClient;
    @Autowired
    AvenantJpaRepository avenantJpaRepository;

    public void updateSignatureElectroniqueHistorique(Avenant avenant) {
        HistoryResponse response = docaposteClient.getHistorique(avenant.getDocumentId());
        int indexOtp = -1;
        try {
            List<String> profils = new ObjectMapper().readValue(avenant.getConvention().getCentreGestion().getOrdreSignature(), List.class);
            for (HistoryEntryVO history : response.getReturn()) {
                Boolean isSignarure = null;
                if (history.getStateName().equals("Informations OTP définies")) {
                    indexOtp++;
                    isSignarure = false;
                }
                if (history.getStateName().equals("Signé")) {
                    isSignarure = true;
                }
                if (indexOtp >= 0) {
                    String profil = profils.get(indexOtp);
                    Date date = history.getDate().toGregorianCalendar().getTime();
                    switch (profil) {
                        case "etudiant":
                            if (isSignarure != null) {
                                if (isSignarure) avenant.setDateSignatureEtudiant(date);
                                else avenant.setDateDepotEtudiant(date);
                            }
                            break;
                        case "enseignant":
                            if (isSignarure != null) {
                                if (isSignarure) avenant.setDateSignatureEnseignant(date);
                                else avenant.setDateDepotEnseignant(date);
                            }
                            break;
                        case "tuteur":
                            if (isSignarure != null) {
                                if (isSignarure) avenant.setDateSignatureTuteur(date);
                                else avenant.setDateDepotTuteur(date);
                            }
                            break;
                        case "signataire":
                            if (isSignarure != null) {
                                if (isSignarure) avenant.setDateSignatureSignataire(date);
                                else avenant.setDateDepotSignataire(date);
                            }
                            break;
                        case "viseur":
                            if (isSignarure != null) {
                                if (isSignarure) avenant.setDateSignatureViseur(date);
                                else avenant.setDateDepotViseur(date);
                            }
                            break;
                    }
                }
            }
            avenantJpaRepository.save(avenant);
        } catch (Exception e) {
            logger.error(e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur de la récupération de l'historique");
        }
    }
}
