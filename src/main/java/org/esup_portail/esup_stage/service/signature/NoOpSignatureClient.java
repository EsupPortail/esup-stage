package org.esup_portail.esup_stage.service.signature;

import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "docaposte", name = "uri", matchIfMissing = true, havingValue = "false")
public class NoOpSignatureClient implements SignatureClient {
    @Override
    public void upload(Convention convention, Avenant avenant) {
    }

    @Override
    public List<Historique> getHistorique(String documentId, List<CentreGestionSignataire> signataires) {
        return new ArrayList<>();
    }

    @Override
    public InputStream download(String documentId) {
        return null;
    }
}