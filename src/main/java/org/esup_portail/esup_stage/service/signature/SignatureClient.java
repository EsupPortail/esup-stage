package org.esup_portail.esup_stage.service.signature;

import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.service.signature.model.Historique;

import java.io.InputStream;
import java.util.List;

public interface SignatureClient {
    void upload(Convention convention, Avenant avenant);
    List<Historique> getHistorique(String documentId, List<CentreGestionSignataire> signataires);
    InputStream download(String documentId);
}
