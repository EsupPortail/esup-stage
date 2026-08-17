package org.esup_portail.esup_stage.model;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests des méthodes métier de l'entité Convention (hors accesseurs).
 */
class ConventionMethodsTest {

    private static Date ilYA(int jours) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -jours);
        return calendar.getTime();
    }

    @Test
    void setValeurNomenclatureCopieLesLibelles() {
        Convention convention = new Convention();
        LangueConvention langue = new LangueConvention();
        langue.setLibelle("Français");
        convention.setLangueConvention(langue);
        TypeConvention type = new TypeConvention();
        type.setLibelle("Stage obligatoire");
        convention.setTypeConvention(type);
        Devise devise = new Devise();
        devise.setLibelle("Euro");
        convention.setDevise(devise);
        TempsTravail tempsTravail = new TempsTravail();
        tempsTravail.setLibelle("Temps plein");
        convention.setTempsTravail(tempsTravail);
        Theme theme = new Theme();
        theme.setLibelle("Informatique");
        convention.setTheme(theme);

        convention.setValeurNomenclature();

        ConventionNomenclature nomenclature = convention.getNomenclature();
        assertThat(nomenclature.getLangueConvention()).isEqualTo("Français");
        assertThat(nomenclature.getTypeConvention()).isEqualTo("Stage obligatoire");
        assertThat(nomenclature.getDevise()).isEqualTo("Euro");
        assertThat(nomenclature.getTempsTravail()).isEqualTo("Temps plein");
        assertThat(nomenclature.getTheme()).isEqualTo("Informatique");
        assertThat(nomenclature.getNatureTravail()).isNull();
    }

    @Test
    void setValeurNomenclatureReutiliseLaNomenclatureExistante() {
        Convention convention = new Convention();
        LangueConvention langue = new LangueConvention();
        langue.setLibelle("Anglais");
        convention.setLangueConvention(langue);
        TypeConvention type = new TypeConvention();
        type.setLibelle("Stage");
        convention.setTypeConvention(type);
        ConventionNomenclature existante = new ConventionNomenclature();
        convention.setNomenclature(existante);

        convention.setValeurNomenclature();

        assertThat(convention.getNomenclature()).isSameAs(existante);
        assertThat(existante.getLangueConvention()).isEqualTo("Anglais");
    }

    @Test
    void isAllSignedDateSettedExigeToutesLesDates() {
        Convention convention = new Convention();
        assertThat(convention.isAllSignedDateSetted()).isFalse();

        Date date = new Date();
        convention.setDateDepotEtudiant(date);
        convention.setDateSignatureEtudiant(date);
        convention.setDateDepotEnseignant(date);
        convention.setDateSignatureEnseignant(date);
        convention.setDateDepotTuteur(date);
        convention.setDateSignatureTuteur(date);
        convention.setDateDepotSignataire(date);
        convention.setDateSignatureSignataire(date);
        convention.setDateDepotViseur(date);
        assertThat(convention.isAllSignedDateSetted()).isFalse();

        convention.setDateSignatureViseur(date);
        assertThat(convention.isAllSignedDateSetted()).isTrue();
    }

    @Test
    void dureeExceptionnellePeriodeEstCalculeeALaVolee() {
        Convention convention = new Convention();
        convention.setNbHeuresHebdo("35");
        convention.setDureeExceptionnelle("154");

        assertThat(convention.getDureeExceptionnellePeriode()).isEqualTo("1 mois 0 jour(s) 0 heure(s)");

        Convention sansDonnees = new Convention();
        assertThat(sansDonnees.getDureeExceptionnellePeriode()).isNull();
    }

    @Test
    void depasseDelaiValidationSelonLeDelaiDuCentre() {
        Convention convention = new Convention();
        assertThat(convention.isDepasseDelaiValidation()).isFalse();

        CentreGestion centre = new CentreGestion();
        centre.setDelaiAlerteConvention(5);
        convention.setCentreGestion(centre);

        convention.setDateDebutStage(ilYA(10));
        convention.setValidationConvention(false);
        assertThat(convention.isDepasseDelaiValidation()).isTrue();

        convention.setDateDebutStage(null);
        assertThat(convention.isDepasseDelaiValidation()).isFalse();
    }
}
