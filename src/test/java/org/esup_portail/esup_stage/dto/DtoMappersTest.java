package org.esup_portail.esup_stage.dto;

import org.esup_portail.esup_stage.model.*;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Projections entité -> DTO utilisées par les listes et les fichiers :
 * on vérifie la copie des champs et la tolérance aux relations absentes.
 */
class DtoMappersTest {

    // ------------------------------------------------------------------
    // ConventionListDto
    // ------------------------------------------------------------------

    @Test
    void conventionListDtoProjetteToutesLesRelations() {
        Convention convention = new Convention();
        convention.setId(42);
        convention.setSujetStage("Développement");
        convention.setAnnee("2025/2026");
        convention.setDateDebutStage(new Date());

        Etudiant etudiant = new Etudiant();
        etudiant.setId(1);
        etudiant.setNom("Dupont");
        etudiant.setPrenom("Marie");
        etudiant.setIdentEtudiant("etu1");
        etudiant.setNumEtudiant("12345");
        convention.setEtudiant(etudiant);

        Enseignant enseignant = new Enseignant();
        enseignant.setId(2);
        enseignant.setNom("Martin");
        enseignant.setUidEnseignant("ens1");
        convention.setEnseignant(enseignant);

        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(3);
        centreGestion.setNomCentre("Centre Sciences");
        centreGestion.setValidationPedagogique(true);
        FicheEvaluation ficheEvaluation = new FicheEvaluation();
        ficheEvaluation.setValidationEtudiant(true);
        centreGestion.setFicheEvaluation(ficheEvaluation);
        convention.setCentreGestion(centreGestion);

        Ufr ufr = new Ufr();
        ufr.setId(new UfrId());
        ufr.setLibelle("Sciences");
        convention.setUfr(ufr);
        Etape etape = new Etape();
        etape.setId(new EtapeId());
        etape.setLibelle("Master 1");
        convention.setEtape(etape);

        Structure structure = new Structure();
        structure.setId(4);
        structure.setRaisonSociale("ACME");
        convention.setStructure(structure);

        ReponseEvaluation reponseEvaluation = new ReponseEvaluation();
        reponseEvaluation.setValidationEtudiant(true);
        reponseEvaluation.setImpressionEnseignant(true);
        convention.setReponseEvaluation(reponseEvaluation);

        Avenant avenant = new Avenant();
        avenant.setId(9);
        convention.setAvenants(List.of(avenant));

        ConventionListDto dto = ConventionListDto.from(convention);

        assertThat(dto.getId()).isEqualTo(42);
        assertThat(dto.getEtudiant().getNom()).isEqualTo("Dupont");
        assertThat(dto.getEtudiant().getNumEtudiant()).isEqualTo("12345");
        assertThat(dto.getEnseignant().getUidEnseignant()).isEqualTo("ens1");
        assertThat(dto.getCentreGestion().getNomCentre()).isEqualTo("Centre Sciences");
        assertThat(dto.getCentreGestion().isValidationPedagogique()).isTrue();
        assertThat(dto.getCentreGestion().getFicheEvaluation().getValidationEtudiant()).isTrue();
        assertThat(dto.getUfr().getLibelle()).isEqualTo("Sciences");
        assertThat(dto.getEtape().getLibelle()).isEqualTo("Master 1");
        assertThat(dto.getStructure().getRaisonSociale()).isEqualTo("ACME");
        assertThat(dto.getReponseEvaluation().getValidationEtudiant()).isTrue();
        assertThat(dto.getReponseEvaluation().getImpressionEnseignant()).isTrue();
        assertThat(dto.getAvenants()).hasSize(1);
        assertThat(dto.getAvenants().get(0).getId()).isEqualTo(9);
        assertThat(dto.getSujetStage()).isEqualTo("Développement");
    }

    @Test
    void conventionListDtoTolereLesRelationsAbsentes() {
        Convention convention = new Convention();
        convention.setId(7);

        ConventionListDto dto = ConventionListDto.from(convention);

        assertThat(dto.getId()).isEqualTo(7);
        assertThat(dto.getEtudiant()).isNull();
        assertThat(dto.getEnseignant()).isNull();
        assertThat(dto.getCentreGestion()).isNull();
        assertThat(dto.getUfr()).isNull();
        assertThat(dto.getEtape()).isNull();
        assertThat(dto.getStructure()).isNull();
        assertThat(dto.getReponseEvaluation()).isNull();
        assertThat(dto.getAvenants()).isEmpty();
    }

    @Test
    void conventionListDtoCentreSansFicheEvaluation() {
        Convention convention = new Convention();
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setNomCentre("Sans fiche");
        convention.setCentreGestion(centreGestion);

        ConventionListDto dto = ConventionListDto.from(convention);

        assertThat(dto.getCentreGestion().getFicheEvaluation()).isNull();
        assertThat(dto.getCentreGestion().isValidationConvention()).isFalse();
    }

    // ------------------------------------------------------------------
    // AvenantResponseDto
    // ------------------------------------------------------------------

