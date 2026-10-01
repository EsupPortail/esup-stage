package org.esup_portail.esup_stage.service.impression;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.enums.SignataireEnum;
import org.esup_portail.esup_stage.enums.TypeSignatureEnum;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.CentreGestionSignataire;
import org.esup_portail.esup_stage.model.Convention;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Vérifie que l'OTP du viseur est généré avec l'adresse configurée pour le viseur
 * (mailDelegataireViseur si renseigné, sinon mailViseur) et non l'adresse générale
 * du centre de gestion.
 */
class ImpressionServiceOtpViseurTest {

    private ImpressionService service;

    @BeforeEach
    void setup() {
        service = new ImpressionService();
        AppliProperties appliProperties = mock(AppliProperties.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        when(appliProperties.getMailer().getDeliveryAddress()).thenReturn(null);
        service.appliProperties = appliProperties;
    }

    private Convention conventionAvecViseur(String mailViseur, String mailDelegataireViseur) {
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(1);
        centreGestion.setMail("mail.general@centre.fr");
        centreGestion.setTelephone("0600000000");
        centreGestion.setNomViseur("NomViseur");
        centreGestion.setPrenomViseur("PrenomViseur");
        centreGestion.setMailViseur(mailViseur);
        centreGestion.setNomDelegataireViseur("NomDelegataire");
        centreGestion.setPrenomDelegataireViseur("PrenomDelegataire");
        centreGestion.setMailDelegataireViseur(mailDelegataireViseur);

        CentreGestionSignataire signataire = new CentreGestionSignataire();
        signataire.setId(new org.esup_portail.esup_stage.model.CentreGestionSignataireId(1, SignataireEnum.viseur));
        signataire.setType(TypeSignatureEnum.otp);
        signataire.setOrdre(1);
        centreGestion.setSignataires(List.of(signataire));

        Convention convention = new Convention();
        convention.setCentreGestion(centreGestion);
        return convention;
    }

    @Test
    void utiliseMailDelegataireViseurQuandRenseigne() {
        Convention convention = conventionAvecViseur("viseur@centre.fr", "delegataire@centre.fr");

        String xml = service.generateXmlData(convention, TypeSignatureEnum.otp);

        assertThat(xml).contains("name=\"OTP_email_0\" value=\"delegataire@centre.fr\"");
        assertThat(xml).contains("name=\"OTP_firstname_0\" value=\"PrenomDelegataire\"");
        assertThat(xml).contains("name=\"OTP_lastname_0\" value=\"NomDelegataire\"");
        assertThat(xml).doesNotContain("mail.general@centre.fr");
        assertThat(xml).doesNotContain("viseur@centre.fr");
    }

    @Test
    void utiliseMailViseurQuandPasDeDelegataire() {
        Convention convention = conventionAvecViseur("viseur@centre.fr", null);

        String xml = service.generateXmlData(convention, TypeSignatureEnum.otp);

        assertThat(xml).contains("name=\"OTP_email_0\" value=\"viseur@centre.fr\"");
        assertThat(xml).contains("name=\"OTP_firstname_0\" value=\"PrenomViseur\"");
        assertThat(xml).contains("name=\"OTP_lastname_0\" value=\"NomViseur\"");
        assertThat(xml).doesNotContain("mail.general@centre.fr");
    }

    @Test
    void utiliseMailViseurQuandDelegataireVide() {
        Convention convention = conventionAvecViseur("viseur@centre.fr", "");

        String xml = service.generateXmlData(convention, TypeSignatureEnum.otp);

        assertThat(xml).contains("name=\"OTP_email_0\" value=\"viseur@centre.fr\"");
        assertThat(xml).doesNotContain("mail.general@centre.fr");
    }
}
