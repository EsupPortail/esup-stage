package org.esup_portail.esup_stage.service.signature;

import org.esup_portail.esup_stage.docaposte.DocaposteClient;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "docaposte", name = "uri")
public class DocaposteSignatureClient implements SignatureClient {
    private final DocaposteClient docaposteClient;

    public DocaposteSignatureClient(DocaposteClient docaposteClient) {
        this.docaposteClient = docaposteClient;
    }

    @Override
    public void upload(Convention convention, Avenant avenant) {
        docaposteClient.upload(convention, avenant);
    }

    @Override
    public List<Historique> getHistorique(String documentId, List<CentreGestionSignataire> signataires) {
        return docaposteClient.getHistorique(documentId, signataires);
    }

    @Override
    public InputStream download(String documentId) {
        return docaposteClient.download(documentId);
    }
}

