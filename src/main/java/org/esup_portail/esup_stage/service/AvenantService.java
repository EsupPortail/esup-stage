package org.esup_portail.esup_stage.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.docaposte.DocaposteClient;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvenantService {
    private static final Logger logger = LogManager.getLogger(AvenantService.class);
    @Autowired
    DocaposteClient docaposteClient;
    @Autowired
    AvenantJpaRepository avenantJpaRepository;

    public void updateSignatureElectroniqueHistorique(Avenant avenant) {
        try {
            List<Historique> historiques = docaposteClient.getHistorique(avenant.getDocumentId(), avenant.getConvention().getCentreGestion().getSignataires());
            for (Historique historique : historiques) {
                switch (historique.getTypeSignataire()) {
                    case etudiant:
                        avenant.setDateSignatureEtudiant(historique.getDateSignature());
                        avenant.setDateDepotEtudiant(historique.getDateDepot());
                        break;
                    case enseignant:
                        avenant.setDateSignatureEnseignant(historique.getDateSignature());
                        avenant.setDateDepotEnseignant(historique.getDateDepot());
                        break;
                    case tuteur:
                        avenant.setDateSignatureTuteur(historique.getDateSignature());
                        avenant.setDateDepotTuteur(historique.getDateDepot());
                        break;
                    case signataire:
                        avenant.setDateSignatureSignataire(historique.getDateSignature());
                        avenant.setDateDepotSignataire(historique.getDateDepot());
                        break;
                    case viseur:
                        avenant.setDateSignatureViseur(historique.getDateSignature());
                        avenant.setDateDepotViseur(historique.getDateDepot());
                        break;
                    default:
                        break;
                }
            }
            avenantJpaRepository.save(avenant);
        } catch (Exception e) {
            logger.error(e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur de la récupération de l'historique");
        }
    }
}
