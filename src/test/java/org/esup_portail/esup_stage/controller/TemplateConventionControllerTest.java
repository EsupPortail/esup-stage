package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.TemplateConventionDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.repository.ParamConventionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TemplateConventionControllerTest {

    private TemplateConventionController controller;

    @BeforeEach
    void setup() {
        ParamConventionJpaRepository paramConventionJpaRepository = mock(ParamConventionJpaRepository.class);
        when(paramConventionJpaRepository.findAll()).thenReturn(Collections.emptyList());

        controller = new TemplateConventionController();
        ReflectionTestUtils.setField(controller, "paramConventionJpaRepository", paramConventionJpaRepository);
    }

    @Test
    void checkTemplateConventionRejectsNativeFreemarkerDirectives() {
        TemplateConventionDto dto = new TemplateConventionDto("<#assign x = 1>", "");

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(controller, "checkTemplateConvention", dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("directives FreeMarker natives");
    }

    @Test
    void checkTemplateConventionStillAllowsBusinessIfSyntax() {
        TemplateConventionDto dto = new TemplateConventionDto("$IF(true)Texte$ENDIF", "");

        ReflectionTestUtils.invokeMethod(controller, "checkTemplateConvention", dto);
    }

    @Test
    void checkTemplateConventionStillAllowsHtmlClosingTags() {
        TemplateConventionDto dto = new TemplateConventionDto("<p>Texte</p>", "");

        ReflectionTestUtils.invokeMethod(controller, "checkTemplateConvention", dto);
    }
}