    @Test
    void avenantResponseDtoProjetteLAvenantComplet() {
        Avenant avenant = new Avenant();
        avenant.setId(9);
        Convention convention = new Convention();
        convention.setId(42);
        avenant.setConvention(convention);
        avenant.setTitreAvenant("Prolongation");
        avenant.setMotifAvenant("Stage prolongé");
        avenant.setRupture(false);
        avenant.setModificationPeriode(true);
        avenant.setDateDebutStage(new Date());
        avenant.setDateFinStage(new Date());
        avenant.setSujetStage("Nouveau sujet");
        avenant.setValidationAvenant(true);
        avenant.setMontantGratification("650");
        avenant.setDocumentId("DOC9");

        Service service = new Service();
        service.setNom("R&D");
        avenant.setService(service);
        Contact contact = new Contact();
        contact.setNom("Tuteur");
        avenant.setContact(contact);
        Enseignant enseignant = new Enseignant();
        enseignant.setNom("Martin");
        avenant.setEnseignant(enseignant);
        UniteGratification uniteGratification = new UniteGratification();
        uniteGratification.setLibelle("€/mois");
        avenant.setUniteGratification(uniteGratification);
        ModeVersGratification modeVersGratification = new ModeVersGratification();
        modeVersGratification.setLibelle("Virement");
        avenant.setModeVersGratification(modeVersGratification);
        Devise devise = new Devise();
        devise.setLibelle("Euro");
        avenant.setDevise(devise);
        UniteDuree uniteDuree = new UniteDuree();
        uniteDuree.setLibelle("mois");
        avenant.setUniteDuree(uniteDuree);

        AvenantResponseDto dto = AvenantResponseDto.from(avenant);

        assertThat(dto.getId()).isEqualTo(9);
        assertThat(dto.getIdConvention()).isEqualTo(42);
        assertThat(dto.getTitreAvenant()).isEqualTo("Prolongation");
        assertThat(dto.isModificationPeriode()).isTrue();
        assertThat(dto.isValidationAvenant()).isTrue();
        assertThat(dto.getService().getNom()).isEqualTo("R&D");
        assertThat(dto.getContact().getNom()).isEqualTo("Tuteur");
        assertThat(dto.getEnseignant().getNom()).isEqualTo("Martin");
        assertThat(dto.getUniteGratification().getLibelle()).isEqualTo("€/mois");
        assertThat(dto.getModeVersGratification().getLibelle()).isEqualTo("Virement");
        assertThat(dto.getDevise().getLibelle()).isEqualTo("Euro");
        assertThat(dto.getUniteDuree().getLibelle()).isEqualTo("mois");
        assertThat(dto.getDocumentId()).isEqualTo("DOC9");
    }

    @Test
    void avenantResponseDtoTolereLesRelationsAbsentes() {
        Avenant avenant = new Avenant();
        avenant.setId(9);

        AvenantResponseDto dto = AvenantResponseDto.from(avenant);

        assertThat(dto.getIdConvention()).isZero();
        assertThat(dto.getService()).isNull();
        assertThat(dto.getContact()).isNull();
        assertThat(dto.getEnseignant()).isNull();
        assertThat(dto.getUniteGratification()).isNull();
        assertThat(dto.getDevise()).isNull();
    }

    // ------------------------------------------------------------------
    // DTO à builder (explorateur de fichiers de logs)
    // ------------------------------------------------------------------

    @Test
    void fileElementDtoSeConstruitParBuilder() {
        Date modification = new Date();
        FileElementDto element = FileElementDto.builder()
                .id("logs/app.log")
                .name("app.log")
                .path("logs")
                .isFolder(false)
                .size(1024L)
                .lastModified(modification)
                .extension("log")
                .parent("logs")
                .build();

        assertThat(element.getId()).isEqualTo("logs/app.log");
        assertThat(element.getName()).isEqualTo("app.log");
        assertThat(element.isFolder()).isFalse();
        assertThat(element.getSize()).isEqualTo(1024L);
        assertThat(element.getLastModified()).isEqualTo(modification);
        assertThat(element.getExtension()).isEqualTo("log");
        assertThat(element.getParent()).isEqualTo("logs");

        FileElementDto identique = FileElementDto.builder()
                .id("logs/app.log")
                .name("app.log")
                .path("logs")
                .isFolder(false)
                .size(1024L)
                .lastModified(modification)
                .extension("log")
                .parent("logs")
                .build();
        assertThat(element).isEqualTo(identique).hasSameHashCodeAs(identique);
        assertThat(element.toString()).contains("app.log");
        assertThat(FileElementDto.builder().toString()).isNotBlank();
    }

    @Test
    void fileContentDtoSeConstruitParBuilder() {
        FileContentDto contenu = FileContentDto.builder()
                .fileName("app.log")
                .content("ligne 1\nligne 2")
                .totalLines(2)
                .page(1)
                .pageSize(100)
                .build();

        assertThat(contenu.getFileName()).isEqualTo("app.log");
        assertThat(contenu.getContent()).contains("ligne 1");
        assertThat(contenu.getTotalLines()).isEqualTo(2);
        assertThat(contenu.getPage()).isEqualTo(1);
        assertThat(contenu.getPageSize()).isEqualTo(100);

        FileContentDto identique = FileContentDto.builder()
                .fileName("app.log")
                .content("ligne 1\nligne 2")
                .totalLines(2)
                .page(1)
                .pageSize(100)
                .build();
        assertThat(contenu).isEqualTo(identique).hasSameHashCodeAs(identique);
        assertThat(contenu.toString()).contains("app.log");
        assertThat(FileContentDto.builder().toString()).isNotBlank();
    }
}
