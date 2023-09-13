package org.esup_portail.esup_stage.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.docaposte.DocaposteClient;
import org.esup_portail.esup_stage.docaposte.gen.HistoryEntryVO;
import org.esup_portail.esup_stage.docaposte.gen.HistoryResponse;
import org.esup_portail.esup_stage.enums.TypeSignatureEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvenantService {
    private static final Logger logger = LogManager.getLogger(AvenantService.class);
    @Autowired
    DocaposteClient docaposteClient;
    @Autowired
    AvenantJpaRepository avenantJpaRepository;

    public void updateSignatureElectroniqueHistorique(Avenant avenant) {
        HistoryResponse response = docaposteClient.getHistorique(avenant.getDocumentId());
        try {
            List<CentreGestionSignataire> profils = avenant.getConvention().getCentreGestion().getSignataires();
            List<CentreGestionSignataire> profilsOtp = profils.stream().filter(p -> p.getType() == TypeSignatureEnum.otp).collect(Collectors.toList());
            List<CentreGestionSignataire> profilsServeur = profils.stream().filter(p -> p.getType() == TypeSignatureEnum.serveur).collect(Collectors.toList());
            int ordreOtp = 0;
            int ordreServeur = 0;
            TypeSignatureEnum typeSignature = null;
            for (HistoryEntryVO history : response.getReturn()) {
                boolean signature = false;
                boolean aTraiter = false;
                Date date = history.getDate().toGregorianCalendar().getTime();
                switch (history.getStateName()) {
                    case "Informations OTP définies":
                        ordreOtp++;
                        typeSignature = TypeSignatureEnum.otp;
                        aTraiter = true;
                        break;
                    case "Envoyé pour signature":
                        ordreServeur++;
                        typeSignature = TypeSignatureEnum.serveur;
                        aTraiter = true;
                        break;
                    case "Signé":
                        signature = true;
                        aTraiter = true;
                        break;
                }
                if (aTraiter && typeSignature != null && (ordreOtp > 0 || ordreServeur > 0)) {
                    switch (typeSignature) {
                        case otp:
                            updateDateSignature(avenant, profilsOtp.get(ordreOtp - 1), date, signature);
                            break;
                        case serveur:
                            updateDateSignature(avenant, profilsServeur.get(ordreServeur - 1), date, signature);
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

    private void updateDateSignature(Avenant avenant, CentreGestionSignataire profil, Date date, boolean signature) {
        switch (profil.getId().getSignataire()) {
            case etudiant:
                if (signature) avenant.setDateSignatureEtudiant(date);
                else avenant.setDateDepotEtudiant(date);
                break;
            case enseignant:
                if (signature) avenant.setDateSignatureEnseignant(date);
                else avenant.setDateDepotEnseignant(date);
                break;
            case tuteur:
                if (signature) avenant.setDateSignatureTuteur(date);
                else avenant.setDateDepotTuteur(date);
                break;
            case signataire:
                if (signature) avenant.setDateSignatureSignataire(date);
                else avenant.setDateDepotSignataire(date);
                break;
            case viseur:
                if (signature) avenant.setDateSignatureViseur(date);
                else avenant.setDateDepotViseur(date);
                break;
            default:
                break;
        }
    }
}
