package org.esup_portail.esup_stage.config;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.sql.init.dependency.DatabaseInitializationDependencyConfigurer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LiquibaseInitializationOrderTest {

    @Test
    void entityManagerFactoryDependsOnLiquibase() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(DependencyConfigurerImport.class);
            context.registerBean("liquibase", SpringLiquibase.class, () -> mock(SpringLiquibase.class));
            context.registerBean("entityManagerFactory", EntityManagerFactory.class, () -> mock(EntityManagerFactory.class));
            context.refresh();

            assertThat(context.getBeanFactory().getBeanDefinition("entityManagerFactory").getDependsOn())
                    .contains("liquibase");
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Import(DatabaseInitializationDependencyConfigurer.class)
    static class DependencyConfigurerImport {
    }
}
