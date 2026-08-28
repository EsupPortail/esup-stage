package org.esup_portail.esup_stage.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sans {@link CascadeType#REMOVE}, la suppression d'une convention portant un avenant
 * avec périodes d'interruption échoue sur la contrainte
 * fk_PeriodeInterruptionAvenant_Avenant1 (SQL 1451).
 */
class AvenantCascadeTest {

    @Test
    void lesPeriodesDInterruptionSontSupprimeesAvecLAvenant() throws NoSuchFieldException {
        Field field = Avenant.class.getDeclaredField("periodeInterruptionAvenants");
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);

        assertThat(oneToMany).isNotNull();
        assertThat(oneToMany.mappedBy()).isEqualTo("avenant");
        assertThat(oneToMany.cascade()).contains(CascadeType.REMOVE);
    }
}
